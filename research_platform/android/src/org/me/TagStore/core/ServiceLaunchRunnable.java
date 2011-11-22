package org.me.TagStore.core;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * This class implements starting the file watch dog service in a separate thread, which improves startup time
 * @author Johannes Anderwald
 *
 */
public class ServiceLaunchRunnable implements Runnable {

	/**
	 * stores the context
	 */
	private Activity m_context;

	/**
	 * stores the service connection
	 */
	private final MainServiceConnection m_connection;
	
	
	/**
	 * constructor of class ServiceLaunchRunnable
	 * @param context
	 * @param mainPagerActivity TODO
	 */
	public ServiceLaunchRunnable(Activity activity, MainServiceConnection connection) {
		
		//
		// init members
		//
		m_context = activity;
		m_connection = connection;
	}

	@Override
	public void run() {

		//
		// now create intent to launch the file watch dog service
		//
		Intent intent = new Intent(m_context, FileWatchdogService.class);

		//
		// start service
		//
		m_context.startService(intent);

		//
		// bind the service now
		//
		m_context.bindService(intent, m_connection,
				Context.BIND_AUTO_CREATE);

		Logger.d("ServiceLaunchRunnable::run service started");
	}
}