package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DatabaseResetTask;
import org.me.TagStore.core.EventDispatcher;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.MainServiceConnection;
import org.me.TagStore.core.ServiceLaunchRunnable;
import org.me.TagStore.core.VocabularyManager;
import org.me.TagStore.interfaces.DatabaseResetCallback;
import org.me.TagStore.ui.MainPageAdapter;
import org.me.TagStore.ui.StatusBarNotification;
import org.me.TagStore.ui.ToastManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Window;

/**
 * This class displays various configuration settings and launches the
 * associated dialogs
 * 
 * @author Johannes Anderwald
 * 
 */
public class ConfigurationTabActivity extends PreferenceActivity implements OnPreferenceClickListener, DatabaseResetCallback, OnPreferenceChangeListener {

	
	private static final int RESET_DIALOG_ID = 10;
	
	/**
	 * stores connection to the service
	 */
	private MainServiceConnection m_connection = null;

	/**
	 * stores handle to the status bar manager
	 */
	private StatusBarNotification m_status_bar = null;

	public void onStop() {
		
		//
		// call base class
		//
		super.onStop();
		
		//
		// unregister us from the event dispatcher
		//
		//
		// register us with event dispatcher
		//
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.DATABASE_RESET_EVENT, this);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("ConfigurationTab::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// load preferences from resource
		//
		addPreferencesFromResource(R.xml.preferences);

		//
		// initialize configuration tab
		//
		initialize();
	}
	
	protected Dialog onCreateDialog(int id) {
		
		if (id == ConfigurationTabActivity.RESET_DIALOG_ID)
		{
			//
			// construct new alert builder
			//
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this);
			builder.setTitle(R.string.reset_factory_settings);
			builder.setMessage(R.string.database_reset_question);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					//
					// create database reset worker
					//
					Thread database_worker = new Thread(new DatabaseResetTask(ConfigurationTabActivity.this.getApplicationContext(), m_connection));
				
					//
					// start the thread
					//
					database_worker.start();                
            	}
			});
		
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					}
			});
		
			//
			// construct dialog
			//
			return builder.create();
		}
		
		//
		// unknown dialog requested
		//
		return null;
	}
	
	
	public void onResume() {
		
		//
		// resume
		//
		super.onResume();
		
		Logger.i("ConfigurationTabActivity::onResume");
	}
	
	public void onPause() {
		
		super.onPause();
		
		Logger.i("ConfigurationTabActivity::onPause");
	}
	
	private void addClickListener(String preference_name, boolean click_listener, boolean change_listener) {
		
		//
		// find preference name
		//
		Preference preference = findPreference(preference_name);
		if (preference == null)
		{
			Logger.e("Error: preference " + preference_name + " not found");
			return;
		}
		
		if (click_listener)
		{
			//
			// add click listener
			//
			preference.setOnPreferenceClickListener(this);

		}
		
		if (change_listener)
		{
			//
			// add change listener
			//
			preference.setOnPreferenceChangeListener(this);
		}
		
	}
	
	
	
	private void initialize() {

		//
		// add click listeners
		//
		addClickListener(ConfigurationSettings.DIRECTORY_PREFERENCE, true, false);
		addClickListener(ConfigurationSettings.DISPLAY_PREFERENCE, false, true);
		addClickListener(ConfigurationSettings.NOTIFICATION_PREFERENCE, false, true);
		addClickListener(ConfigurationSettings.VOCABULARY_PREFERENCE, false, true);
		addClickListener(ConfigurationSettings.DATABASE_PREFERENCE, true, false);
		addClickListener(ConfigurationSettings.ICON_VIEW_ITEM_ROW_PREFERENCE, false, true);
		addClickListener(ConfigurationSettings.ICON_VIEW_SORT_MODE_PREFERENCE, false, true);
		
		//
		// build main service connection
		//
		m_connection  = new MainServiceConnection();
		
		//
		// construct service launcher
		//
		ServiceLaunchRunnable launcher = new ServiceLaunchRunnable(getApplicationContext(), m_connection);
		
		//
		// create a thread which will start the service
		//
		Thread launcher_thread = new Thread(launcher);
		
		//
		// now start the thread
		//
		launcher_thread.start();		
		
		//
		// build status bar
		//
		m_status_bar  = new StatusBarNotification(getApplicationContext());		
		
		//
		// acquire shared settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get current list view
		//
		String current_view_class = settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_CLASS,
				ConfigurationSettings.DEFAULT_LIST_VIEW_CLASS);

		//
		// enables / disables options
		//
		toggleDisplayPreference(current_view_class);
		
		//
		// register us with event dispatcher
		//
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.DATABASE_RESET_EVENT, this);
		
	}


	@Override
	public boolean onPreferenceClick(Preference preference) {

		Logger.e("pref: clicked on " + preference.getKey());
		
		final String key = preference.getKey();
		
		if (key.equals(ConfigurationSettings.DIRECTORY_PREFERENCE))
		{
			//
			// launch directory activity
			//
			Intent intent = new Intent(ConfigurationTabActivity.this, DirectoryListActivity.class);
			super.startActivity(intent);
			return true;
		}
		else if (key.equals(ConfigurationSettings.DATABASE_PREFERENCE))
		{
			//
			// launch alert dialog for reset
			//
			showDialog(RESET_DIALOG_ID);
			return true;
		}
		return false;		
	}
	
	/**
	 * updates a preference enabled state
	 * @param preference_name preference name to be updated
	 * @param enabled if it should be enabled
	 */
	private void togglePreferenceState(String preference_name, boolean enabled) {
		
		Preference pref = findPreference(preference_name);
		if (pref != null)
		{
			pref.setEnabled(enabled);
		}
	}
	
	
	private void toggleDisplayPreference(String value) {
		
		//
		// check if icon view is selected
		//
		boolean icon_view = value.equals(ConfigurationSettings.ICON_LIST_VIEW_CLASS);
		
		//
		// enable/disable state
		//
		togglePreferenceState(ConfigurationSettings.ICON_VIEW_ITEM_ROW_PREFERENCE, icon_view);
		togglePreferenceState(ConfigurationSettings.ICON_VIEW_SORT_MODE_PREFERENCE, icon_view);
	}
	

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		//
		// get preference key
		//
		String key = preference.getKey();
		if (key.equals(ConfigurationSettings.DISPLAY_PREFERENCE))
		{
			//
			// get selected item
			//
			String default_display_class = (String)newValue;
			
			//
			// update view
			//
			toggleDisplayPreference(default_display_class);
			Logger.e("DisplayClass: " + default_display_class);
			//
			// save display preferences
			//
			saveDisplayPreferences(default_display_class, null, null);
			return true;
		}
		else if(key.equals(ConfigurationSettings.ICON_VIEW_ITEM_ROW_PREFERENCE))
		{
			//
			// save display preferences
			//
			saveDisplayPreferences(null, (String)newValue, null);
			return true;
		}
		else if (key.equals(ConfigurationSettings.ICON_VIEW_SORT_MODE_PREFERENCE))
		{
			//
			// save display preferences
			//
			saveDisplayPreferences(null, null, (String)newValue);
			return true;
		}
		else if (key.equals(ConfigurationSettings.NOTIFICATION_PREFERENCE))
		{
			//
			// get check box preference
			//
			Boolean value = (Boolean)newValue;
			//
			// update status bar notification settings
			//
			Logger.e("key: " + key + " value: " + value);
			m_status_bar.setStatusBarNotificationState(value);
			if (!m_status_bar.isStatusBarNotificationEnabled())
			{
				//
				// cancel notifications if shown
				//
				m_status_bar.removeStatusBarNotification();
			}
			return true;
		}
		else if (key.equals(ConfigurationSettings.VOCABULARY_PREFERENCE))
		{
			//
			// get new value
			//
			Boolean value = (Boolean)newValue;
			
			//
			// get instance of vocabulary manager
			//
			VocabularyManager voc_manager = VocabularyManager.getInstance();
			
			if (value.booleanValue())
			{
				//
				// verify that it exists
				//
				if (!voc_manager.doesVocabularyFileExist())
				{
					//
					// display warning toast
					//
					ToastManager.getInstance().displayToastWithString(R.string.error_vocabulary_file_not_exists);
					return false;
				}
			}
			
			//
			// set new state
			//
			voc_manager.setControlledVocabularyState(value.booleanValue());
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * saves the display settings
	 * @param default_display_class default display class
	 * @param items_per_row items per row icon view
	 * @param sort_mode sort mode for icon view
	 */
	private void saveDisplayPreferences(String default_display_class, String items_per_row, String sort_mode) {
		
		//
		// acquire shared settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get editor
		//
		SharedPreferences.Editor editor = settings.edit();

		if (items_per_row != null)
		{
			try
			{
				//
				// convert to integer
				//
				int items_row = Integer.parseInt(items_per_row);
				
				//
				// store number of rows
				//
				editor.putInt(ConfigurationSettings.NUMBER_OF_ITEMS_PER_ROW, items_row);
				
			}catch(NumberFormatException exc)
			{
				Logger.e("NumberFromatException while parsing: items_per_row:" + items_per_row);
			}

		}
		if (default_display_class != null)
		{
			//
			// set list view mode
			//
			editor.putString(ConfigurationSettings.CURRENT_LIST_VIEW_CLASS, default_display_class);
		}
		
		if (sort_mode != null)
		{
			//
			// store sort mode
			//
			editor.putString(ConfigurationSettings.CURRENT_LIST_VIEW_SORT_MODE, sort_mode);
		}
		
		//
		// now commit changes
		//
		editor.commit();
	}
	
	
	/**
	 * updates the ui after the database has been reset
	 * @param reset_database if true the database has successfully been reset
	 */
	protected void updateUIDatabaseCallback(boolean reset_database) {

		//
		// get main page adapter
		//
		MainPageAdapter adapter = MainPageAdapter.getInstance();

		//
		// is the add file tag fragment present
		//
		if (adapter.isAddFileTagFragmentActive())
		{
			//
			// hide that fragment as the database is now empty
			//
			adapter.removeAddFileTagFragment();
				
			//
			// clear notification settings
			//
			clearNotification();
		}


		if (reset_database) {
			//
			// successfully deleted database
			//
			ToastManager.getInstance().displayToastWithString(R.string.reset_database);
		} else {
			//
			// failed to reset database
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_reset_database);			
		}
	}

	/**
	 * clears any active status bar notification when enabled
	 */
	private void clearNotification()
	{
		//
		// are notification settings enabled
		//
		boolean enabled_status_bar = m_status_bar.isStatusBarNotificationEnabled();
		if(enabled_status_bar)
		{
			//
			// cancel notification
			//
			m_status_bar.removeStatusBarNotification();
		}
	}
	
	
	/**
	 * callback which is invoked when database worker has been reset
	 * @param reset_database if true the database has been reset
	 */
	public void onDatabaseResetResult(final boolean success) {
		//
		// run on ui thread
		//
		runOnUiThread(new Runnable() {
		    public void run() {
		    	updateUIDatabaseCallback(success);
		    }
		});
		
	}

	
	
}
