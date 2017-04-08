package com.think.easypool;

/**
 * Connection manage class.
 * 
 * @author veione
 * 
 */
public class ConnectionManager {
	/** Connection pool. */
	private ConnectionPool pool;
	/** Connection config. */
	private ConnectionConfig config;

	/**
	 * Default constructor.
	 */
	private ConnectionManager() {
	}
	
	/**
	 * Single Pattern Holder class.
	 */
	public static final class ConnectionManagerHolder {
		public static ConnectionManager INSTANCE = new ConnectionManager();
	}
	
	public static ConnectionManager getInstance() {
		return ConnectionManagerHolder.INSTANCE;
	}
	
	public void setConnectionConfig(ConnectionConfig config) {
		this.config = config;
		this.pool = new ConnectionPool(this.config);
	}

	public void setConnectionPool(ConnectionPool pool) {
		this.pool = pool;
	}

	/**
	 * Release connection and close connection.
	 * @param connection
	 */
	public void releaseConnection(ConnectionHandle connection) {
		this.pool.releaseConnection(connection);
	}
	
	/**
	 * obtain a connection from free connections queue.
	 * @return
	 */
	public ConnectionHandle getConnection() {
		return this.pool.getConnection();
	}

	/**
	 * reback connection to connection pool.
	 * @param connection
	 */
	public void returnConnection(ConnectionHandle connection) {
		this.pool.returnConnection(connection);
	}

	public ConnectionPool getPool() {
		return pool;
	}

	public ConnectionConfig getConfig() {
		return config;
	}

	/**
	 * get num of free connections.
	 * @return
	 */
	public int getFreeConnections() {
		return this.pool.getFreeConnections().size();
	}
	
	/**
	 * get num of created connections.
	 * @return
	 */
	public int getCreatedConnections() {
		return this.pool.getCreatedConnections();
	}
	
	/**
	 * get num of busy connections.
	 * @return
	 */
	public int getBusyConnections() {
		return this.pool.getBusyConnections().size();
	}
}
