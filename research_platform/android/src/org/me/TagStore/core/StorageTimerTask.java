package org.me.TagStore.core;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	private final Timer m_Timer;
	
	/**
	 * holds a list of call backs
	 */
	private final HashSet<TimerTaskCallback> m_callbacks;
	
	
	/**	
	 * sole instance of timer task
	 */
	private static StorageTimerTask s_Instance;
	
	/**
	 * lock protecting callbacks array
	 */
	private final ReentrantReadWriteLock m_lock;
	
	
	/**
	 * setting if the timer was already started
	 */
	private boolean m_timer_scheduled;
	
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
		
		//
		// construct lock
		//
		m_lock = new ReentrantReadWriteLock();
		
		//
		// timer not scheduled
		//
		m_timer_scheduled = false;
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
		
		//
		// lock
		//
		m_lock.writeLock().lock();
		
		//
		// add call back
		//
		boolean registered = m_callbacks.add(callback);
			
		if (!registered)
		{
			m_lock.writeLock().unlock();
			return;
		}
		
		//
		// is this the first call back
		//
		if (!m_timer_scheduled)
		{
			m_Timer.scheduleAtFixedRate(this, 0, 1000);
			m_timer_scheduled = true;
		}
			
		//	
		// unlock
		//
		m_lock.writeLock().unlock();
	}

	/**
	 * removes a specified call back from the list of call backs
	 * @param callback to be removed
	 */
	public void removeCallback(TimerTaskCallback callback) {
		
		//
		// lock
		//
		m_lock.writeLock().lock();

		//
		// remove call back
		//
		m_callbacks.remove(callback);
			
		//	
		// unlock
		//
		m_lock.writeLock().unlock();		
		
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
		// is it available
		//
		boolean available = Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
		
		//
		// acquire read lock
		//
		m_lock.readLock().lock();
		
		//
		// create temporary list
		//
		TimerTaskCallback[] callbacks = m_callbacks.toArray(new TimerTaskCallback[1]);
		
		//
		// release read lock
		//
		m_lock.readLock().unlock();	
		
		//
		// call appropiate callback function
		//
		for(TimerTaskCallback callback : callbacks)
		{
			if (available)
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
