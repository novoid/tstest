package org.me.TagStore.ui;

import java.io.File;
import org.me.TagStore.R;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.OptionsDialogCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.interfaces.RetagDialogCallback;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * This class is used to perform common dialog operations. It performs all necessary tasks for creating dialogs and displaying the items
 * @author Johannes Anderwald
 *
 */
public class DialogItemOperations {

	private final Fragment m_activity;
	private final FragmentActivity m_activity_group;
	private final FragmentManager m_fragment_manager;
	
	/**
	 * constructor of class DialogItemOperations
	 * @param activity owner activity for the dialogs
	 * @param activity_group owner of activity which is assed for launching / sharing files
	 */
	public DialogItemOperations(Fragment activity, FragmentActivity activity_group, FragmentManager fragment_manager) {
		
		//
		// initialize activity
		//
		m_activity = activity;
		m_activity_group = activity_group;
		m_fragment_manager = fragment_manager;
	}
	
	/**
	 * creates the requested dialog
	 * @param id dialog to be created
	 * @return Dialog
	 */
	public Dialog createDialog(int id, String item_name, boolean is_tag, DialogFragment fragment) {
		
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
			dialog = FileDialogBuilder.buildDetailDialogFile(m_activity_group, item_name);
			break;
		case DialogIds.DIALOG_RENAME:
			dialog = FileDialogBuilder.buildRenameDialogFile(m_activity_group, item_name, is_tag, true, (RenameDialogCallback)m_activity, fragment);
			break;
		case DialogIds.DIALOG_RETAG:
			dialog = FileDialogBuilder.buildRetagDialogFile(m_activity_group, item_name, (RetagDialogCallback)m_activity, fragment);
			break;
		case DialogIds.DIALOG_GENERAL_TAG_MENU:
			dialog = FileDialogBuilder.buildGeneralTagDialog(m_activity_group, (GeneralDialogCallback)m_activity);
			break;			
		case DialogIds.DIALOG_DETAILS_TAG:
			dialog = FileDialogBuilder.buildTagDetailDialog(m_activity_group, item_name);
			break;
		case DialogIds.DIALOG_OPTIONS:
			dialog = FileDialogBuilder.buildOptionsDialogFile(m_activity_group, item_name, (OptionsDialogCallback)m_activity, fragment);
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
			CommonDialogFragment fragment = CommonDialogFragment.newInstance(item_path, is_tag, DialogIds.DIALOG_RETAG);
			fragment.setDialogItemOperation(this);
			fragment.show(m_fragment_manager, "FIXME");		
		}
		
		if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_RENAME)
		{
			//
			// launch rename dialog
			//
			CommonDialogFragment fragment = CommonDialogFragment.newInstance(item_path, is_tag, DialogIds.DIALOG_RENAME);
			fragment.setDialogItemOperation(this);			
			fragment.show(m_fragment_manager, "FIXME");		
		}
		else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DETAILS)
		{
			
			Logger.i(item_path + " is_tag: " + is_tag);
			
			if (is_tag) 
			{
				//
				// launch tag details dialog
				//
				CommonDialogFragment fragment = CommonDialogFragment.newInstance(item_path, is_tag, DialogIds.DIALOG_DETAILS_TAG);
				fragment.setDialogItemOperation(this);				
				fragment.show(m_fragment_manager, "FIXME");		
			}
			else
			{
				//
				// launch file details dialog
				//
				CommonDialogFragment fragment = CommonDialogFragment.newInstance(item_path, is_tag, DialogIds.DIALOG_DETAILS);
				fragment.setDialogItemOperation(this);				
				fragment.show(m_fragment_manager, "FIXME");		
			}
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DELETE) 
		{
			
			if (is_tag)
			{
				//
				// remove tag
				//
				FileTagUtility.removeTag(item_path);
			}
			else
			{
				//
				// check if external disk is mounted
				//
				if (!isExternalStorageAvailable())
					return false;

				//
				// remove the file
				//
				FileTagUtility.removeFile(item_path, m_activity_group);
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
	
	/**
	 * sends a file
	 * @param item_path path of the item
	 * @return true on success
	 */
	private boolean sendFile(String item_path) {
		
		//
		// check if external disk is mounted
		//
		if (!isExternalStorageAvailable())
			return false;

		//
		// does the file still exist
		//
		if (!isFileExisting(item_path))
			return false;

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
		Uri uri_file = Uri.fromFile(new File(item_path));

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
	 * checks if the external storage disk is available
	 * @return
	 */
	private boolean isExternalStorageAvailable() {
		
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
			Toast toast = Toast.makeText(m_activity_group, media_available, Toast.LENGTH_SHORT);
			
			//
			// display toast
			//
			toast.show();
			
			//
			// done
			//
			return false;
		}
		
		//
		// all ready
		//
		return true;
	}
	
	/**
	 * checks if the specified file still exists
	 * @param item_path item path to be checked
	 * @return true when it still exists
	 */
	private boolean isFileExisting(String item_path) {
		
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
			Toast toast = Toast.makeText(m_activity_group,
					msg,
					Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// FIXME: refresh perhaps?
			//
			return false;
		}
		
		//
		// file exists
		//
		return true;
	}
	
	
	/**
	 * launches a file with the default registered application
	 * @param item_path path of the file
	 */
	private void launchFile(String item_path) {
		
		//
		// check if it is mounted
		//
		if (!isExternalStorageAvailable())
			return;

		//
		// does the file still exist
		//
		if (!isFileExisting(item_path))
			return;

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
		Uri uri_file = Uri.fromFile(new File(item_path));

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
