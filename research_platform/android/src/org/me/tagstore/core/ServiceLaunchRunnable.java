package org.me.tagstore.core;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * This class implements starting the file watch dog service in a separate
 * thread, which improves startup time
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
	private ServiceConnection m_connection;

	/**
	 * initializes the service launch runnable
	 * 
	 * @param context
	 *            application context
	 * @param connection
	 *            service connection
	 */
	public void initializeServiceLaunchRunnable(Context context,
			ServiceConnection connection) {

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
		m_context.bindService(intent, m_connection, Context.BIND_AUTO_CREATE);

		Logger.d("ServiceLaunchRunnable::run service started");
	}
}