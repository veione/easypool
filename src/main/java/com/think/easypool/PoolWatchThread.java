package com.think.easypool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches a create new connections.
 * 
 * @author veione
 * 
 */
public class PoolWatchThread implements Runnable {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(PoolWatchThread.class);
	/** Pool handle.  */
	private ConnectionPool pool;
	/** 多长时间后重试添加连接失败时等待。*/
	private long acquireRetryDelayInMs = 1000L;
	private ConnectionConfig config;
	/** Mostly used to break out easily in unit testing.  */
	private boolean signalled;

	public PoolWatchThread(ConnectionPool pool) {
		this.pool = pool;
		this.config = this.pool.getConfig();
		this.acquireRetryDelayInMs = this.pool.getConfig().getAcquireRetryDelayInMs();
	}

	public void run() {
		int maxNewConnections;
		int needReduceConnections;
		
		while (!this.signalled) {
			maxNewConnections = 0;
			try {
				maxNewConnections = this.config.getMaxConnections() - this.pool.getCreatedConnections();
				needReduceConnections = this.pool.getAvailableConnections() - this.config.getMaxConnections();
				if (maxNewConnections > 0 && !this.pool.poolShuttingDown) {
					fillConnections(Math.min(maxNewConnections, this.config.getMinConnections()));
					if (this.pool.getCreatedConnections() < this.config.getMinConnections()) {
						fillConnections(this.config.getMinConnections() - this.pool.getCreatedConnections());
					}
				}
				
				if (needReduceConnections > 0) {
					for (int i = 0; i < needReduceConnections; i++) {
						this.pool.getFreeConnections().poll();
					}
				}
				
				if (this.pool.poolShuttingDown) {
					return;
				}
				
				// sleep a while
				Thread.sleep(this.config.getValidationInterval());
			} catch (InterruptedException e) {
				logger.debug("Terminating pool watch thread");
				return; // we've been asked to terminate.
			}
		}
	}
	/**
	 * Adds new connections to the pool.
	 * @param connectionToCreate number of connections to be create
	 * @throws InterruptedException
	 */
	private void fillConnections(int connectionToCreate) throws InterruptedException {
		try {
			for (int i = 0; i < connectionToCreate; i++) {
				if (this.pool.poolShuttingDown) {
					break;
				}
				this.pool.addFreeConnection(new ConnectionHandle(this.config));
			}
		} catch (Exception e) {
			logger.error("Error in trying to obtain a connection. Retrying in " + this.acquireRetryDelayInMs + "ms");
			Thread.sleep(this.acquireRetryDelayInMs);
		}
	}
}
