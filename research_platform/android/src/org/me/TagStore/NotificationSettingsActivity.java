package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.Logger;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * This class is used to display notification settings
 * @author Johannes Anderwald
 *
 */
public class NotificationSettingsActivity extends Activity{

	/**
	 * stores the cancel button
	 */
	Button m_cancel_button;
	
	/**
	 * stores the accept button
	 */
	Button m_done_button;
	
	/**
	 * stores the notification button
	 */
	CheckBox m_enable_notification_button;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("NotificationSettingsActivity::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// lets sets our own design
		//
		setContentView(R.layout.notification_settings);
		
		//
		// init gui elements
		//
		m_cancel_button = (Button) findViewById(R.id.button_cancel);
		m_done_button = (Button) findViewById(R.id.button_done);
		m_enable_notification_button = (CheckBox)findViewById(R.id.button_enable_notification);
		
		if (m_done_button != null) {

			//
			// add on click listener
			//
			m_done_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//
					// call save settings method
					//
					saveSettings();
				}


			});
		}

		if (m_cancel_button != null) {

			//
			// add on click listener
			//
			m_cancel_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					//
					// cancel
					//
					ConfigurationActivityGroup.s_Instance.back();
				}
			});
		}
		
		if (m_enable_notification_button != null)
		{
			//
			// get settings
			//
			SharedPreferences settings = getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			
			//
			// are notification settings enabled
			//
			boolean enable_notifications = settings.getBoolean(ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS, true);
			
			//
			// set state
			//
			m_enable_notification_button.setChecked(enable_notifications);
		}
		
	}

	protected void saveSettings() {
		
		//
		// get settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		//
		// get settings editor
		//
		SharedPreferences.Editor editor = settings.edit();

		//
		// are notification settings enabled
		//
		boolean enable_notifications = settings.getBoolean(ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS, true);
	
		//
		// set current file
		//
		editor.putBoolean(ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS, m_enable_notification_button.isChecked());


		//
		// and commit the changes
		//
		editor.commit();
		
		if (enable_notifications && m_enable_notification_button.isChecked() == false)
		{
			//
			// get notification service name
			//
			String ns_name = Context.NOTIFICATION_SERVICE;

			//
			// get notification service manager instance
			//
			NotificationManager notification_manager = (NotificationManager) getSystemService(ns_name);

			//
			// clear notification if it exists
			//
			notification_manager.cancel(ConfigurationSettings.NOTIFICATION_ID);
		}
		
		
		//
		// hit back
		//
		ConfigurationActivityGroup.s_Instance.back();
	}
}
