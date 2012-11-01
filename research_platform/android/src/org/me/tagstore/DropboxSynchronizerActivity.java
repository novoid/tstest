package org.me.tagstore;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.me.tagstore.R;
import org.me.tagstore.core.ConfigurationSettings;
import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.StorageProviderFactory;
import org.me.tagstore.core.SynchronizationAlgorithmBackend;
import org.me.tagstore.core.SynchronizationAlgorithmBackend.ConflictData;
import org.me.tagstore.interfaces.StorageProvider;
import org.me.tagstore.interfaces.SyncTaskCallback;
import org.me.tagstore.interfaces.SynchronizationAlgorithmCallback;
import org.me.tagstore.ui.ToastManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class DropboxSynchronizerActivity extends Activity implements SynchronizationAlgorithmCallback,  SyncTaskCallback{

	/**
	 * dropbox storage provider
	 */
	private StorageProvider m_provider;
	
	/**
	 * synchronization algorithm backend
	 */
	private SynchronizationAlgorithmBackend m_backend;
	
	/**
	 * target store name
	 */
	private String m_target_store;
	
	/**
	 * synchronization button
	 */
	private Button m_synchronize_button;
	
	/**
	 * progress bar
	 */
	private ProgressBar m_progress_bar;
	
	
	/**
	 * status notification colors
	 */
	static final private String STATUS_NOTIFICATION_COLOR = "#5599FF";
	static final private String STATUS_ERROR_COLOR ="#FF0000";
	
	
	private int m_max_progress = 0;
	private int m_current_progress = 0;
	
	/**
	 * status text field
	 */
	private TextView m_status_text;

	private ConflictData m_conflict;
	
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);
		
		//
		// informal debug message
		//
		Logger.d("DropboxSynchronizerActivity::onCreate");
		
		//
		// set layout
		//
		setContentView(R.layout.dropbox_synchronizer);
		
		// register events
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_COMPLETE_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_INFO_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_UPLOAD_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_FILE_INFO_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_CONFLICT_EVENT, DropboxSynchronizerActivity.this);
		
		//
		// initialize
		//
		initializeUI();
	}
	
	
	/*
	private boolean test() {
		
		NFSStorageProvider provider = (NFSStorageProvider)m_provider;
		
		// authenticate
		try {
			
			Logger.e("logging in");
			boolean login = provider.login("Johnseyii", "1234", "192.168.2.2");
			Logger.e("logged in: " + login);
			
			if (!login)
				return false;
			
			ArrayList<String> files = provider.getFiles("/", null);
			for(String file : files)
				Logger.e(file);
			
			Logger.e("done");
			
			// acquire shared settings
			SharedPreferences settings = getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			
			SharedPreferences.Editor editor = settings.edit();
			
			editor.putString(ConfigurationSettings.CURRENT_SYNCHRONIZATION_TAGSTORE, "/tagstore/store1");
			editor.commit();
			return true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	*/
	/**
	 * initializes the backend
	 * @return true on success
	 */
	private boolean initializeBackend() {
	
		// construct storage provider factory
		StorageProviderFactory provider_factory = new StorageProviderFactory(DropboxSynchronizerActivity.this);
		
		// construct storage provider
		m_provider = provider_factory.getDefaultStorageProvider();

		/*
		if (!test())
			return false;
		*/
		
		// construct synchronization backend
		m_backend = new SynchronizationAlgorithmBackend(m_provider, DropboxSynchronizerActivity.this);
		
		if (!m_provider.isLoggedIn())
		{
			// not logged in
			ToastManager.getInstance().displayToastWithString("Need to authenticate first");
			//Intent intent = new Intent(DropboxSynchronizerActivity.this, DropboxSettingsActivity.class);
			//super.startActivity(intent);
			return false;
		}
		
		// acquire shared settings
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		
		// read target tagstore
		String target_store = settings.getString(ConfigurationSettings.CURRENT_SYNCHRONIZATION_TAGSTORE, "");	
		if (target_store.length() == 0) {
			
			// no tagstore yet selected
			ToastManager.getInstance().displayToastWithString("No tagstore yet selected");
			//Intent intent = new Intent(DropboxSynchronizerActivity.this, DropboxSettingsActivity.class);
			//super.startActivity(intent);
			return false;
		}
		
		// succeeded
		return true;
	}
	
	/**
	 * initializes the user interface
	 */
	private void initializeUI() {
		
		// acquire shared settings
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		
		// read target tagstore
		m_target_store = settings.getString(ConfigurationSettings.CURRENT_SYNCHRONIZATION_TAGSTORE, "");
		if (m_target_store.length() != 0)
		{
			// set target store name
			TextView text = (TextView)findViewById(R.id.target_tagstore_label);
			if (text != null) {
				text.setText(m_target_store.substring(1));				
			}
		}
		
		// init synchronize button
		m_synchronize_button = (Button)findViewById(R.id.button_synchronize);
		if (m_synchronize_button != null) {
			m_synchronize_button.setOnClickListener(new OnClickListener() {

				
				public void onClick(View v) {
					performSynchronization();
				}
				
			});
		}
		
		// init status field
		m_status_text = (TextView)findViewById(R.id.status_text);
		
		// init progress bar
		m_progress_bar = (ProgressBar)findViewById(R.id.pb);
		
	}
	
	private void performSynchronization() {
		
		// init backend
		if (!initializeBackend())
			return;
		
		// disable synchronize button
		m_synchronize_button.setEnabled(false);

		// reset progress bar
		m_progress_bar.setProgress(0);
		m_max_progress = 0;
		m_current_progress = 0;
		m_progress_bar.setMax(100);		
		
		
		// construct sync thread
		Thread sync_thread = new Thread(new Runnable() {

			public void run() {
				m_backend.synchronize();
			}
			
		});

		// execute sync thread
		sync_thread.start();
	}
	
	
	public void onResume() {
		
		//
		// resume base class
		//
		super.onResume();
		
		// register events
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_COMPLETE_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_INFO_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_UPLOAD_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_FILE_INFO_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().registerEvent(EventDispatcher.EventId.SYNC_CONFLICT_EVENT, DropboxSynchronizerActivity.this);		
		
	}

	public void onPause() {
		
		super.onPause();
		
		// unregister events
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_COMPLETE_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_INFO_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_UPLOAD_EVENT, DropboxSynchronizerActivity.this);
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_FILE_INFO_EVENT, DropboxSynchronizerActivity.this);	
		EventDispatcher.getInstance().unregisterEvent(EventDispatcher.EventId.SYNC_CONFLICT_EVENT, DropboxSynchronizerActivity.this);		
	}
	
	
	public void onUploadFile(String file_name) {

		m_current_progress++;
		String msg = getString(R.string.uploading) + " " + file_name;
		setStatusText(msg, STATUS_NOTIFICATION_COLOR);
		
	}

	public void onDownloadFile(String file_name) {

		m_current_progress++;		
		String msg = getString(R.string.downloading) + " " + file_name;
		setStatusText(msg, STATUS_NOTIFICATION_COLOR);

	}

	public void notifyInfo(String msg) {

		setStatusText(msg, STATUS_NOTIFICATION_COLOR);
	}

	public void notifyError(String msg) {

		setStatusText(msg, STATUS_ERROR_COLOR);
	}
	
	
	private void updateProgressBar() {
		
		if (m_current_progress > 0 && m_max_progress > 0)
		{
			int progress = (m_current_progress * 100) / m_max_progress;
			m_progress_bar.setProgress(progress);
		}
	}
	
	/**
	 * sets the status text and color
	 * @param msg status text
	 * @param colorString color
	 */
	private void setStatusText(final String msg, final String colorString)
	{
		
		runOnUiThread(new Runnable() {

			public void run() {

				// log it
				Logger.i(msg);
				
				// display it
				if (m_status_text != null)
				{
					m_status_text.setText(msg);
					m_status_text.setTextColor(Color.parseColor(colorString));
				}
				
				// set progress
				updateProgressBar();
				
			}
			
		});
	}

	public void onNotifyConflict() {
		
		runOnUiThread(new Runnable() {

			public void run() {
			
				if(m_backend.hasConflicts()) {
					
					// fetch conflict
					m_conflict = m_backend.nextConflict();
					showConflictDialog(m_conflict);
				}
				else
				{
					// finish synchronization
					m_backend.finishSynchronization();
				}
				
				
			}
		});		
	};
	
	private void showConflictDialog(ConflictData data) {
		
		// construct alert dialog
		AlertDialog alert_dialog = new AlertDialog.Builder(DropboxSynchronizerActivity.this).create();
			
		// set title
		String title_msg = data.m_type.name();
		alert_dialog.setTitle(title_msg);
		
		// get message format message
		String dialog_msg_format = getString(R.string.conflict_dialog_msg_format);
		
		// format result
		String dialog_msg = String.format(dialog_msg_format, data.m_source_file, data.m_source_store, data.m_target_file, data.m_target_store);
		alert_dialog.setMessage(dialog_msg);
	
		// set button notifier
		alert_dialog.setButton("Yes", new DialogInterface.OnClickListener() {
			
			
			public void onClick(DialogInterface dialog, int which) {

				// handle conflict
				m_backend.handleConflict(m_conflict);
				m_conflict = null;
			}
		});
		
		alert_dialog.setButton2("No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				if(m_backend.hasConflicts()) {
					
					// fetch conflict
					m_conflict = m_backend.nextConflict();
					showConflictDialog(m_conflict);
				}
				else
				{
					// finish synchronization
					m_backend.finishSynchronization();
				}
				
			}
		});
		
		// set warning dialog
		alert_dialog.setIcon(R.drawable.remove_button);
		
		// not cancellable
		alert_dialog.setCancelable(false);
		
		// show the dialog
		alert_dialog.show();
	}
	
	
	public void onSyncTaskCompletion(int new_files, int old_files,
			int modified_files) {

		runOnUiThread(new Runnable() {

			public void run() {

		
				// get translated string
				String msg = getString(R.string.sync_completed);
		
				SimpleDateFormat date_format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

				// get date
				String date = date_format.format(new Date());
		
				// append date
				msg += date;
		
				// set final status
				setStatusText(msg, STATUS_NOTIFICATION_COLOR);
				
				// set 100 %
				m_progress_bar.setProgress(100);
				
				// enable button
				m_synchronize_button.setEnabled(true);
			}
		});
	}

	public void notifyFileSyncInfo(final int source_files, final int target_files) {
		runOnUiThread(new Runnable() {

			public void run() {

				// source store files + target store files + 2 * (target store file + target sync store file)
				m_max_progress = source_files + target_files + 4;
				m_current_progress = 2;
				Logger.e("notifyFileSyncInfo");
				updateProgressBar();
				}
		});
	}
	
	
}
