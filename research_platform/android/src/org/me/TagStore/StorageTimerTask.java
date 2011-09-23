package org.me.TagStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Environment;

/**
 * This class is used to query the storage disk state. If the disk is available, it will invoke the registered callbacks
 * @author Johannes Anderwald
 *
 */
public class StorageTimerTask extends TimerTask {

	/**
	 * This abstract class is used to invoke callbacks when disk is available / no longer available 
	 * @author Johannes Anderwald
	 *
	 */
	public interface TimerTaskCallback {
		
		/**
		 * invoked when the disk is available
		 */
		public void diskAvailable();
		
		/**
		 * invoked when the disk is not available
		 */
		public void diskNotAvailable();
	}

	/**
	 * timer object
	 */
	private Timer m_Timer;
	
	/**
	 * holds a list of call backs
	 */
	HashSet<TimerTaskCallback> m_callbacks;
	
	
	/**	
	 * sole instance of timer task
	 */
	private static StorageTimerTask s_Instance;
	
	/**
	 * constructor of class StorageTimerTask
	 */
	private StorageTimerTask() {
		
		//
		// create new timer
		//
		m_Timer = new Timer();
		
		//
		// construct new array list
		//
		m_callbacks = new HashSet<TimerTaskCallback>();
	}
	
	
	/**
	 * acquires an instance of the class StorageTimerTask
	 * @return StorageTimerTask
	 */
	public static StorageTimerTask acquireInstance() {
		
		if (s_Instance == null)
		{
			//
			// construct new storage timer task
			//
			s_Instance = new StorageTimerTask();
		}
		
		//
		// done
		//
		return s_Instance;
	}

	/**
	 * adds a call back
	 * @param callback to be added
	 */
	public void addCallback(TimerTaskCallback callback) {
		
		synchronized(this)
		{
			//
			// add call back
			//
			boolean registered = m_callbacks.add(callback);
			
			if (!registered)
				return;

			//
			// is this the first call back
			//
			if (m_callbacks.size() == 1)
			{
				m_Timer.schedule(this, new Date(), 1000);
			}
		}
	}

	/**
	 * removes a specified call back from the list of call backs
	 * @param callback to be removed
	 */
	public void removeCallback(TimerTaskCallback callback) {
		
		synchronized(this)
		{
			//
			// remove call back
			//
			m_callbacks.remove(callback);
			
			//
			// was this the last one
			//
			if (m_callbacks.size() == 0)
			{
				//
				// stop timer task
				//
				this.cancel();
			}
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void run() {

		//
		// get environment state
		//
		String state = Environment.getExternalStorageState();
		
		//
		// perform notification synchronized
		//
		synchronized(this)
		{
			//
			// iterate through all callbacks and call the appropiate callback functions
			//
			for(TimerTaskCallback callback : m_callbacks)
			{
				if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
				{
					//
					// sd card is available
					//
					callback.diskAvailable();
				}
				else
				{
					//
					// sd card is not available
					//
					callback.diskNotAvailable();
				}
			}
		}
	}

}
