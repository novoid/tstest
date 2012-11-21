package org.me.tagstore;

import java.util.ArrayList;

import org.me.tagstore.R;
import org.me.tagstore.core.ConfigurationSettings;
import org.me.tagstore.core.DBManager;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.SMBStorageProvider;
import org.me.tagstore.ui.ToastManager;

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
import android.widget.EditText;

/**
 * This class implements Dropbox specific settings, such as authentication,
 * logging out, and selecting a tagstore
 * 
 */
public class SMBSettingsActivity extends Activity {

	/**
	 * Dropbox api reference
	 */
	private SMBStorageProvider m_Provider;

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
	 * configuration dialog id
	 */
	private static final int CONFIGURATION_DIALOG_ID = 2;

	/**
	 * tagstore list
	 */
	private ArrayList<CharSequence> m_tagstore_list;

	private static final String STORE_TGS_PATH = "/.tagstore/store.tgs";

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

	private void initialize() {

		// initialize authenticate button
		m_button_authenticate = (Button) findViewById(R.id.button_authenticate);
		if (m_button_authenticate != null) {

			// add click listener
			m_button_authenticate.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					performAuthentication();
				}

			});
		}

		// initialize unlink button
		m_button_unlink = (Button) findViewById(R.id.button_unlink);
		if (m_button_unlink != null) {

			// add click listener
			m_button_unlink.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					performUnlink();
				}

			});
		}

		// initialize select tagstore button
		m_button_select = (Button) findViewById(R.id.button_select_tagstore);
		if (m_button_select != null) {

			// add click listener
			m_button_select.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					performSelect();
				}
			});
		}

		// acquire instance
		m_Provider = (SMBStorageProvider) SMBStorageProvider.getInstance();

		// toggle button states
		toggleButtonState();

		// construct tagstore list
		m_tagstore_list = new ArrayList<CharSequence>();

	}

	/**
	 * enables / disables button states
	 */
	private void toggleButtonState() {

		if (m_Provider.isLoggedIn()) {
			// enable logout button and select tagstore button
			m_button_select.setEnabled(true);
			m_button_unlink.setEnabled(true);
			m_button_authenticate.setEnabled(false);
		} else {
			m_button_select.setEnabled(false);
			m_button_unlink.setEnabled(false);
			m_button_authenticate.setEnabled(true);
		}
	}

	/**
	 * performs authentication
	 */
	private final void performAuthentication() {

		// show alert dialog
		showDialog(SMBSettingsActivity.CONFIGURATION_DIALOG_ID);
	}

	/**
	 * performs unlinking
	 */
	private final void performUnlink() {

		// log out
		m_Provider.unlink(SMBSettingsActivity.this);

		// clear login info
		saveConfiguration("", "", "", "");

		// toggle button states
		toggleButtonState();
	}

	/**
	 * performs selecting a tagstore
	 */
	private final void performSelect() {

		// build stores task
		EnumerateStoresTask task = new EnumerateStoresTask(
				SMBSettingsActivity.this);

		// execute it
		task.execute();
	}

	protected Dialog onCreateDialog(int id) {

		if (id == SMBSettingsActivity.SELECT_DIALOG_ID) {
			// convert tagstore list to array
			CharSequence[] list = m_tagstore_list
					.toArray(new CharSequence[m_tagstore_list.size()]);

			// construct alert dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		} else if (id == SMBSettingsActivity.CONFIGURATION_DIALOG_ID) {
			// construct dialog
			final Dialog dialog = new Dialog(SMBSettingsActivity.this);

			// set content layout
			dialog.setContentView(R.layout.smb_configuration);

			// set title
			dialog.setTitle(getString(R.string.smb_configuration));

			// init configuration dialog
			initializeConfigurationDialog(dialog);

			// done
			return dialog;
		}

		//
		// unknown dialog requested
		//
		return null;
	}

	private void initializeConfigurationDialog(final Dialog view) {

		// acquire shared settings
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		// get stored credentials
		String user_name = settings.getString(
				ConfigurationSettings.SMB_USER_NAME, "");
		String pwd = settings.getString(ConfigurationSettings.SMB_PASSWORD, "");
		String server = settings
				.getString(ConfigurationSettings.SMB_SERVER, "");
		String share = settings.getString(ConfigurationSettings.SMB_SHARE, "");

		// set credentials
		final EditText user_name_txt = (EditText) view
				.findViewById(R.id.user_name_value);
		if (user_name_txt != null)
			user_name_txt.setText(user_name);

		final EditText pwd_txt = (EditText) view
				.findViewById(R.id.password_value);
		if (pwd_txt != null)
			pwd_txt.setText(pwd);

		final EditText server_txt = (EditText) view
				.findViewById(R.id.server_value);
		if (server_txt != null)
			server_txt.setText(server);

		final EditText share_txt = (EditText) view
				.findViewById(R.id.share_value);
		if (share_txt != null)
			share_txt.setText(share);

		Button button_connect = (Button) view.findViewById(R.id.button_connect);
		if (button_connect != null) {

			// set click listener
			button_connect.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					if (user_name_txt != null) {

						if (user_name_txt.getText().toString().length() == 0) {
							ToastManager.getInstance().displayToastWithString(
									R.string.error_no_user_name);
							return;
						}
					}

					if (pwd_txt != null) {

						if (pwd_txt.getText().toString().length() == 0) {
							ToastManager.getInstance().displayToastWithString(
									R.string.error_no_password);
							return;
						}
					}

					if (server_txt != null) {

						if (server_txt.getText().toString().length() == 0) {
							ToastManager.getInstance().displayToastWithString(
									R.string.error_no_server);
							return;
						}
					}

					if (share_txt != null) {

						if (share_txt.getText().toString().length() == 0) {
							ToastManager.getInstance().displayToastWithString(
									R.string.error_no_share);
							return;
						}
					}

					// get smb configuration
					String user_name = user_name_txt.getText().toString();
					String pwd = pwd_txt.getText().toString();
					String server = server_txt.getText().toString();
					String share = share_txt.getText().toString();

					boolean connect = connect(user_name, pwd, server, share);
					if (connect) {
						// successfully connected
						ToastManager.getInstance().displayToastWithString(
								R.string.connect_success);

						// save configuration
						saveConfiguration(user_name, pwd, server, share);

						// dismiss
						view.dismiss();

						// enable other buttons
						toggleButtonState();
					} else {
						// failed to connect
						ToastManager.getInstance().displayToastWithString(
								R.string.error_failed_connect);
					}
				}

			});

		}
	}

	private void saveConfiguration(String username, String password,
			String server, String share) {

		// acquire shared settings
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		// get settings editor
		Editor editor = settings.edit();

		// store credentials
		editor.putString(ConfigurationSettings.SMB_USER_NAME, username);
		editor.putString(ConfigurationSettings.SMB_PASSWORD, password);
		editor.putString(ConfigurationSettings.SMB_SERVER, server);

		// shares need to be in the format '/share_name/'
		if (!share.startsWith("/"))
			share = "/" + share;
		if (!share.endsWith("/"))
			share += "/";

		editor.putString(ConfigurationSettings.SMB_SHARE, share);

		// done
		editor.commit();
	}

	private boolean connect(String username, String password, String server,
			String share) {

		// perform login

		ProgressDialog dialog = new ProgressDialog(SMBSettingsActivity.this);
		dialog.show();

		try {

			// connect
			m_Provider.login(server, username, password);

			// shares need to be in the format '/share_name/'
			if (!share.startsWith("/"))
				share = "/" + share;
			if (!share.endsWith("/"))
				share += "/";

			// get files
			m_Provider.getFiles(share, null);
			dialog.dismiss();
			return true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		// dismiss dialog
		dialog.dismiss();

		// done
		return false;
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
		String table_name = ((String) tagstore_name).substring(1);

		// construct sync table
		boolean result = DBManager.getInstance().createSyncTable(table_name);
		if (!result) {
			// failed to create sync table
			ToastManager.getInstance().displayToastWithString(
					R.string.error_failed_configure_tagstore);
			return;
		}

		// get share name
		String share = settings.getString(ConfigurationSettings.SMB_SHARE, "");

		// construct path
		String path = share + table_name;
		Logger.i("path: " + path);

		// store new tagstore
		editor.putString(
				ConfigurationSettings.CURRENT_SYNCHRONIZATION_TAGSTORE,
				(String) path);

		// done
		editor.commit();
	}

	private class EnumerateStoresTask extends AsyncTask<String, Void, Boolean> {

		/**
		 * activity
		 */
		private SMBSettingsActivity m_activity;

		/**
		 * progress dialog
		 */
		private ProgressDialog m_dialog;

		/**
		 * share
		 */
		private String m_share;

		/**
		 * constructor EnumerateStoresTask
		 * 
		 * @param activity
		 */
		public EnumerateStoresTask(SMBSettingsActivity activity) {

			// store activity
			m_activity = activity;

			// construct dialog
			m_dialog = new ProgressDialog(m_activity);

			// init share
			initShare();
		}

		private void initShare() {

			// acquire shared settings
			SharedPreferences settings = getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);

			// get default share
			m_share = settings.getString(ConfigurationSettings.SMB_SHARE, "");
		}

		protected void onPreExecute() {
			m_dialog.setMessage(getString(R.string.enumerate_tagstores));
			m_dialog.show();
		}

		protected void onPostExecute(final Boolean success) {
			if (m_dialog.isShowing()) {
				m_dialog.dismiss();
			}

			if (m_tagstore_list.size() > 0) {
				// show tagstore selection dialog
				showDialog(SMBSettingsActivity.SELECT_DIALOG_ID);
			} else {
				// display toast that no tagstores have yet been created
				ToastManager.getInstance().displayToastWithString(
						R.string.no_tagstores_found);
			}

		}

		@Override
		protected Boolean doInBackground(String... params) {

			// clear tagstore list
			m_tagstore_list.clear();

			// 1. fetch a list of all available items
			ArrayList<String> items = m_Provider.getFiles(m_share, null);

			// check which items are directories
			for (String item : items) {

				// is file
				Logger.i("current_item: " + item);
				boolean is_file = m_Provider.isFile(m_share + item);
				if (is_file)
					continue;

				// does it have a path / .tagstore/store.cfg
				String store_path = m_share + item + STORE_TGS_PATH;
				boolean exists = is_file = m_Provider.isFile(store_path);
				if (exists) {
					// HACK: store.tgs file exists, assume it a valid tagstore
					// remove trailing backslash
					if (item.endsWith("/"))
						item = item.substring(0, item.length() - 1);
					m_tagstore_list.add("/" + item);
					Logger.i("Found tagstore: " + item);
				}
			}
			return true;
		}

	}

}
