package org.me.TagStore.core;

import org.me.TagStore.AddFileTagActivity;
import org.me.TagStore.R;
import org.me.TagStore.interfaces.FileSystemObserverNotification;
import org.me.TagStore.ui.MainPageAdapter;

import android.app.Activity;

public class MainFileSystemObserverNotification implements
		FileSystemObserverNotification {

	/**
	 * application context
	 */
	private final Activity m_activity;
	
	/**
	 * add file tab launcher
	 */
	private final AddFileTabLauncher m_launcher;
	
	
	/**
	 * constructor of class MainFileSystemObserverNotification
	 * @param context application context
	 */
	public MainFileSystemObserverNotification(Activity activity) {
		
		//
		// store context
		//
		m_activity = activity;
		
		//
		// construct laucher thread
		//
		m_launcher = new AddFileTabLauncher();
	}

	
	public void notify(String file_name, NotificationType type) {

		//
		// check notification type
		//
		if (type == NotificationType.FILE_CREATED) {

			//
			// add the file tab by running it on the gui thread
			//
			m_activity.runOnUiThread(m_launcher);
		}
	}

	/**
	 * Helper class which adds the add file tab
	 * @author Johannes Anderwald
	 *
	 */
	private class AddFileTabLauncher implements Runnable {

		
		
		
		public void run() {
			
			//
			// new file arrived, is there already a file queued
			//
			MainPageAdapter adapter = MainPageAdapter.getInstance();
			if (adapter.isAddFileTagFragmentActive())
			{
				//
				// already present
				//
				return;
			}

			//
			// get current index
			//
			int current_index = adapter.getCurrentItem();
			
			
			//
			// get localized new file
			//
			String new_file = m_activity.getString(
					R.string.new_file);
			
			//
			// rebuild tabs
			//
			adapter.addFragment(AddFileTagActivity.class.getName(),
					new_file);					

			
			Logger.e("AddFileTabLauncher::current index: " + current_index);
			adapter.setCurrentFragmentByIndex(current_index);
			
		}
		
		
		
	}
	
	
}