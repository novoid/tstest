package org.me.TagStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.SyncFileLog;
import org.me.TagStore.core.SyncLogEntry;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SynchronizeTagStoreActivity extends Activity {

	
	/**
	 * stores the text view for synchronization date
	 */
	TextView m_synch_date;
	
	/**
	 * synch button
	 */
	Button m_synch_button;
	
	class ButtonTask extends TimerTask
	{
		/**
		 * stores the button
		 */
		Button m_button;
		
		/**
		 * constructor of class ButtonTask
		 * @param button holds the button
		 */
		ButtonTask(Button button)
		{
			m_button = button;
		}
		
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
			    public void run() {
					//
					// re-enable the button
					//
					m_button.setEnabled(true);
			    }
			});

		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("SynchronizeTagStoreActivity::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// lets sets our own design
		//
		setContentView(R.layout.synchronize_tag_store);
		
		
		//
		// get synchronize button
		//
		m_synch_button = (Button) findViewById(R.id.button_synchronize);
		if (m_synch_button != null) {
			//
			// add click listener
			//
			m_synch_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//
					// invoke synchronization
					//
					performSynchronization();
				}
			});
		}
		
		//
		// synchronization text view
		//
		m_synch_date = (TextView)findViewById(R.id.synchronization_history); 
		if (m_synch_date != null)
		{
			//
			// acquire shared settings
			//
			SharedPreferences settings = getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);

			//
			// get localized unknown string
			//
			String unknown = getApplicationContext().getString(R.string.unknown);
			
			//
			// get last file which was started to get tagged
			//
			String date = settings.getString(
					ConfigurationSettings.SYNCHRONIZATION_HISTORY, unknown);
			
			//
			// now update synch history
			//
			m_synch_date.setText(date);
		}
		
		
	}

	private void performSynchronization() {
		
		//
		// get current storage state
		//
		String storage_state = Environment.getExternalStorageState();
		if (!storage_state.equals(Environment.MEDIA_MOUNTED) && !storage_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
		{
			//
			// the media is currently not accessible
			//
			String media_available = getApplicationContext().getString(R.string.error_media_not_mounted);
			
			//
			// create toast
			//
			Toast toast = Toast.makeText(getApplicationContext(), media_available, Toast.LENGTH_SHORT);
			
			//
			// display toast
			//
			toast.show();
			
			//
			// done
			//
			return;
		}
		
		//
		// disable the sync button first
		//
		m_synch_button.setEnabled(false);
		
		//
		// instantiate the SyncFileLog
		//
		SyncFileLog file_log = SyncFileLog.getInstance();
		
		//
		// construct array list
		//
		ArrayList<SyncLogEntry> entries = new ArrayList<SyncLogEntry>();
		
		//
		// read all log entries
		//
		file_log.readLogEntries(entries);
		
		
		DBManager db_man = DBManager.getInstance();
		
		//
		// checks if updates were performed
		//
		boolean performed_sync = false;
		
		//
		// go through each entry
		//
		for (SyncLogEntry entry : entries)
		{
			//
			// get file name
			//
			File file = new File(entry.m_file_name);
			if (!file.exists())
			{
				//
				// skip non existing files
				//
				continue;
			}
			
			//
			// get file id
			//
			long file_id = db_man.getFileId(entry.m_file_name);
			if (file_id != -1)
			{
				//
				// file is already present
				// FIXME: should update tags???
				//
				continue;
			}
			
			//
			// updates are being performed
			//
			performed_sync = true;
			
			//
			// add file as pending
			//
			db_man.addPendingFile(entry.m_file_name);
		
			//
			// let FileTagUtility do the rest
			//
			FileTagUtility.addFile(entry.m_file_name, entry.m_tags, getApplicationContext());
		}
		
		String date;
		


		if (performed_sync)
		{
			//
			// construct date format
			//
			SimpleDateFormat date_format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			//
			// format date
			//
			date = date_format.format(new Date());
		}
		else
		{
			//
			// get localized string
			//
			String no_updates = getApplicationContext().getString(R.string.no_update);
			
			
			//
			// display toast
			//
			Toast toast = Toast.makeText(getApplicationContext(), no_updates, Toast.LENGTH_SHORT);
			toast.show();

			//
			// construct timer
			//
			Timer timer = new Timer();
			
			//
			// schedule thread to re-enable timer
			//
			timer.schedule(new ButtonTask(m_synch_button), 2500);
			
			//
			// done
			//
			return;
		}
		
		
		if (m_synch_date != null)
		{
			//
			// update sync date
			//
			m_synch_date.setText(date);
		}
		
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
		// store synchronization date
		//
		editor.putString(ConfigurationSettings.SYNCHRONIZATION_HISTORY, date);
			
			
		//
		// and commit the changes
		//
		editor.commit();
	
		//
		// re-enable sync button
		//
		m_synch_button.setEnabled(true);
		
	}

}
