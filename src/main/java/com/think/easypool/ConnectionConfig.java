package com.think.easypool;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Configuration class.
 * 
 * @author veione
 */
public class ConnectionConfig implements Cloneable, Serializable {
	/** Serialization UID. */
	private static final long serialVersionUID = -2347907240421286388L;
	/** Remote host */
	private String host;
	/** Remote port */
	private int port;
	/** Socket timeout */
	private int timeout;
	/** Min number of connections */
	private int minConnections;
	/** Max number of connections */
	private int maxConnections;
	/** Interval check time */
	private long validationInterval;
	/** How long to wait before retrying to add a connection upon failure. */
	private long acquireRetryDelayInMs;
	/** Config file. */
	private String configFile;
	/** Name of the pool. */
	private String poolName;
	/** Enable pre load , default disable preload. */
	private boolean preload = false;

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	/**
	 * @return the minConnections
	 */
	public int getMinConnections() {
		return minConnections;
	}

	/**
	 * @param minConnections the minConnections to set
	 */
	public void setMinConnections(int minConnections) {
		this.minConnections = minConnections;
	}

	/**
	 * @return the maxConnections
	 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/**
	 * @param maxConnections the maxConnections to set
	 */
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	/**
	 * @return the validationInterval
	 */
	public long getValidationInterval() {
		return validationInterval;
	}

	/**
	 * @param validationInterval
	 *            the validationInterval to set
	 */
	public void setValidationInterval(long validationInterval) {
		this.validationInterval = validationInterval;
	}

	/**
	 * @return the configFile
	 */
	public String getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile
	 *            the configFile to set
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return the poolName
	 */
	public String getPoolName() {
		return poolName;
	}

	/**
	 * @param poolName
	 *            the poolName to set
	 */
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public long getAcquireRetryDelayInMs() {
		return acquireRetryDelayInMs;
	}

	public void setAcquireRetryDelayInMs(long acquireRetryDelayInMs) {
		this.acquireRetryDelayInMs = acquireRetryDelayInMs;
	}
	
	public boolean isPreload() {
		return preload;
	}

	public void setPreload(boolean preload) {
		this.preload = preload;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		ConnectionConfig clone = (ConnectionConfig) super.clone();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.set(clone, field.get(this));
			} catch (Exception e) {
				// should never happen
			}
		}
		return clone;
	}
}
