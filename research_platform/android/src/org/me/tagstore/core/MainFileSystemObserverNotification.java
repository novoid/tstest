package org.me.tagstore.core;

import org.me.tagstore.R;
import org.me.tagstore.AddFileTagActivity;
import org.me.tagstore.interfaces.FileSystemObserverNotification;
import org.me.tagstore.ui.MainPageAdapter;

import android.app.Activity;

public class MainFileSystemObserverNotification implements
		FileSystemObserverNotification {

	/**
	 * application context
	 */
	private Activity m_activity;

	/**
	 * add file tab launcher
	 */
	public AddFileTabLauncher m_launcher;

	private MainPageAdapter m_adapter;

	public void initializeMainFileSystemObserverNotification(Activity activity,
			MainPageAdapter adapter) {

		//
		// init members
		//
		m_activity = activity;
		m_adapter = adapter;

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
	 * 
	 * @author Johannes Anderwald
	 * 
	 */
	private class AddFileTabLauncher implements Runnable {

		public void run() {

			if (m_adapter == null)
				m_adapter = MainPageAdapter.getInstance();

			//
			// new file arrived, is there already a file queued
			//
			if (m_adapter.isAddFileTagFragmentActive()) {
				//
				// already present
				//
				return;
			}

			//
			// get current index
			//
			int current_index = m_adapter.getCurrentItem();

			//
			// get localized new file
			//
			String new_file = m_activity.getString(R.string.new_file);

			//
			// rebuild tabs
			//
			m_adapter.addFragment(AddFileTagActivity.class.getName(), new_file);

			Logger.e("AddFileTabLauncher::current index: " + current_index);
			m_adapter.setCurrentFragmentByIndex(current_index);

		}

	}

}