package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.Logger;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DatabaseSettingsActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.i("DatabaseSettingsActivity::onCreate");

		//
		// lets sets our own design
		//
		setContentView(R.layout.database_settings);

		//
		// initialize configuration tab
		//
		initialize();
	}

	/**
	 * initialize user interface
	 */
	private void initialize() {

		//
		// get back button
		//
		Button back_button = (Button) findViewById(R.id.button_back);
		if (back_button != null) {
			//
			// add click listener
			//
			back_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//
					// notify that we are done
					//
					ConfigurationActivityGroup.s_Instance.back();
				}
			});
		}

		//
		// get reset button
		//
		Button reset_button = (Button) findViewById(R.id.button_reset);
		if (reset_button != null) {

			//
			// add click listener
			//
			reset_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//
					// call reset database method
					//
					resetDatabase();
				}
			});
		}

	}

	/**
	 * resets the database
	 */
	protected void resetDatabase() {

		//
		// acquire database manager instance
		//
		DBManager db_man = DBManager.getInstance();

		//
		// reset the database
		//
		boolean reset_database = db_man.resetDatabase(getApplicationContext());

		Toast toast;
		
		if (reset_database) {
			//
			// successfully deleted database
			//
			String text = getApplicationContext().getString(R.string.reset_database);
			toast = Toast.makeText(getApplicationContext(),
					text, Toast.LENGTH_SHORT);
		} else {
			//
			// failed to reset database
			//
			String text = getApplicationContext().getString(R.string.error_reset_database);			
			toast = Toast.makeText(getApplicationContext(),
					text, Toast.LENGTH_SHORT);
		}

		//
		// get localized new file
		//
		String new_file = getApplicationContext().getString(R.string.new_file);
		
		//
		// is the new file tab being shown
		//
		if (MainActivity.s_Instance.isTabVisible(new_file))
		{
			//
			// the database was cleaned, there are no more pending files
			//
			MainActivity.s_Instance.showTab(new_file, false);
		

			//
			// clear file settings
			//
			clearCurrentFile();
			
			//
			// clear notification settings
			//
			clearNotification();
		}
		
		//
		// display toast
		//
		toast.show();
	}

	/**
	 * clears any active status bar notification when enabled
	 */
	private void clearNotification()
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
		// check notification settings are enabled
		//
		if (!enable_notifications)
			return;
		
		//
		// get notification service name
		//
		String ns_name = Context.NOTIFICATION_SERVICE;

		//
		// get notification service manager instance
		//
		NotificationManager notification_manager = (NotificationManager) getSystemService(ns_name);

		//
		// clear notification
		//
		notification_manager.cancel(ConfigurationSettings.NOTIFICATION_ID);
	}
	
	/**
	 * clears the current file setting, which is used to determine which file was last tagged / started to be tagged
	 */
	private void clearCurrentFile() {
		
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
		// set current file
		//
		editor.putString(ConfigurationSettings.CURRENT_FILE_TO_TAG, "");

		//
		// set current tag line
		//
		editor.putString(ConfigurationSettings.CURRENT_TAG_LINE, "");
		
		//
		// and commit the changes
		//
		editor.commit();
	}
	
}
