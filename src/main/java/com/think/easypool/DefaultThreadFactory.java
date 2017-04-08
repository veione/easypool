package com.think.easypool;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default thread factory class.
 * 
 * @author veione
 * 
 */
public class DefaultThreadFactory implements ThreadFactory,
		UncaughtExceptionHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(DefaultThreadFactory.class);
	/** Daemon state. */
	private boolean daemon;
	/** Thread name. */
	private String threadName;

	public DefaultThreadFactory(String threadName, boolean daemon) {
		this.threadName = threadName;
		this.daemon = daemon;
	}

	public Thread newThread(Runnable r) {
		Thread task = new Thread(r, this.threadName);
		task.setDaemon(daemon);
		task.setUncaughtExceptionHandler(this);
		return task;
	}

	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught Exception in thread " + t.getName(), e);
	}

}
