package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.Logger;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
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

		if (m_enable_notification_button != null) {
			//
			// get settings
			//
			SharedPreferences settings = getActivity().getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);

			//
			// are notification settings enabled
			//
			boolean enable_notifications = settings.getBoolean(
					ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS, true);

			//
			// set state
			//
			m_enable_notification_button.setChecked(enable_notifications);
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
		// get settings
		//
		SharedPreferences settings = getActivity().getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		//
		// get settings editor
		//
		SharedPreferences.Editor editor = settings.edit();

		//
		// are notification settings enabled
		//
		boolean enable_notifications = settings.getBoolean(
				ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS, true);

		//
		// set current file
		//
		editor.putBoolean(ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS,
				m_enable_notification_button.isChecked());

		//
		// and commit the changes
		//
		editor.commit();

		if (enable_notifications
				&& m_enable_notification_button.isChecked() == false) {
			//
			// get notification service name
			//
			String ns_name = Context.NOTIFICATION_SERVICE;

			//
			// get notification service manager instance
			//
			NotificationManager notification_manager = (NotificationManager) getActivity()
					.getSystemService(ns_name);

			//
			// clear notification if it exists
			//
			notification_manager.cancel(ConfigurationSettings.NOTIFICATION_ID);
		}

	}
}
