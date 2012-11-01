package org.me.TagStore;

import java.util.ArrayList;

import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.DropboxStorageProvider;
import org.me.TagStore.core.Logger;
import org.me.TagStore.ui.ToastManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This class implements Dropbox specific settings, such as authentication, logging out, and
 * selecting a tagstore
 * @author Johannes Anderwald
 */
public class DropboxSettingsActivity extends Activity {

	/**
	 * Dropbox api reference
	 */
	private DropboxStorageProvider m_Provider;
	
	/**
	 * authentication button
	 */
	private Button m_button_authenticate;
	
	/**
	 * unlink button
	 */
	private Button m_button_unlink;
	
	/**
	 * select tagstore button
	 */
	private Button m_button_select;
	
	/* dialog id */
	static final private int SELECT_DIALOG_ID = 1;
	
	/**
	 * tagstore list
	 */
	private ArrayList<CharSequence> m_tagstore_list;
	
	private static final String STORE_TGS_PATH ="/.tagstore/store.tgs";
	
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
		setContentView(R.layout.dropbox_settings);
		
		//
		// initialize
		//
		initialize();
	}
	
	public void onResume() {
		
		//
		// resume base class
		//
		super.onResume();
		
		//
		// finish authentication
		//
		if (m_Provider != null)
		{
			// finish authentication
			m_Provider.finishAuthentication(DropboxSettingsActivity.this);			
		}

		// toggle button states
		toggleButtonState();
	}
			
	
	private void initialize() {
		
		// initialize authenticate button
		m_button_authenticate = (Button)findViewById(R.id.button_authenticate);
		if (m_button_authenticate != null) {

			// add click listener
			m_button_authenticate.setOnClickListener(new OnClickListener() {

				
				public void onClick(View v) {

					performAuthentication();
				}
				
			});
		}
		
		// initialize unlink button
		m_button_unlink = (Button)findViewById(R.id.button_unlink);
		if (m_button_unlink != null) {

			// add click listener
			m_button_unlink.setOnClickListener(new OnClickListener() {

				
				public void onClick(View v) {

					performUnlink();
				}
				
			});
		}

		// initialize select tagstore button
		m_button_select = (Button)findViewById(R.id.button_select_tagstore);
		if (m_button_select != null) {

			// add click listener
			m_button_select.setOnClickListener(new OnClickListener() {

				
				public void onClick(View v) {

					performSelect();
				}
			});
		}
		
		// acquire instance
		m_Provider = DropboxStorageProvider.getInstance();
		
		
		// toggle button states
		toggleButtonState();
		
		// construct tagstore list
		m_tagstore_list = new ArrayList<CharSequence>();
		
	}

	/**
	 * enables / disables button states
	 */
	private void toggleButtonState() {
		
		if (m_Provider.isLoggedIn())
		{
			// enable logout button and select tagstore button
			m_button_select.setEnabled(true);
			m_button_unlink.setEnabled(true);
			m_button_authenticate.setEnabled(false);
		}
		else
		{
			m_button_select.setEnabled(false);
			m_button_unlink.setEnabled(false);
			m_button_authenticate.setEnabled(true);
		}
	}
	
	
	
	/**
	 * performs authentication
	 */
	private final void performAuthentication() {
	
		// start the authentication
		m_Provider.startAuthentication(DropboxSettingsActivity.this);
		
		// toggle button states
		toggleButtonState();		
	}
	
	/**
	 * performs unlinking
	 */
	private final void performUnlink() {
		
		// log out
		m_Provider.unlink(DropboxSettingsActivity.this);
		
		// toggle button states
		toggleButtonState();
	}
	
	/**
	 * performs selecting a tagstore
	 */
	private final void performSelect() {
		
		// build stores task
		EnumerateStoresTask task = new EnumerateStoresTask(DropboxSettingsActivity.this);
		
		// execute it
		task.execute();
	}

	protected Dialog onCreateDialog(int id) {
		
		if (id == DropboxSettingsActivity.SELECT_DIALOG_ID)
		{
			// convert tagstore list to array
			CharSequence[] list = m_tagstore_list.toArray(new CharSequence[m_tagstore_list.size()]);
			
			// construct alert dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this);
			builder.setTitle(R.string.select_tagstore);
			builder.setItems(list, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					handleSelection(m_tagstore_list.get(which));
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
	
	private final void handleSelection(CharSequence tagstore_name) {
		
		Logger.i("tagstore: " + tagstore_name);
		
		// acquire shared settings
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		
		// get settings editor
		Editor editor = settings.edit();
		
		// remove preceding slash
		String table_name = ((String)tagstore_name).substring(1);

		// construct sync table
		boolean result = DBManager.getInstance().createSyncTable(table_name);
		if (!result)
		{
			// failed to create sync table
			ToastManager.getInstance().displayToastWithString(R.string.error_failed_configure_tagstore);
			return;
		}
		
		// store new tagstore
		editor.putString(ConfigurationSettings.CURRENT_SYNCHRONIZATION_TAGSTORE, (String)tagstore_name);
		
		// done
		editor.commit();
	}
	
	private class EnumerateStoresTask extends AsyncTask<String, Void, Boolean> {
		
		/**
		 * activity
		 */
		private DropboxSettingsActivity m_activity;

		/**
		 * progress dialog
		 */
		private ProgressDialog m_dialog;
		
		/**
		 * constructor EnumerateStoresTask
		 * @param activity
		 */
		public EnumerateStoresTask(DropboxSettingsActivity activity) {
			
			// store activity
			m_activity = activity;
			
			// construct dialog
			m_dialog = new ProgressDialog(m_activity);
		}

		   protected void onPreExecute() {
		        m_dialog.setMessage(getString(R.string.enumerate_tagstores));
		        m_dialog.show();
		    }
		    protected void onPostExecute(final Boolean success) {
		        if (m_dialog.isShowing()) {
		            m_dialog.dismiss();
		        }

				if (m_tagstore_list.size() > 0)
				{
					// show tagstore selection dialog
					showDialog(DropboxSettingsActivity.SELECT_DIALOG_ID);
				}
				else
				{
					// display toast that no tagstores have yet been created
					ToastManager.getInstance().displayToastWithString(R.string.no_tagstores_found);
				}		        
		        
		    }
		   
		@Override
		protected Boolean doInBackground(String... params) {

			// clear tagstore list
			m_tagstore_list.clear();
			
			// 1. fetch a list of all available items
			ArrayList<String> items = m_Provider.getFiles("/", null);
			
			// check which items are directories
			for(String item : items) {
				
				// is file
				Logger.i("current_item: " + item);
				boolean is_file = m_Provider.isFile(item);
				if (is_file)
					continue;
				
				// does it have a path / .tagstore/store.cfg
				String store_path = item + STORE_TGS_PATH;
				boolean exists = is_file = m_Provider.isFile(store_path);
				if (exists)
				{
					// HACK: store.tgs file exists, assume it a valid tagstore
					m_tagstore_list.add(item);
					Logger.i("Found tagstore: " + item);
				}
			}
			return true;
		}
		
	}
	
	
}
