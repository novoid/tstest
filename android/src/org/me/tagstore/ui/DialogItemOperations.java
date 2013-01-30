package org.me.tagstore.ui;

import java.io.File;

import junit.framework.Assert;

import org.me.tagstore.R;
import org.me.tagstore.core.FileTagUtility;
import org.me.tagstore.core.Logger;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.webkit.MimeTypeMap;

/**
 * This class is used to perform common dialog operations. It performs all
 * necessary tasks for creating dialogs and displaying the items
 * 
 */
public class DialogItemOperations {

	/**
	 * activity of the fragment
	 */
	private FragmentActivity m_activity_group;

	/**
	 * fragment manager
	 */
	private FragmentManager m_fragment_manager;

	/**
	 * new fragment
	 */
	private CommonDialogFragment m_fragment = null;

	/**
	 * file utility
	 */
	private FileTagUtility m_utility;

	/**
	 * toast manager
	 */
	private ToastManager m_toast_man;

	private FileDialogBuilder m_builder;

	/**
	 * constructor of class DialogItemOperations
	 * 
	 * @param activity_group
	 *            owner of activity which is used for launching / sharing files
	 */
	public void initDialogItemOperations(FragmentActivity activity_group,
			FragmentManager fragment_manager, FileTagUtility utility,
			ToastManager toast_man, FileDialogBuilder builder) {

		//
		// initialize members
		//
		m_activity_group = activity_group;
		m_fragment_manager = fragment_manager;
		m_utility = utility;
		m_toast_man = toast_man;
		m_builder = builder;
	}

	/**
	 * creates the requested dialog
	 * 
	 * @param id
	 *            dialog to be created
	 * @return Dialog
	 */
	public Dialog createDialog(int id, String item_name, boolean is_tag,
			DialogFragment fragment) {

		//
		// construct dialog
		//
		Dialog dialog = null;

		//
		// check which dialog is requested
		//
		switch (id) {

		case DialogIds.DIALOG_GENERAL_FILE_MENU:
			dialog = m_builder.buildGeneralFileDialog(m_activity_group,
					item_name, fragment);
			break;

		case DialogIds.DIALOG_DETAILS:
			dialog = m_builder.buildDetailDialogFile(m_activity_group,
					item_name, fragment);
			break;
		case DialogIds.DIALOG_RENAME:
			dialog = m_builder.buildRenameDialogFile(m_activity_group,
					item_name, is_tag, true, fragment);
			break;
		case DialogIds.DIALOG_RETAG:
			dialog = m_builder.buildRetagDialogFile(m_activity_group,
					item_name, fragment);
			break;
		case DialogIds.DIALOG_GENERAL_TAG_MENU:
			dialog = m_builder
					.buildGeneralTagDialog(m_activity_group, fragment);
			break;
		case DialogIds.DIALOG_DETAILS_TAG:
			dialog = m_builder.buildTagDetailDialog(m_activity_group,
					item_name, fragment);
			break;
		case DialogIds.DIALOG_OPTIONS:
			dialog = m_builder.buildOptionsDialogFile(m_activity_group,
					item_name, fragment);
			break;
		case DialogIds.DIALOG_OPEN_AS:
			dialog = m_builder.buildOpenAsDialog(m_activity_group, item_name,
					fragment);
			break;
		}
		return dialog;
	}

	/**
	 * performs a common dialog operation
	 * 
	 * @param item_path
	 *            path of item or tag name when is_tag is set to true
	 * @param is_tag
	 *            determines if the current item is a tag or not
	 * @param action_id
	 *            selection choice
	 * @return true if it needs a refresh
	 */
	public boolean performDialogItemOperation(String item_path, boolean is_tag,
			FileDialogBuilder.MENU_ITEM_ENUM action_id) {

		if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_RETAG) {
			//
			// launch retag dialog
			//
			m_fragment = CommonDialogFragment.newInstance(m_activity_group,
					item_path, is_tag, DialogIds.DIALOG_RETAG);
			m_fragment.setDialogItemOperation(this);
			m_fragment.show(m_fragment_manager, "FIXME");
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS) {
			//
			// launch open as dialog
			//
			m_fragment = CommonDialogFragment.newInstance(m_activity_group,
					item_path, is_tag, DialogIds.DIALOG_OPEN_AS);
			m_fragment.setDialogItemOperation(this);
			m_fragment.show(m_fragment_manager, "FIXME");
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_RENAME) {
			//
			// launch rename dialog
			//
			m_fragment = CommonDialogFragment.newInstance(m_activity_group,
					item_path, is_tag, DialogIds.DIALOG_RENAME);
			m_fragment.setDialogItemOperation(this);
			m_fragment.show(m_fragment_manager, "FIXME");
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DETAILS) {

			Logger.i(item_path + " is_tag: " + is_tag);

			if (is_tag) {
				//
				// launch tag details dialog
				//
				m_fragment = CommonDialogFragment.newInstance(m_activity_group,
						item_path, is_tag, DialogIds.DIALOG_DETAILS_TAG);
				m_fragment.setDialogItemOperation(this);
				m_fragment.show(m_fragment_manager, "FIXME");
			} else {
				//
				// launch file details dialog
				//
				m_fragment = CommonDialogFragment.newInstance(m_activity_group,
						item_path, is_tag, DialogIds.DIALOG_DETAILS);
				m_fragment.setDialogItemOperation(this);
				m_fragment.show(m_fragment_manager, "FIXME");
			}
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DELETE) {
			if (is_tag) {
				//
				// remove tag
				//
				m_utility.removeTag(item_path);
			} else {
				//
				// check if external disk is mounted
				//
				if (!isExternalStorageAvailable())
					return false;

				//
				// remove the file
				//
				m_utility.removeFile(item_path, true);
			}

			//
			// refresh needed
			//
			return true;
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN) {
			//
			// launch file
			//
			launchFile(item_path);

		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_SEND) {

			//
			// send file
			//
			return sendFile(item_path);
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_AUDIO
				|| action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_IMAGE
				|| action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_TEXT
				|| action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_VIDEO) {
			//
			// open file as
			//
			openFileAs(item_path, action_id);
		}

		//
		// no refresh needed
		//
		return false;
	}

	/**
	 * sends a file
	 * 
	 * @param item_path
	 *            path of the item
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
		String file_extension = item_path
				.substring(item_path.lastIndexOf(".") + 1).toLowerCase();

		//
		// guess file extension
		//
		String mime_type = mime_map.getMimeTypeFromExtension(file_extension);
		if (mime_type == null)
		{
			// RFC 2046 
			mime_type = "application/octet-stream";
		}
		Logger.e(mime_type);
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
	 * 
	 * @return
	 */
	private boolean isExternalStorageAvailable() {

		//
		// get current storage state
		//
		String storage_state = Environment.getExternalStorageState();
		if (!storage_state.equals(Environment.MEDIA_MOUNTED)
				&& !storage_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			//
			// the media is currently not accessible
			//
			m_toast_man
					.displayToastWithString(R.string.error_media_not_mounted);

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
	 * 
	 * @param item_path
	 *            item path to be checked
	 * @return true when it still exists
	 */
	private boolean isFileExisting(String item_path) {

		//
		// construct file object
		//
		File file = new File(item_path);

		if (file.exists() == false) {

			//
			// display error format
			//
			m_toast_man.displayToastWithFormat(
					R.string.error_format_file_removed, item_path);

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
	 * opens the file with specified content type
	 * 
	 * @param item_path
	 *            item path
	 * @param action_id
	 *            action id
	 */
	private void openFileAs(String item_path,
			FileDialogBuilder.MENU_ITEM_ENUM action_id) {

		String mime_type = "";

		if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_AUDIO) {
			mime_type = "audio/*";
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_IMAGE) {
			mime_type = "image/*";
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_VIDEO) {
			mime_type = "video/*";
		} else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_TEXT) {
			mime_type = "text/plain";
		} else {
			//
			// unexpected
			//
			Logger.e("BUG detected in openFileAs");
			return;
		}

		//
		// launch file
		//
		performLaunch(item_path, mime_type, false);
	}

	private void performLaunch(String item_path, String mime_type,
			boolean launch_open_as_dialog) {

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
		// set intent data type
		//
		intent.setDataAndType(uri_file, mime_type);

		try {
			//
			// start activity
			//
			Assert.assertTrue(m_activity_group != null);
			m_activity_group.startActivityForResult(intent, 1);
		} catch (android.content.ActivityNotFoundException exc) {
			//
			// file failed to launch, launch open as dialog
			//
			if (launch_open_as_dialog) {
				m_fragment = CommonDialogFragment.newInstance(m_activity_group,
						item_path, false, DialogIds.DIALOG_OPEN_AS);
				m_fragment.setDialogItemOperation(this);
				m_fragment.show(m_fragment_manager, "FIXME");
			}
		}
	}

	/**
	 * launches a file with the default registered application
	 * 
	 * @param item_path
	 *            path of the file
	 */
	private void launchFile(String item_path) {

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
		// launch file in external application
		//
		performLaunch(item_path, mime_type, true);
	}
}
