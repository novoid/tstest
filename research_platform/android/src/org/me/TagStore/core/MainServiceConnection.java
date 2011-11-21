package org.me.TagStore.core;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MainServiceConnection implements ServiceConnection {

	/**
	 * holds reference to service
	 */
	private FileWatchdogService m_service;

	/**
	 * register notification
	 */
	private final MainFileSystemObserverNotification m_notification;
	
	
	/**
	 * @param mainPagerActivity
	 */
	public MainServiceConnection(Activity activity) {
		
		//
		// init members
		//
		m_service = null;
		m_notification = new MainFileSystemObserverNotification(activity);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {

		//
		// store service
		//
		m_service = ((FileWatchdogServiceBinder<FileWatchdogService>) service)
				.getService();

		Logger.i("MainActivity::onServiceConnected service " + m_service);

		if (m_service != null) {
			//
			// register our notification handler
			//
			m_service.registerExternalNotification(m_notification);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

		//
		// should not happen
		//
		m_service = null;
		Logger.e("Error: Service disconnected!!!");
	}
}