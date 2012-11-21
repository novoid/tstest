package org.me.tagstore.ui;

import org.me.tagstore.R;
import org.me.tagstore.MainPagerActivity;
import org.me.tagstore.core.ConfigurationSettings;
import org.me.tagstore.core.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * helper class to add notifications to the status bar
 * 
 */
public class StatusBarNotification {

	/**
	 * notification manager
	 */
	private NotificationManager m_notification_manager;

	/**
	 * stores the context
	 */
	private Context m_context;

	/**
	 * private settings
	 */
	private SharedPreferences m_settings;

	/**
	 * constructor of class StatusBarNotification
	 * 
	 * @param context
	 *            application context
	 */
	public void initializeStatusBarNotification(Context context) {

		//
		// get notification service name
		//
		String ns_name = Context.NOTIFICATION_SERVICE;

		//
		// get notification service manager instance
		//
		m_notification_manager = (NotificationManager) context
				.getSystemService(ns_name);

		//
		// store context
		//
		m_context = context;

		//
		// get settings
		//
		m_settings = m_context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

	}

	/**
	 * returns true when notification settings are enabled
	 * 
	 * @return
	 */
	public boolean isStatusBarNotificationEnabled() {

		//
		// are notification settings enabled
		//
		return m_settings.getBoolean(
				ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS,
				ConfigurationSettings.DEFAULT_TOOLBAR_NOTIFICATION);
	}

	/**
	 * removes a status bar notification
	 */
	public void removeStatusBarNotification() {

		//
		// clear notification if it exists
		//
		m_notification_manager.cancel(ConfigurationSettings.NOTIFICATION_ID);

	}

	/**
	 * adds a notification to the status bar
	 * 
	 * @param filename
	 *            to be added
	 */
	public void addStatusBarNotification(String filename) {

		//
		// construct new notification
		//
		Notification notification = new Notification(R.drawable.tagstore,
				m_context.getString(R.string.app_name),
				System.currentTimeMillis());

		//
		// get new file localized
		//
		String new_file = m_context.getString(R.string.new_file);

		//
		// get notification description localized
		//
		String description = m_context.getString(R.string.status_bar);

		//
		// set notification title
		//
		CharSequence contentTitle = new_file;

		//
		// set notification text
		//
		CharSequence contentText = description;

		//
		// construct new intent
		//
		Intent notificationIntent = new Intent(m_context,
				MainPagerActivity.class);

		//
		// construct pending intent
		//
		PendingIntent contentIntent = PendingIntent.getActivity(m_context, 0,
				notificationIntent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		//
		// setup notification details
		//
		notification.setLatestEventInfo(m_context, contentTitle, contentText,
				contentIntent);

		//
		// pass notification to manager
		//
		m_notification_manager.notify(ConfigurationSettings.NOTIFICATION_ID,
				notification);

		//
		// done
		//
		Logger.i("Notified notification manager");
	}

	/**
	 * enables / disables notification of status bar
	 * 
	 * @param enable_notifications
	 *            if true it is enabled
	 */
	public void setStatusBarNotificationState(boolean enable_notifications) {

		//
		// get settings editor
		//
		SharedPreferences.Editor editor = m_settings.edit();

		//
		// set current file
		//
		editor.putBoolean(ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS,
				enable_notifications);

		//
		// and commit the changes
		//
		editor.commit();
	}
}
