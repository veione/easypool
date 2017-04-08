package com.think.easypool;

public class TestManager {
	public static void main(String[] args) {
		ConnectionConfig config = new ConnectionConfig();
		config.setHost("10.150.0.53");
		config.setPort(9092);
		config.setMaxConnections(30);
		config.setMinConnections(6);
		config.setPoolName("Thrift-Client-Pool");
		config.setTimeout(3000);
		config.setValidationInterval(3000);
		config.setAcquireRetryDelayInMs(3000);
		config.setPreload(true);
		ConnectionManager manager = ConnectionManager.getInstance();
		manager.setConnectionConfig(config);
		manager.getConnection();
	}
}
