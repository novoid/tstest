package org.me.TagStore.ui;

import java.io.File;
import org.me.TagStore.R;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.DialogIds;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.interfaces.RetagDialogCallback;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * This class is used to perform common dialog operations. It performs all necessary tasks for creating dialogs and displaying the items
 * @author Johannes Anderwald
 *
 */
public class DialogItemOperations {

	private final Activity m_activity;
	private final ActivityGroup m_activity_group;
	
	/**
	 * constructor of class DialogItemOperations
	 * @param activity owner activity for the dialogs
	 * @param activity_group owner of activity which is assed for launching / sharing files
	 */
	public DialogItemOperations(Activity activity, ActivityGroup activity_group) {
		
		//
		// initialize activity
		//
		m_activity = activity;
		m_activity_group = activity_group;
	}
	
	/**
	 * prepares the dialog
	 * @param id dialog id
	 * @param dialog dialog object
	 * @param file_name file name
	 * @param is_tag if it is a tag
	 */
	public void prepareDialog(int id, Dialog dialog, String file_name, boolean is_tag) {
		
		//
		// check which dialog is requested
		//
		switch (id) {
			case DialogIds.DIALOG_DETAILS:
				FileDialogBuilder.updateDetailDialogFileView(dialog, m_activity_group, file_name);
				break;
			case DialogIds.DIALOG_RENAME:
				FileDialogBuilder.updateRenameDialogFile(dialog, file_name, is_tag, true, (RenameDialogCallback)m_activity);
				break;
			case DialogIds.DIALOG_RETAG:
				FileDialogBuilder.updateRetagDialogFile(dialog, file_name, (RetagDialogCallback)m_activity);
				break;
			case DialogIds.DIALOG_DETAILS_TAG:
				FileDialogBuilder.updateTagDetailDialog(dialog, file_name);
				break;
		}
	}
	
	/**
	 * creates the requested dialog
	 * @param id dialog to be created
	 * @return Dialog
	 */
	public Dialog createDialog(int id) {
		
		//
		// construct dialog
		//
		Dialog dialog = null;

		//
		// check which dialog is requested
		//
		switch (id) {

		case DialogIds.DIALOG_GENERAL_FILE_MENU:
			dialog = FileDialogBuilder.buildGeneralFileDialog(m_activity_group, (GeneralDialogCallback)m_activity);
			break;
		case DialogIds.DIALOG_DETAILS:
			dialog = FileDialogBuilder.buildDetailDialogFile(m_activity_group);
			break;
		case DialogIds.DIALOG_RENAME:
			dialog = FileDialogBuilder.buildRenameDialogFile(m_activity_group);
			break;
		case DialogIds.DIALOG_RETAG:
			dialog = FileDialogBuilder.buildRetagDialogFile(m_activity_group);
			break;
		case DialogIds.DIALOG_GENERAL_TAG_MENU:
			dialog = FileDialogBuilder.buildGeneralTagDialog(m_activity_group, (GeneralDialogCallback)m_activity);
			break;			
		case DialogIds.DIALOG_DETAILS_TAG:
			dialog = FileDialogBuilder.buildTagDetailDialog(m_activity_group);
			break;
		}
		return dialog;
	}
	
	
	/**
	 * performs a common dialog operation
	 * @param item_path path of item or tag name when is_tag is set to true
	 * @param is_tag determines if the current item is a tag or not
	 * @param action_id selection choice
	 * @return true if it needs a refresh
	 */
	public boolean performDialogItemOperation(String item_path, boolean is_tag, FileDialogBuilder.MENU_ITEM_ENUM action_id) {
		
		if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_RETAG)
		{
			//
			// launch retag dialog
			//
			m_activity.showDialog(DialogIds.DIALOG_RETAG);
			
		}
		if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_RENAME)
		{
			//
			// launch rename dialog
			//
			m_activity.showDialog(DialogIds.DIALOG_RENAME);
		}
		else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DETAILS)
		{
			if (is_tag) 
			{
				//
				// launch tag details dialog
				//
				m_activity.showDialog(DialogIds.DIALOG_DETAILS_TAG);
			}
			else
			{
				//
				// launch file details dialog
				//
				m_activity.showDialog(DialogIds.DIALOG_DETAILS);
			}
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DELETE) 
		{
			
			if (is_tag)
			{
				//
				// remove tag
				//
				DBManager db_man = DBManager.getInstance();
				db_man.deleteTag(item_path);
			}
			else
			{
				//
				// remove the file
				//
				FileTagUtility.removeFile(item_path, m_activity);
			}

			//
			// refresh needed
			//
			return true;
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN)
		{
			//
			// launch file
			//
			launchFile(item_path);
			
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_SEND) {

			//
			// send file
			//
			return sendFile(item_path);
		}
		
		//
		// no refresh needed
		//
		return false;
	}
	
	private boolean sendFile(String item_path) {
		
		File file = new File(item_path);

		if (file.exists() == false) {
			
			//
			// get localized error format
			//
			String error_format = m_activity.getString(R.string.error_format_file_removed);
			
			//
			// format the error
			//
			String msg = String.format(error_format, item_path);
			
			//
			// create the toast
			//
			Toast toast = Toast.makeText(m_activity,
					msg,
					Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// needs refresh
			//
			return true;
		}

		//
		// create new intent
		//
		Intent intent = new Intent();

		//
		// set intent action
		//
		intent.setAction(android.content.Intent.ACTION_SEND);

		//
		// create uri from file
		//
		Uri uri_file = Uri.fromFile(file);

		//
		// get mime type map instance
		//
		MimeTypeMap mime_map = MimeTypeMap.getSingleton();

		//
		// get file extension
		//
		String file_extension = item_path.substring(item_path
				.lastIndexOf(".") + 1);

		//
		// guess file extension
		//
		String mime_type = mime_map
				.getMimeTypeFromExtension(file_extension);

		//
		// set intent data type
		//
		intent.setDataAndType(uri_file, mime_type);

		//
		// put file contents
		//
		intent.putExtra(Intent.EXTRA_STREAM, uri_file);

		//
		// start activity
		//
		m_activity_group.startActivity(intent);
		
		//
		// no refresh needed
		//
		return false;
	}
	
	/**
	 * launches a file with the default registered application
	 * @param item_path path of the file
	 */
	private void launchFile(String item_path) {
		
		//
		// get current storage state
		//
		String storage_state = Environment.getExternalStorageState();
		if (!storage_state.equals(Environment.MEDIA_MOUNTED) && !storage_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
		{
			//
			// the media is currently not accessible
			//
			String media_available = m_activity.getString(R.string.error_media_not_mounted);
			
			//
			// create toast
			//
			Toast toast = Toast.makeText(m_activity, media_available, Toast.LENGTH_SHORT);
			
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
		// construct file object
		//
		File file = new File(item_path);

		if (file.exists() == false) {
			
			//
			// get localized error format
			//
			String error_format = m_activity.getString(R.string.error_format_file_removed);
			
			//
			// format the error
			//
			String msg = String.format(error_format, item_path);
			
			//
			// create the toast
			//
			Toast toast = Toast.makeText(m_activity,
					msg,
					Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// FIXME: refresh perhaps?
			//
			return;
		}

		//
		// create new intent
		//
		Intent intent = new Intent();

		//
		// set intent action
		//
		intent.setAction(android.content.Intent.ACTION_VIEW);

		//
		// create uri from file
		//
		Uri uri_file = Uri.fromFile(file);

		//
		// get mime type map instance
		//
		MimeTypeMap mime_map = MimeTypeMap.getSingleton();

		//
		// get file extension
		//
		String file_extension = item_path
				.substring(item_path.lastIndexOf(".") + 1);

		//
		// guess file extension
		//
		String mime_type = mime_map.getMimeTypeFromExtension(file_extension);

		//
		// set intent data type
		//
		intent.setDataAndType(uri_file, mime_type);

		//
		// start activity
		//
		m_activity_group.startActivity(intent);
	}
}
