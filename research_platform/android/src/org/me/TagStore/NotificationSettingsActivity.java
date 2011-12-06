package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.Logger;
import org.me.TagStore.ui.StatusBarNotification;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

/**
 * This class is used to display notification settings and let the user edit them
 * 
 * @author Johannes Anderwald
 * 
 */
public class NotificationSettingsActivity extends Fragment {

	/**
	 * stores the notification button
	 */
	private CheckBox m_enable_notification_button;

	/**
	 * reference to status bar helper
	 */
	private StatusBarNotification m_status_bar;
	
	/**
	 * initial status bar setting
	 */
	private boolean m_init_status_bar_enabled;
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.d("NotificationSettingsActivity::onCreate");
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle saved) {

		//
		// construct layout
		//
		View view = inflater.inflate(R.layout.notification_settings, null);

		//
		// init gui elements
		//
		m_enable_notification_button = (CheckBox) view
				.findViewById(R.id.button_enable_notification);

		//
		// build status bar notification
		//
		m_status_bar = new StatusBarNotification(getActivity().getApplicationContext());
		
		//
		// get current settings
		//
		m_init_status_bar_enabled = m_status_bar.isStatusBarNotificationEnabled();
		
		//
		// set button
		//
		if (m_enable_notification_button != null) {

			//
			// set state
			//
			m_enable_notification_button.setChecked(m_init_status_bar_enabled);
		}

		//
		// done
		//
		return view;
	}

	public void onPause() {

		//
		// call super method
		//
		super.onPause();
		
		Logger.i("NotificationSetting::onPause");

		//
		// default is checked
		//
		boolean current_status_bar_enabled = true;
		
		//
		// get current setting
		//
		if (m_enable_notification_button != null)
		{
			//
			// is it checked
			//
			current_status_bar_enabled = m_enable_notification_button.isChecked();
		}
		
		//
		// update status bar notification settings
		//
		m_status_bar.setStatusBarNotificationState(current_status_bar_enabled);
		
		//
		// was it activated before and now deactivated
		//
		if (m_init_status_bar_enabled && current_status_bar_enabled == false) {
			
			//
			// cancel notification if it exists
			//
			m_status_bar.removeStatusBarNotification();
		}

	}
}
