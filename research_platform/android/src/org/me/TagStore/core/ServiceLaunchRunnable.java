package org.me.TagStore.core;


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
	private Context m_context;

	/**
	 * stores the service connection
	 */
	private final MainServiceConnection m_connection;
	
	
	/**
	 * constructor of class ServiceLaunchRunnable
	 * @param context
	 * @param mainPagerActivity TODO
	 */
	public ServiceLaunchRunnable(Context context, MainServiceConnection connection) {
		
		//
		// init members
		//
		m_context = context;
		m_connection = connection;
	}

	
	public void run() {

		//
		// now create intent to launch the file watch dog service
		//
		Intent intent = new Intent(m_context, FileWatchdogService.class);
		Logger.i("Service::launch " + System.currentTimeMillis());
		
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