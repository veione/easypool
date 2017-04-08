package com.think.easypool;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection pool (main class);
 * 
 * @author veione
 * 
 */
public class ConnectionPool implements Serializable, Closeable {
	/** Serialization UID */
	private static final long serialVersionUID = 2850294816261604229L;
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	/** The ConnectionPool is shutdown flag. */
	public volatile boolean poolShuttingDown = false;
	/** Connection pool. */
	private ConcurrentLinkedQueue<ConnectionHandle> connPool;
	/** Connections available to be taken  */
	private BlockingQueue<ConnectionHandle> freeConnections;
	/** Busy connections. */
	private BlockingQueue<ConnectionHandle> busyConnections;
	/** Statistics lock. */
	protected ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();
	/** Number of connections that have been created. */
	private int createdConnections=0;
	/** How long to wait before retrying to add a connection upon failure. */
	private long acquireRetryDelayInMs = 1000L;
	/** Connection configuration model class. */
	private ConnectionConfig config;
	/** 
	 * Executor for threads watching each connection to dynamically create new threads/kill off excess ones.
	 */
	protected ExecutorService connectionsScheduler;
	
	public void close() throws IOException {
		shutdown();
	}

	public synchronized void shutdown() {
		poolShuttingDown = true;
		freeConnections.clear();
		busyConnections.clear();
	}

	/**
	 * Constructor.
	 * @param config Configuration for pool
	 */
	public ConnectionPool(ConnectionConfig config) { 
		this.config = config;
		this.freeConnections = new ArrayBlockingQueue<ConnectionHandle>(this.config.getMaxConnections());
		this.busyConnections = new ArrayBlockingQueue<ConnectionHandle>(this.config.getMaxConnections());
		this.connPool = new ConcurrentLinkedQueue<ConnectionHandle>();
		if (this.config.isPreload()) {
			try {
				fillConnections(this.config.getMinConnections());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 
		
		if (this.config.getAcquireRetryDelayInMs() != 0) {
			this.acquireRetryDelayInMs = this.config.getAcquireRetryDelayInMs();
		}
		
		String suffix = "";
		if (this.config.getPoolName() != null) {
			suffix = "-" + this.config.getPoolName();
		}
		// watch this pool connectinos.
		this.connectionsScheduler = Executors.newFixedThreadPool(this.config.getMinConnections(), new DefaultThreadFactory("EasyPool-watch-thread"+suffix, false));
		this.connectionsScheduler.execute(new PoolWatchThread(this));
	}
	
	/**
	 * Get a connection from connection pool.
	 * @return ConnectionHandler object or null.
	 */
	public ConnectionHandle getConnection() {
		ConnectionHandle connection = null;

		if (!poolShuttingDown) {
			if (this.getAvailableConnections() == 0) {
				try {
					fillConnections(this.config.getMinConnections());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			connection = this.freeConnections.poll();
			if (connection != null) {
				connPool.add(connection);
				busyConnections.offer(connection);
			}
		} else {
			throw new RuntimeException("pool is shutdown.");
		}
		logger.info("createConnections={}, freeConnections={}, busyConnections={}", this.getCreatedConnections(), this.getAvailableConnections(), this.getBusyConnections().size());
		return connection;
	}
	
	/**
	 * Reback connection to connection pool.
	 * @param connection 
	 */
	public void returnConnection(ConnectionHandle connection) {
		if (connection == null || !isConnectionHandleAlive(connection)) {
			return;
		}
		connPool.add(connection);
		busyConnections.remove(connection);
		if (!this.freeConnections.offer(connection)) {
			// if connection offer failed, then close the connection.
			connection.close();
		}
		logger.info("createConnections={}, freeConnections={}, busyConnections={}", this.getCreatedConnections(), this.getAvailableConnections(), this.getBusyConnections().size());
	}
	
	/**
	 * Release the given connection back to to the pool. 
	 * @param connection to release
	 */
	public void releaseConnection(ConnectionHandle connection) {
		if (!this.poolShuttingDown && connection != null) {
			connection.close();
			busyConnections.remove(connection);
			freeConnections.remove(connection);
			connPool.remove(connection);
		}
		logger.info("createConnections={}, freeConnections={}, busyConnections={}", this.getCreatedConnections(), this.getAvailableConnections(), this.getBusyConnections().size());
	}
	
	/**
	 * 需要被填充的连接
	 * @param connectionsToCreate 需要被创建的连接数量
	 * @throws InterruptedException 
	 */
	private void fillConnections(int connectionsToCreate) throws InterruptedException {
		try {
			for (int i = 0; i < connectionsToCreate; i++) {
				if (poolShuttingDown) {
					break;
				}
				ConnectionHandle handle = new ConnectionHandle(this.config);
				this.addFreeConnection(handle);
				this.connPool.offer(handle);
			}
		} catch (Exception e) {
			Thread.sleep(this.acquireRetryDelayInMs);
		}
		logger.info("createConnections={}, freeConnections={}, busyConnections={}", this.getCreatedConnections(), this.getAvailableConnections(), this.getBusyConnections().size());
	}
	
	/**
	 * Adds a free connection.
	 * 
	 * @param connectionHandle
	 */
	public void addFreeConnection(ConnectionHandle connectionHandle) {
		updateCreatedConnections(1);
		// 插入元素失败
		if (!this.freeConnections.offer(connectionHandle)) {
			// add connection failed. rollback.
			updateCreatedConnections(-1);
			//关闭该连接
			connectionHandle.close();
		}
	}
	
	/**
	 * 更新空闲的连接统计
	 * @param increment
	 */
	private void updateCreatedConnections(int increment) {
		try {
			this.statsLock.writeLock().lock();
			this.createdConnections += increment;
		} finally {
			this.statsLock.writeLock().unlock();
		}
	}

	/**
	 * @return the freeConnections
	 */
	public BlockingQueue<ConnectionHandle> getFreeConnections() {
		return freeConnections;
	}

	/**
	 * @param freeConnections the freeConnections to set
	 */
	public void setFreeConnections(BlockingQueue<ConnectionHandle> freeConnections) {
		this.freeConnections = freeConnections;
	}

	
	/**
	 * Returns the number of avail connections
	 * @return available connections.
	 */
	protected int getAvailableConnections() {
		return this.freeConnections.size();
	}
	
	/**
	 * Returns the number of current created connections
	 * @return created connections.
	 */
	public int getCreatedConnections() {
		try {
			this.statsLock.readLock().lock();
			return this.createdConnections;
		} finally {
			this.statsLock.readLock().unlock();
		}
	}
	
	/**
	 * 当前连接时候还存活
	 * @param connection 当前连接对象
	 * @return true表示还存活在,否则false.
	 */
	public boolean isConnectionHandleAlive(ConnectionHandle connection) {
		return connection.isOpen();
	}

	public long getAcquireRetryDelayInMs() {
		return acquireRetryDelayInMs;
	}

	public void setAcquireRetryDelayInMs(long acquireRetryDelayInMs) {
		this.acquireRetryDelayInMs = acquireRetryDelayInMs;
	}

	public BlockingQueue<ConnectionHandle> getBusyConnections() {
		return busyConnections;
	}

	public ConnectionConfig getConfig() {
		return config;
	}

	public void setConfig(ConnectionConfig config) {
		this.config = config;
	}

}
