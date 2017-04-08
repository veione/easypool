package com.think.easypool;

import java.io.Serializable;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

/**
 * Connection handle wrapper around a Thrift socket.
 * 
 * @author veione
 * 
 */
public class ConnectionHandle extends TSocket implements Serializable {
	/** uid */
	private static final long serialVersionUID = 4894317087754918413L;
	/** identify id */
	private String id;
	/** Connection close flag. */
	private volatile boolean isClose;
	/** Connection configuration model class. */
	private ConnectionConfig config;

	/**
	 * Default constructor
	 * 
	 * @param host
	 *            Remote host.
	 * @param port
	 *            Remote port
	 */
	public ConnectionHandle(String host, int port) {
		super(host, port, 0);
	}

	/**
	 * Full constructor
	 * 
	 * @param host
	 *            Remote host.
	 * @param port
	 *            Remote port.
	 * @param timeout
	 *            Socket timeout
	 */
	public ConnectionHandle(String host, int port, int timeout) {
		super(host, port, timeout);
	}

	public ConnectionHandle(ConnectionConfig config) {
		this(config.getHost(), config.getPort(), config.getTimeout());
		this.config = config;
		try {
			this.open();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isClose() {
		return isClose;
	}

	public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

	public ConnectionConfig getConfig() {
		return config;
	}

	public void setConfig(ConnectionConfig config) {
		this.config = config;
	}

}
