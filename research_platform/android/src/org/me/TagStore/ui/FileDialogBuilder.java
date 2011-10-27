package org.me.TagStore.ui;

import java.io.File;
import java.util.ArrayList;

import org.me.TagStore.R;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TimeFormatUtility;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.OptionsDialogCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.interfaces.RetagDialogCallback;


import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class builds various dialogs when a file is selected
 * @author Johannes Anderwald
 *
 */
public class FileDialogBuilder {

	public enum MENU_ITEM_ENUM {
		MENU_ITEM_DETAILS, MENU_ITEM_SEND, MENU_ITEM_DELETE, MENU_ITEM_RENAME, MENU_ITEM_RETAG, MENU_ITEM_OPEN
	};

	final static int [] s_ids = new int[]{R.id.button_ignore_current, R.id.button_ignore_new, R.id.button_rename_current, R.id.button_rename_new};

	
	public static Dialog buildGeneralTagDialog(ActivityGroup activity, GeneralDialogCallback callback) {
		
		//
		// call internal builder
		//
		return buildCommonDialog(activity, true, callback);		
		
	}
	
	public static Dialog buildGeneralFileDialog(ActivityGroup activity, GeneralDialogCallback callback) {
		//
		// call internal builder
		//
		return buildCommonDialog(activity, false, callback);
	}
	
	/**
	 * constructs a general dialog
	 * 
	 * @param activity activity group this dialog belongs to
	 * @param item name of the file
	 */
	private static Dialog buildCommonDialog(ActivityGroup activity, boolean is_tag, GeneralDialogCallback callback) {

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

		String rename_string = activity.getApplicationContext().getString(R.string.rename);
		menu_options.add(rename_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_RENAME);
		
		String delete_string = activity.getApplicationContext().getString(R.string.delete);
		menu_options.add(delete_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_DELETE);

		if (!is_tag)
		{
			String send_string = activity.getApplicationContext().getString(R.string.send);
			menu_options.add(send_string);
			action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_SEND);
		
			String retag_string = activity.getApplicationContext().getString(R.string.retag);
			menu_options.add(retag_string);
			action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_RETAG);
		}

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
		FileMenuClickListener listener = new FileMenuClickListener(action_ids, callback);
		
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
			text.setText(new File(file_name).getName());
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
		
		text = (TextView) alert_dialog.findViewById(R.id.file_hashsum_value);
		if (text != null)
		{
			//
			// get & update hash sum
			//
			DBManager db_man = DBManager.getInstance();
			String hash_sum = db_man.getHashsum(file_name);
			
			if (hash_sum != null)
			{
				text.setText(hash_sum);
			}
		}
		
		text = (TextView)alert_dialog.findViewById(R.id.file_date_value);
		if (text != null)
		{
			//
			// get & update file date
			//
			DBManager db_man = DBManager.getInstance();
			String utc_file_date = db_man.getFileDate(file_name);
			
			//
			// convert date to local time
			//
			String local_time = TimeFormatUtility.convertStringToLocalTime(utc_file_date);

			//
			// update time
			//
			text.setText(local_time);
		}
		
	}
	
	/**
	 * constructs the detail dialog
	 * @param activity activity which is parent of the dialog
	 * @param file_name path of the file
	 * @return Dialog
	 */
	public static Dialog buildDetailDialogFile(ActivityGroup activity) {

		//
		// call internal builder
		//
		return buildDialogWithId(activity, R.layout.details_dialog);
	}

	/**
	 * updates the options dialog
	 * @param dialog dialog to be updated
	 * @param current_file_name file name which identifies a file currently included in the tagstore
	 * @param new_file_name file name which identifies a file to be added to the tagstore
	 * @param callback call-back class which receives call out when the user has made a choice
	 */
	public static void updateOptionsDialogFileView(Dialog dialog, String current_file_name, String new_file_name, OptionsDialogCallback callback) {
		
		//
		// convert to AlertDialog
		//
		AlertDialog alert_dialog = (AlertDialog)dialog;
		
		//
		// first find the current file name text view
		//
		TextView current_view = (TextView)alert_dialog.findViewById(R.id.file_path_current);
		if (current_view != null) 
		{
			//
			// update path
			//
			current_view.setText(current_file_name);
		}
		
		// 
		// now find the new file name text view
		//
		TextView new_view = (TextView)alert_dialog.findViewById(R.id.file_path_new);
		if (new_view != null)
		{
			//
			// update path
			//
			new_view.setText(new_file_name);
		}
		
		//
		// add click listener for all buttons
		//
		for(int id : s_ids)
		{
			//
			// get button
			//
			Button current_button = (Button)alert_dialog.findViewById(id);
			if (current_button != null)
			{
				//
				// set click listener
				//
				current_button.setOnClickListener(new OptionsDialogButtonListener(callback, alert_dialog));
			}
		}
		
		//
		// finally mark dialog as not cancelable
		//
		alert_dialog.setCancelable(false);
	}
	
	
	/**
	 * constructs the option dialog
	 * @param activity activity which is the parent of the dialog
	 * @return Dialog object
	 */
	public static Dialog buildOptionsDialogFile(ActivityGroup activity) {
		
		//
		// call internal builder
		//
		return buildDialogWithId(activity, R.layout.file_options);
	}

	public static void updateRetagDialogFile(Dialog dialog, String current_file, RetagDialogCallback callback) {
		
		//
		// cast to alert dialog
		//
		AlertDialog alert_dialog = (AlertDialog)dialog;
		
		//
		// get text view
		//
		TextView text_view = (TextView)alert_dialog.findViewById(R.id.old_tag_line);
		if (text_view != null)
		{
			//
			// acquire instance of database manager
			//
			DBManager db_man = DBManager.getInstance();
		
			//
			// get all associated tags of the current file
			//
			ArrayList<String> tags = db_man.getAssociatedTags(current_file);
		
			if (tags != null)
			{
				//
				// 	construct tag line
				//
				StringBuilder builder = new StringBuilder();
				for(String tag : tags)
				{
					//
					// FIXME: hardcoded seperator string
					//
					if (builder.length() == 0)
						builder.append(tag);
					else
						builder.append(", " + tag);
				}

				//
				// update tag line
				//
				text_view.setText(builder.toString());
			}
		}
		
		//
		// get button tag done
		//
		Button button_tag = (Button) alert_dialog.findViewById(R.id.button_tag_done);
		if (button_tag != null) {
			
			//
			// add listener
			//
			button_tag.setOnClickListener( new RetagButtonClickListener(alert_dialog, current_file, callback));
		}

		//
		// get tag text field
		//
		EditText edit_text = (EditText) alert_dialog.findViewById(R.id.tag_field);
		if (edit_text != null)
		{
			//
			// add editor action listener
			//
			edit_text.setOnEditorActionListener(new UIEditorActionListener());
			
			//
			// add text changed listener
			//
			edit_text.addTextChangedListener(new UITagTextWatcher(dialog.getContext(), false));		
		}
	}
	
	/**
	 * constructs the retag dialog
	 * @param activity activity this dialog belongs to
	 * @return Dialog object
	 */
	public static Dialog buildRetagDialogFile(ActivityGroup activity) {
		
		//
		// call internal builder
		//
		return buildDialogWithId(activity, R.layout.file_retag);
	}
	
	/**
	 * constructs a rename dialog
	 * @param activity activity this dialog belongs to
	 * @return Dialog object
	 */
	public static Dialog buildRenameDialogFile(ActivityGroup activity) {

		//
		// call internal builder
		//
		return buildDialogWithId(activity, R.layout.file_rename);
	}
	
	/**
	 * updates the rename dialog
	 * @param dialog dialog to be updated
	 * @param item_name item which is supposed to be renamed
	 * @param is_tag if the rename_file_name specifies a tag it is set to true
	 * @param cancellable if true the dialog is cancelable
	 * @param callback callback which is invoked when rename operation has been completed
	 */
	public static void updateRenameDialogFile(Dialog dialog,
			String item_name,
			boolean is_tag,
			boolean cancelable,
			RenameDialogCallback callback) {

		//
		// cast to alert dialog
		//
		AlertDialog alert_dialog = (AlertDialog)dialog;
		
		//
		// find text view for old file path text view
		//
		TextView text_view = (TextView) alert_dialog.findViewById(R.id.old_file_name);
		if (text_view != null)
		{
			//
			// update old file path
			//
			text_view.setText(item_name);
		}
		
		String name = item_name;
		if (!is_tag)
		{
			//
			// remove directory
			//
			name = new File(item_name).getName();
		}
		
		EditText edit_text = (EditText) alert_dialog.findViewById(R.id.new_file_name);
		if (edit_text != null)
		{
			//
			// set item name
			//
			edit_text.setText(name);
			
			//
			// add editor action listener
			//
			edit_text.setOnEditorActionListener(new UIEditorActionListener());
			
			//
			// add text changed listener
			//
			edit_text.addTextChangedListener(new UITagTextWatcher(dialog.getContext(), false));		
		}
		
		//
		// get rename button
		//
		Button button = (Button)alert_dialog.findViewById(R.id.button_rename);
		if (button != null)
		{
			//
			// add listener
			//
			button.setOnClickListener(new RenameDialogButtonListener(alert_dialog, item_name, is_tag, callback));
		}
		
		//
		// finally mark dialog as not cancelable
		//
		alert_dialog.setCancelable(cancelable);
	}
	
	/**
	 * constructs a tag detail dialog
	 * @param activity activity this dialog belongs to
	 * @return Dialog object
	 */
	public static Dialog buildTagDetailDialog(ActivityGroup activity) {

		//
		// call internal builder
		//
		return buildDialogWithId(activity, R.layout.tag_details);
	}
	
	public static void updateTagDetailDialog(Dialog dialog, String tag_name) {
		
		//
		// cast to alert dialog
		//
		AlertDialog alert_dialog = (AlertDialog)dialog;
		
		//
		// find tag name item
		//
		TextView text = (TextView)alert_dialog.findViewById(R.id.tag_name_value);
		if (text != null)
		{
			//
			// set text
			//
			text.setText(tag_name);
		}
		
		//
		// access database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// build pesudo tag stack
		//
		ArrayList<String> tags = new ArrayList<String>();
		tags.add(tag_name);
		
		//
		// find tag link item
		//
		text = (TextView) alert_dialog.findViewById(R.id.tag_link_value);
		if (text != null)
		{
			//
			// get associated tags
			//
			ArrayList<String> links = db_man.getLinkedTags(tags);
		
			int number_tags = 0;
			if (links != null && links.size() > 0)
				number_tags = links.size();
			
			//
			// set number of linked tags
			//
			text.setText(Integer.toString(number_tags));
		}
		
		//
		// find tag file item
		//
		text = (TextView) alert_dialog.findViewById(R.id.tag_files_value);
		if (text != null)
		{
			//
			// get number of linked files
			//
			ArrayList<String> files = db_man.getLinkedFiles(tags);
			
			int number_tags = 0;
			if (files != null && files.size() > 0)
				number_tags = files.size();
			
			//
			// set number of linked files
			//
			text.setText(Integer.toString(number_tags));
		}
	}
	
	/**
	 * builds a specific alert dialog
	 * @param activity activity this dialog belongs to
	 * @param dialog_id resource id of the dialog
	 * @return
	 */
	private static Dialog buildDialogWithId(ActivityGroup activity, int dialog_id) {
		
		//
		// get layout inflater service
		//
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//
		// construct view
		//
		View layout = inflater.inflate(dialog_id,
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
