package org.me.tagstore.core;

import android.os.Environment;
import android.os.Handler;

/**
 * This class is used to query the storage disk state. If the disk is available,
 * it will invoke the registered callbacks
 * 
 */
public class StorageTimerTask {

	/**
	 * This abstract class is used to invoke callbacks when disk is available /
	 * no longer available
	 * 
	 * @author Johannes Anderwald
	 * 
	 */
	public interface TimerTaskCallback {

		/**
		 * invoked when the disk is available
		 */
		public abstract boolean diskAvailable();

		/**
		 * invoked when the disk is not available
		 */
		public abstract boolean diskNotAvailable();
	}

	/**
	 * sole instance of timer task
	 */
	private static StorageTimerTask s_Instance;

	/**
	 * acquires an instance of the class StorageTimerTask
	 * 
	 * @return StorageTimerTask
	 */
	public static StorageTimerTask acquireInstance() {

		if (s_Instance == null) {
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
	 * 
	 * @param callback
	 *            to be added
	 */
	public void addCallback(TimerTaskCallback callback) {

		// construct handler
		Handler handler = new Handler();

		// construct storage handler
		StorageHandler storage_handler = new StorageHandler(handler, callback);

		// lets invoke the task
		boolean scheduled = handler.postDelayed(storage_handler,
				ConfigurationSettings.STORAGE_TASK_TIMER_DELAY);
		Logger.i("scheduled: " + scheduled);
	}

	private class StorageHandler implements Runnable {

		/**
		 * handler
		 */
		private Handler m_handler;

		/**
		 * timer callback
		 */
		private TimerTaskCallback m_callback;

		public StorageHandler(Handler handler, TimerTaskCallback callback) {
			m_handler = handler;
			m_callback = callback;
		}

		@Override
		public void run() {

			//
			// get environment state
			//
			String state = Environment.getExternalStorageState();

			//
			// is it available
			//
			boolean available = Environment.MEDIA_MOUNTED.equals(state)
					|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);

			//
			// call callback function
			//
			boolean re_schedule = false;
			if (available) {

				//
				// call callback
				//
				re_schedule = m_callback.diskAvailable();
			} else {
				re_schedule = m_callback.diskNotAvailable();
			}

			if (re_schedule) {

				//
				// reschedule task
				//
				m_handler.postDelayed(this,
						ConfigurationSettings.STORAGE_TASK_TIMER_INTERVAL);
			}
		}
	}
}
