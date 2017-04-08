package com.think.easypool;

public class Worker implements Runnable {
	private ConnectionPool pool;
	
	public Worker(ConnectionPool pool) {
		this.pool = pool;
	}

	public void run() {
		// brrown a connection
		ConnectionHandle connection = this.pool.getConnection();
		// return a connection
		this.pool.returnConnection(connection);
		// release a connection
		connection = this.pool.getConnection();
		this.pool.releaseConnection(connection);
	}

}
