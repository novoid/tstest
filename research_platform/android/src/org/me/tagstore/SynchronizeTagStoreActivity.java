package org.me.tagstore;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.me.tagstore.R;
import org.me.tagstore.core.ConfigurationSettings;
import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.SyncTask;
import org.me.tagstore.interfaces.SyncTaskCallback;
import org.me.tagstore.ui.ToastManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SynchronizeTagStoreActivity extends Activity implements SyncTaskCallback{

	/**
	 * stores the text view for synchronization date
	 */
	private TextView m_synch_date;
	
	/**
	 * synch button
	 */
	private Button m_synch_button;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);
		
		//
		// informal debug message
		//
		Logger.d("SynchronizeTagStoreActivity::onCreate");
		
		//
		// set layout
		//
		setContentView(R.layout.synchronize_tag_store);
		
		//
		// initialize
		//
		initialize();
	}

	public void onStop() {
		
		//
		// call base method
		//
		super.onStop();
		
		//
		// unregister us from the event dispatcher
		//
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_COMPLETE_EVENT, SynchronizeTagStoreActivity.this);
	}
	
	
	
	 private void initialize() {

		//
		// get synchronize button
		//
		m_synch_button = (Button) findViewById(R.id.button_synchronize);
		if (m_synch_button != null) {
			//
			// add click listener
			//
			m_synch_button.setOnClickListener(new OnClickListener() {


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
			String unknown = getString(R.string.unknown);
			
			//
			// get last date when synchronization was performed
			//
			String date = settings.getString(
					ConfigurationSettings.SYNCHRONIZATION_HISTORY, unknown);
			
			//
			// now update synch history
			//
			m_synch_date.setText(date);
		}
		
		//
		// register us with the event dispatcher
		//
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_COMPLETE_EVENT, SynchronizeTagStoreActivity.this);		
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
			ToastManager.getInstance().displayToastWithString(R.string.error_media_not_mounted);
			
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
		// create sync thread
		//
		Thread sync_thread = new Thread(new SyncTask(SynchronizeTagStoreActivity.this.getApplicationContext()));
		
		//
		// perform the sync
		//
		sync_thread.start();
	}


	/**
	 * updates gui after a sync operation
	 * @param performed_sync if a real sync occur. A sync occurs when a file is added / removed / tags got changed
	 */
	public void syncResultCallback(boolean performed_sync) {
		
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
			String date = date_format.format(new Date());
			
			if (m_synch_date != null)
			{
				//
				// update sync date
				//
				m_synch_date.setText(date);
			}
		}
		else
		{
			//
			// no updates found
			//
			ToastManager.getInstance().displayToastWithString(R.string.no_update);
		}
	
		//
		// re-enable sync button
		//
		m_synch_button.setEnabled(true);
		
	}
	
	public void onSyncTaskCompletion(int new_files, int old_files,
			int modified_files) {

		//
		// were there any changes
		//
		final boolean sync_performed = (new_files != 0 || old_files != 0 || modified_files != 0);
		
		//
		// run on ui thread
		//
		runOnUiThread(new Runnable() {
		    public void run() {
		    	syncResultCallback(sync_performed);
		    }
		});
	}
}
