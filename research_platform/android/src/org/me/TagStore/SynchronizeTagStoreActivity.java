package org.me.TagStore;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.SyncFileWriter;
import org.me.TagStore.core.SyncManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SynchronizeTagStoreActivity extends Fragment {

	
	/**
	 * stores the text view for synchronization date
	 */
	private TextView m_synch_date;
	
	/**
	 * synch button
	 */
	private Button m_synch_button;
	
	/**
	 * stores if an sync was performed
	 */
	private boolean m_sync_performed = false;
	
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
	}
	
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {

		 //
		 // construct layout
		 //
		 View view = inflater.inflate(R.layout.synchronize_tag_store, null);

		//
		// get synchronize button
		//
		m_synch_button = (Button) view.findViewById(R.id.button_synchronize);
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
		m_synch_date = (TextView)view.findViewById(R.id.synchronization_history); 
		if (m_synch_date != null)
		{
			//
			// acquire shared settings
			//
			SharedPreferences settings = getActivity().getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);

			//
			// get localized unknown string
			//
			String unknown = getActivity().getString(R.string.unknown);
			
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
		// done
		//
		return view;
		
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
			String media_available = getActivity().getString(R.string.error_media_not_mounted);
			
			//
			// create toast
			//
			Toast toast = Toast.makeText(getActivity(), media_available, Toast.LENGTH_SHORT);
			
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
		// create sync thread
		//
		Thread sync_thread = new Thread(new SyncTask());
		
		//
		// perform the sync
		//
		sync_thread.start();
	}

	/**
	 * callback invoked by synchronization thread when the sync has been completed
	 * @param performed_sync if the sync has been made
	 */	
	public void syncCallback(boolean performed_sync) {
		
		//
		// store result
		//
		m_sync_performed = performed_sync;
		
		
		//
		// run on ui thread
		//
		getActivity().runOnUiThread(new Runnable() {
		    public void run() {
		    	syncResultCallback(m_sync_performed);
		    }
		});
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
			// get localized string
			//
			String no_updates = getActivity().getString(R.string.no_update);

			
			//
			// display toast
			//
			Toast toast = Toast.makeText(getActivity(), no_updates, Toast.LENGTH_SHORT);
			toast.show();
		}
	
		//
		// re-enable sync button
		//
		m_synch_button.setEnabled(true);
		
	}
	
	/**
	 * This class executes the synchronization task
	 * @author Johnseyii
	 *
	 */
	private class SyncTask implements Runnable {
		
		@Override
		public void run() {

			//
			// instantiate the sync manager
			//
			SyncManager sync_mgr = new SyncManager(getActivity());
			
			int new_files = sync_mgr.getNumberOfNewFiles();
			int removed_files = sync_mgr.getNumberOfRemovedFiles();
			int changed_files = sync_mgr.getNumberOfChangedFiles();
			
			
			
			Logger.i("New     Files: " + new_files);
			Logger.i("Removed Files: " + removed_files);
			Logger.i("Changed files: " + changed_files);
			//
			// now sync the files
			//
			sync_mgr.syncRemovedFiles();
			sync_mgr.syncNewFiles();
			sync_mgr.syncChangedFiles();	
			
			//
			// did any updates happen?
			//
			boolean performed_sync = (new_files != 0) || (removed_files != 0) || (changed_files != 0);

			if (performed_sync)
			{
				//
				// instantiate the sync file write
				//
				SyncFileWriter file_writer = new SyncFileWriter();
				
				//
				// write entries
				//
				file_writer.writeTagstoreFiles();	
			}
			
			//
			// lets update the gui
			//
			syncCallback(performed_sync);
			
		}
		
	}


	
	
}
