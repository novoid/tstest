package org.me.tagstore.core;

import java.util.HashSet;

import org.me.tagstore.interfaces.FileSystemObserverNotification;
import org.me.tagstore.interfaces.WatchdogServiceConnection;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MainServiceConnection implements ServiceConnection,
		WatchdogServiceConnection {

	/**
	 * holds reference to service
	 */
	private FileWatchdogService m_service = null;

	/**
	 * stores observer notifications
	 */
	private final HashSet<FileSystemObserverNotification> m_observers = new HashSet<FileSystemObserverNotification>();

	/**
	 * stores the startup timestamp
	 */
	private long m_timestamp;

	/**
	 * setting if service is connected
	 */
	private boolean m_service_connected;

	/**
	 * returns true when service is connected
	 * 
	 * @return boolean
	 */
	public boolean isServiceConnected() {
		Logger.i("isServiceConnected: " + m_service_connected);
		return m_service != null;
	}

	/**
	 * returns the timestamp when the service has connected
	 * 
	 * @return
	 */
	public long getServiceConnectTimestamp() {
		return m_timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.me.tagstore.core.WatchdogServiceConnection#unregisterExternalNotification
	 * (org.me.tagstore.interfaces.FileSystemObserverNotification)
	 */
	public boolean unregisterExternalNotification(
			FileSystemObserverNotification notification) {

		if (m_service != null) {
			//
			// unregister notification
			//
			return m_service.unregisterExternalNotification(notification);
		}

		//
		// failed to unregister
		//
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.me.tagstore.core.WatchdogServiceConnection#registerExternalNotification
	 * (org.me.tagstore.interfaces.FileSystemObserverNotification)
	 */
	public boolean registerExternalNotification(
			FileSystemObserverNotification notification) {

		if (m_service != null) {
			//
			// register our notification handler
			//
			return m_service.registerExternalNotification(notification);
		} else {
			//
			// perform this synchronized
			//
			synchronized (this) {
				//
				// add to pending notification list
				//
				return m_observers.add(notification);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.me.tagstore.core.WatchdogServiceConnection#registerDirectory(java
	 * .lang.String)
	 */
	public boolean registerDirectory(String path) {

		if (m_service != null) {

			//
			// register the path
			//
			return m_service.registerDirectory(path);
		} else {
			//
			// no service yet connected
			//
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.me.tagstore.core.WatchdogServiceConnection#unregisterDirectory(java
	 * .lang.String)
	 */
	public boolean unregisterDirectory(String path) {

		if (m_service != null) {
			//
			// unregister path
			//
			return m_service.unregisterDirectory(path);
		}

		//
		// no service yet connected
		//
		return false;
	}

	@SuppressWarnings("unchecked")
	public void onServiceConnected(ComponentName name, IBinder service) {

		m_service_connected = true;
		//
		// store service
		//
		m_service = ((FileWatchdogServiceBinder<FileWatchdogService>) service)
				.getService();

		//
		// save timestamp
		//
		m_timestamp = System.currentTimeMillis();

		Logger.e("Service::launch " + m_timestamp);

		//
		// register any pending observers
		//
		synchronized (this) {
			for (FileSystemObserverNotification observer : m_observers) {
				//
				// register observers
				//
				boolean result = m_service
						.registerExternalNotification(observer);
				if (!result) {
					Logger.e("Error: failed to register external observer");
				}
			}

			//
			// clear list
			//
			m_observers.clear();
		}
		Logger.i("MainActivity::onServiceConnected service " + m_service);
	}

	public void onServiceDisconnected(ComponentName name) {

		//
		// should not happen
		//
		m_service = null;
		Logger.e("Error: Service disconnected!!!");
	}
}