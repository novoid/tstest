package org.me.TagStore.core;

import java.util.ArrayList;

import org.me.TagStore.interfaces.FileSystemObserverNotification;

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
	 * stores observer notifications
	 */
	private final ArrayList<FileSystemObserverNotification> m_observers;
	
	/**
	 * stores the startup timestamp
	 */
	private long m_timestamp;
	
	
	/**
	 * @param mainPagerActivity
	 */
	public MainServiceConnection(Activity activity) {
		
		//
		// init members
		//
		m_service = null;
		m_observers = new ArrayList<FileSystemObserverNotification>();
	}

	/**
	 * returns true when service is connected
	 * @return boolean
	 */
	public boolean isServiceConnected() {
		return m_service != null;
	}
	
	/**
	 * returns the timestamp when the service has connected
	 * @return
	 */
	public long getServiceConnectTimestamp() {
		return m_timestamp;
	}
	
	/**
	 * unregisters the external observers
	 * @param notification to be unregistered
	 */
	public boolean unregisterExternalNotification(FileSystemObserverNotification notification) {
		
		if (m_service != null) 
		{
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
	
	/**
	 * registers un external observer
	 * @param notification to be registered
	 * @return true on success
	 */
	public boolean registerExternalNotification(FileSystemObserverNotification notification) {
		
		if (m_service != null) {
			//
			// register our notification handler
			//
			return m_service.registerExternalNotification(notification);
		}
		else
		{
			//
			// perform this synchronized
			//
			synchronized(this)
			{
				//
				// add to pending notification list
				//
				return m_observers.add(notification);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {

		//
		// store service
		//
		m_service = ((FileWatchdogServiceBinder<FileWatchdogService>) service)
				.getService();

		//
		// save timestamp
		//
		m_timestamp = System.currentTimeMillis();
		
		//
		// register any pending observers
		//
		synchronized(this)
		{
			for(FileSystemObserverNotification observer : m_observers)
			{
				//
				// register observers
				//
				boolean result = m_service.registerExternalNotification(observer);
				if (!result)
				{
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

	@Override
	public void onServiceDisconnected(ComponentName name) {

		//
		// should not happen
		//
		m_service = null;
		Logger.e("Error: Service disconnected!!!");
	}
}