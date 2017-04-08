package com.think.easypool;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 * @throws InterruptedException 
	 */
	public void testApp() throws InterruptedException {
		ConnectionConfig config = new ConnectionConfig();
		config.setHost("10.150.0.53");
		config.setPort(9092);
		config.setMaxConnections(30);
		config.setMinConnections(6);
		config.setPoolName("Thrift-Client-Pool");
		config.setTimeout(3000);
		config.setValidationInterval(3000);
		config.setAcquireRetryDelayInMs(3000);
		config.setPreload(false);

		ConnectionPool pool = new ConnectionPool(config);
		
		for (int i = 0; i < 1000; i++) {
			Thread thread = new Thread(new Worker(pool));
			thread.start();
		}
		Thread.sleep(2000);
		ConnectionHandle connection = pool.getConnection();
		
		pool.returnConnection(connection);
	}
}
