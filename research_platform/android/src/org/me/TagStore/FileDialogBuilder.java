package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

/**
 * This class builds various dialogs when a file is selected
 * @author Johannes Anderwald
 *
 */
public class FileDialogBuilder {

	public enum MENU_ITEM_ENUM {
		MENU_ITEM_DETAILS, MENU_ITEM_SEND, MENU_ITEM_DELETE
	};

	/**
	 * This interface is used as a call-back mechanism which is invoked when a dialog menu is pressed
	 * @author Johannes Anderwald
	 *
	 */
	public interface GeneralDialogCallback
	{
		public abstract void processMenuFileSelection(String file_name, MENU_ITEM_ENUM selection);
	}

	/**
	 * Implements a helper class which invokes a callback when a menu item of dialog is clicked
	 * @author Johannes Anderwald
	 *
	 */
	private static class FileMenuClickListener implements
			DialogInterface.OnClickListener {

		/**
		 * stores the menu item actions
		 */
		private ArrayList<MENU_ITEM_ENUM> m_action_ids;
		
		/**
		 * stores the file name of file
		 */
		private String m_file_name;
		
		/**
		 * stores the callback object
		 */
		GeneralDialogCallback m_callback;
		
		/**
		 * constructor of class FileMenuClickListener
		 * @param file_name file name of item to be launched
		 * @param action_ids array of actions
		 * @param callback 
		 */
		
		FileMenuClickListener(String file_name, ArrayList<MENU_ITEM_ENUM> action_ids, GeneralDialogCallback callback) {

			//
			// store members
			//
			m_action_ids = action_ids;
			m_file_name = file_name;
			m_callback = callback;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			//
			// perform callback
			//
			if (m_callback != null)
			{
				m_callback.processMenuFileSelection(m_file_name, m_action_ids.get(which));
			}				
		}
	};
	
	/**
	 * constructs a general dialog
	 * 
	 * @param activity activity group this dialog belongs to
	 * @param file_name name of the file
	 */
	public static Dialog buildGeneralDialogFile(ActivityGroup activity, String file_name, GeneralDialogCallback callback) {

		//
		// construct new alert builder
		//
		AlertDialog.Builder builder = new AlertDialog.Builder(
				activity);

		//
		// get localized title
		//
		String title = activity.getApplicationContext().getString(R.string.options);
		
		//
		// set title
		//
		builder.setTitle(title);

		//
		// construct array list for options
		//
		ArrayList<String> menu_options = new ArrayList<String>();
		ArrayList<MENU_ITEM_ENUM> action_ids = new ArrayList<MENU_ITEM_ENUM>();

		//
		// add default menu items
		//
		String details_string = activity.getApplicationContext().getString(R.string.details);
		menu_options.add(details_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_DETAILS);

		String delete_string = activity.getApplicationContext().getString(R.string.delete);
		menu_options.add(delete_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_DELETE);

		String send_string = activity.getApplicationContext().getString(R.string.send);
		menu_options.add(send_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_SEND);

		//
		// FIXME: implement mime options
		//
		Logger.i("Should FileDialogBuilder::buildGeneralDialogFile process mime types options for file ");

		//
		// convert to string array
		//
		String[] menu_option_list = menu_options.toArray(new String[1]);

		
		//
		// create menu click listener
		//
		FileDialogBuilder.FileMenuClickListener listener = new FileDialogBuilder.FileMenuClickListener(file_name, action_ids, callback);
		
		//
		// set menu options and click handler
		//
		builder.setItems(menu_option_list, listener);

		//
		// create dialog
		//
		AlertDialog dialog = builder.create();
		
		return dialog;
	}
	
	/**
	 * creates a view for the detail dialog view
	 * @param activity activity group
	 * @param file_name file name
	 * @return View
	 */
	public static void updateDetailDialogFileView(Dialog dialog, ActivityGroup activity, String file_name) {
		
		//
		// cast to AlertDialog
		//
		AlertDialog alert_dialog = (AlertDialog)dialog;
		
		//
		// get text field for name
		//
		TextView text = (TextView) alert_dialog.findViewById(R.id.file_name_value);
		if (text != null) {
			//
			// set file name
			//
			text.setText(file_name);
		}

		//
		// create file object
		//
		File file = new File(file_name);

		//
		// remove file name part
		//
		String item_directory = file.getParent();

		//
		// get text field for folder
		//
		text = (TextView) alert_dialog.findViewById(R.id.file_folder_value);
		if (text != null) {
			//
			// set file name
			//
			text.setText(item_directory);
		}

		//
		// get file size
		//
		long length = file.length();

		//
		// get text field for size
		//
		text = (TextView) alert_dialog.findViewById(R.id.file_size_value);
		if (text != null) {
			
			//
			// get localized bytes
			//
			String bytes = activity.getApplicationContext().getString(R.string.bytes);
			
			//
			// set file name
			//
			text.setText(Long.toString(length) + " " + bytes);
		}

		//
		// get mime type map instance
		//
		MimeTypeMap mime_map = MimeTypeMap.getSingleton();

		//
		// get file extension
		//
		String file_extension = file_name
				.substring(file_name.lastIndexOf(".") + 1);

		//
		// guess mime from extension extension
		//
		String mime_type = mime_map.getMimeTypeFromExtension(file_extension);
		//
		// get text field for size
		//
		text = (TextView) alert_dialog.findViewById(R.id.file_type_value);
		if (text != null) {
			//
			// set mime type
			//
			text.setText(mime_type);
		}
	}
	
	/**
	 * constructs the detail dialog
	 * @param activity activity which is parent of the dialog
	 * @param file_name path of the file
	 * @return Dialog
	 */
	public static Dialog buildDetailDialogFile(ActivityGroup activity, String file_name) {

		//
		// get layout inflater service
		//
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//
		// construct view
		//
		View layout = inflater.inflate(R.layout.details_dialog,
				null);//(ViewGroup) findViewById(R.id.layout_root));
		

		//
		// construct builder
		//
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		//
		// set view
		//
		builder.setView(layout);
		
		
		//
		// create dialog
		//
		AlertDialog alertDialog = builder.create();
		
		//
		// done
		//
		return alertDialog;
	}


	
}
