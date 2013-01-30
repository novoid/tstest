package org.me.tagstore.ui;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;

import org.me.tagstore.R;
import org.me.tagstore.core.DBManager;
import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.FileTagUtility;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.TagValidator;
import org.me.tagstore.core.TagstoreApplication;
import org.me.tagstore.core.TimeFormatUtility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class builds various dialogs when a file is selected
 * 
 */
public class FileDialogBuilder {

	public enum MENU_ITEM_ENUM {
		MENU_ITEM_DETAILS, MENU_ITEM_SEND, MENU_ITEM_DELETE, MENU_ITEM_RENAME, MENU_ITEM_RETAG, MENU_ITEM_OPEN, MENU_ITEM_OPEN_AS, MENU_ITEM_OPEN_AS_AUDIO, MENU_ITEM_OPEN_AS_VIDEO, MENU_ITEM_OPEN_AS_TEXT, MENU_ITEM_OPEN_AS_IMAGE
	};

	final static int[] s_ids = new int[] { R.id.button_ignore_current,
			R.id.button_ignore_new, R.id.button_rename_current,
			R.id.button_rename_new };
	private DBManager m_db_man;
	private FileTagUtility m_utility;
	private TimeFormatUtility m_time_format;

	public void initializeFileDialogBuilder(DBManager db_man,
			FileTagUtility utility, TimeFormatUtility time_format) {

		m_db_man = db_man;
		m_utility = utility;
		m_time_format = time_format;
	}

	public Dialog buildGeneralTagDialog(FragmentActivity m_activity_group,
			DialogFragment fragment) {

		//
		// call internal builder
		//
		return buildCommonDialog(m_activity_group, true, null, fragment);

	}

	public Dialog buildGeneralFileDialog(FragmentActivity m_activity_group,
			String item_name, DialogFragment fragment) {
		//
		// call internal builder
		//
		return buildCommonDialog(m_activity_group, false, item_name, fragment);
	}

	/**
	 * constructs a general dialog
	 * 
	 * @param m_activity_group
	 *            activity group this dialog belongs to
	 * @param item
	 *            name of the file
	 */
	private Dialog buildCommonDialog(FragmentActivity m_activity_group,
			boolean is_tag, String item_name, DialogFragment fragment) {

		//
		// construct new alert builder
		//
		AlertDialog.Builder builder = new AlertDialog.Builder(m_activity_group);

		//
		// get localized title
		//
		String title = m_activity_group.getApplicationContext().getString(
				R.string.options);

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
		if (!is_tag) {
			String openas_string = m_activity_group.getApplicationContext()
					.getString(R.string.open_as);
			menu_options.add(openas_string);
			action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS);
		}

		String details_string = m_activity_group.getApplicationContext()
				.getString(R.string.details);
		menu_options.add(details_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_DETAILS);

		String rename_string = m_activity_group.getApplicationContext()
				.getString(R.string.rename);
		menu_options.add(rename_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_RENAME);

		String delete_string = m_activity_group.getApplicationContext()
				.getString(R.string.delete);
		menu_options.add(delete_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_DELETE);

		if (!is_tag) {
			String send_string = m_activity_group.getApplicationContext()
					.getString(R.string.send);
			menu_options.add(send_string);
			action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_SEND);

			//
			// does the file already exist in the tagstore?
			//
			if (m_db_man.getFileId(item_name) != -1) {
				String retag_string = m_activity_group.getApplicationContext()
						.getString(R.string.retag);
				menu_options.add(retag_string);
				action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_RETAG);
			}
		}

		//
		// convert to string array
		//
		String[] menu_option_list = menu_options.toArray(new String[1]);

		//
		// get application object
		//
		TagstoreApplication app = (TagstoreApplication) m_activity_group.getApplication();
		
		
		//
		// create menu click listener
		//
		FileMenuClickListener listener = new FileMenuClickListener();
		listener.initFileMenuClickListener(action_ids, fragment,
				app.getEventDispatcher());

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
	 * 
	 * @param activity
	 *            activity group
	 * @param file_name
	 *            file name
	 * @return View
	 */
	public void updateDetailDialogFileView(View view,
			FragmentActivity activity, String file_name,
			final DialogFragment fragment) {

		//
		// get text field for name
		//
		TextView text = (TextView) view.findViewById(R.id.file_name_value);
		if (text != null) {
			//
			// set file name
			//

			String name = "";
			try {
				//
				// extract name
				//
				name = new File(file_name).getName();
			} catch (NullPointerException exc) {
				Logger.e("Failed to get file name from path: " + file_name);
			}

			text.setText(name);
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
		text = (TextView) view.findViewById(R.id.file_folder_value);
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
		text = (TextView) view.findViewById(R.id.file_size_value);
		if (text != null) {

			//
			// get localized bytes
			//
			String bytes = activity.getApplicationContext().getString(
					R.string.bytes);

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
		if (mime_type == null)
			mime_type = "";

		//
		// get text field for size
		//
		text = (TextView) view.findViewById(R.id.file_type_value);
		if (text != null) {
			//
			// set mime type
			//
			text.setText(mime_type);
		}

		text = (TextView) view.findViewById(R.id.file_hashsum_value);
		if (text != null) {
			//
			// get & update hash sum
			//
			String hash_sum = m_db_man.getHashsum(file_name);

			if (hash_sum != null) {
				text.setText(hash_sum);
			}
		}

		text = (TextView) view.findViewById(R.id.file_date_value);
		if (text != null) {
			//
			// get & update file date
			//
			String utc_file_date = m_db_man.getFileDate(file_name);
			if (utc_file_date != null) {
				//
				// convert date to local time
				//
				String local_time = m_time_format
						.convertStringToLocalTime(utc_file_date);

				//
				// update time
				//
				text.setText(local_time);
			}
			else
			{
				// construct date object
				Date date = new Date(file.lastModified());
				String local_time = m_time_format.convertDateToLocalTime(date);
				if (local_time != null) {
					//
					// update time
					//
					text.setText(local_time);
				}
			}
		}
	}

	/**
	 * constructs the detail dialog
	 * 
	 * @param activity
	 *            activity which is parent of the dialog
	 * @param file_name
	 *            path of the file
	 * @return Dialog
	 */
	public Dialog buildDetailDialogFile(FragmentActivity activity,
			String file_name, DialogFragment fragment) {

		//
		// construct layout
		//
		View layout = constructViewWithId(activity, R.layout.details_dialog);

		//
		// update dialog
		//
		updateDetailDialogFileView(layout, activity, file_name, fragment);

		//
		// complete builder
		//
		return constructDialogWithView(activity, layout);
	}

	/**
	 * updates the options dialog
	 * 
	 * @param dialog
	 *            dialog to be updated
	 * @param new_file_name
	 *            file name which identifies a file to be added to the tagstore
	 * @param callback
	 *            call-back class which receives call out when the user has made
	 *            a choice
	 */
	private void updateOptionsDialogFileView(View view, String new_file_name,
			DialogFragment fragment) {

		//
		// get all files which have the same name
		//
		ArrayList<String> files = m_db_man.getSimilarFilePaths(new File(
				new_file_name).getName());

		//
		// any file paths which have the same file name but different directory
		//
		if (files == null || files.isEmpty()) {
			//
			// BUG: no duplicates found
			//
			Logger.e("no duplicates found while building updateOptionsDialog: "
					+ new_file_name);
			return;
		}

		//
		// get first duplicate
		//
		String current_file_name = files.get(0);

		Logger.i("current: " + current_file_name + " new: " + new_file_name);

		//
		// first find the current file name text view
		//
		TextView current_view = (TextView) view
				.findViewById(R.id.file_path_current);
		if (current_view != null) {
			//
			// update path
			//
			current_view.setText(current_file_name);
		}

		//
		// now find the new file name text view
		//
		TextView new_view = (TextView) view.findViewById(R.id.file_path_new);
		if (new_view != null) {
			//
			// update path
			//
			new_view.setText(new_file_name);
		}
		
		//
		// get application object
		//
		TagstoreApplication app = (TagstoreApplication) fragment.getActivity().getApplication();
		
		
		//
		// add click listener for all buttons
		//
		for (int id : s_ids) {
			//
			// get button
			//
			Button current_button = (Button) view.findViewById(id);
			if (current_button != null) {
				//
				// construct optionsdialog listener
				//
				OptionsDialogButtonListener listener = new OptionsDialogButtonListener();
				listener.initializeOptionsDialogButtonListener(view, fragment,
						app.getEventDispatcher());

				//
				// set click listener
				//
				current_button.setOnClickListener(listener);
			}
		}
	}

	/**
	 * constructs the option dialog
	 * 
	 * @param fragmentActivity
	 *            activity which is the parent of the dialog
	 * @return Dialog object
	 */
	public Dialog buildOptionsDialogFile(FragmentActivity fragmentActivity,
			String new_file_name, DialogFragment fragment) {

		//
		// construct layout
		//
		View layout = constructViewWithId(fragmentActivity,
				R.layout.file_options);

		//
		// update dialog
		//
		updateOptionsDialogFileView(layout, new_file_name, fragment);

		//
		// complete builder
		//
		AlertDialog alert_dialog = (AlertDialog) constructDialogWithView(
				fragmentActivity, layout);

		//
		// finally mark dialog as not cancelable
		//
		alert_dialog.setCancelable(false);

		//
		// done
		//
		return alert_dialog;

	}

	private void updateRetagDialogFile(View view, String current_file,
			DialogFragment fragment) {

		//
		// get text view
		//
		TextView text_view = (TextView) view.findViewById(R.id.old_tag_line);
		if (text_view != null) {
			//
			// get all associated tags of the current file
			//
			ArrayList<String> tags = m_db_man.getAssociatedTags(current_file);

			if (tags != null) {
				//
				// construct tag line
				//
				StringBuilder builder = new StringBuilder();
				for (String tag : tags) {
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
		Button button_tag = (Button) view.findViewById(R.id.button_tag_done);

		//
		// get application object
		//
		TagstoreApplication app = (TagstoreApplication) fragment.getActivity().getApplication();
				
		
		//
		// get tag text field
		//
		EditText edit_text = (EditText) view.findViewById(R.id.tag_field);
		if (button_tag != null && edit_text != null) {

			//
			// create retag listener
			//
			RetagButtonClickListener listener = new RetagButtonClickListener();
			listener.initializeRetagButtonClickListener(edit_text,
					current_file, fragment, app.getEventDispatcher(),
					m_utility);

			//
			// add listener
			//
			button_tag.setOnClickListener(listener);

			//
			// add editor action listener
			//
			edit_text.setOnEditorActionListener(new UIEditorActionListener());

			//
			// construct text watcher
			//
			UITagTextWatcher watcher = new UITagTextWatcher();
			watcher.initializeUITagTextWatcher(app.getTagValidator(), app.getToastManager(), false);

			//
			// add text changed listener
			//
			edit_text.addTextChangedListener(watcher);
		}
	}

	/**
	 * constructs the retag dialog
	 * 
	 * @param activity
	 *            activity this dialog belongs to
	 * @return Dialog object
	 */
	public Dialog buildRetagDialogFile(FragmentActivity activity,
			String current_file, DialogFragment fragment) {

		//
		// construct layout
		//
		View layout = constructViewWithId(activity, R.layout.file_retag);

		//
		// update dialog
		//
		updateRetagDialogFile(layout, current_file, fragment);

		//
		// complete builder
		//
		return constructDialogWithView(activity, layout);
	}

	/**
	 * constructs a rename dialog
	 * 
	 * @param activity
	 *            activity this dialog belongs to
	 * @return Dialog object
	 */
	public Dialog buildRenameDialogFile(FragmentActivity activity,
			String item_name, boolean is_tag, boolean cancellable,
			DialogFragment fragment) {

		//
		// construct layout
		//
		View layout = constructViewWithId(activity, R.layout.file_rename);

		//
		// update details
		//
		updateRenameDialogFile(layout, item_name, is_tag, fragment);

		//
		// complete builder
		//
		AlertDialog alert_dialog = (AlertDialog) constructDialogWithView(
				activity, layout);

		//
		// finally mark dialog as not cancelable
		//
		alert_dialog.setCancelable(cancellable);

		//
		// done
		//
		return alert_dialog;
	}

	/**
	 * updates the rename dialog
	 * 
	 * @param view
	 *            view to be updated
	 * @param item_name
	 *            item which is supposed to be renamed
	 * @param is_tag
	 *            if the rename_file_name specifies a tag it is set to true
	 */
	private void updateRenameDialogFile(View view, String item_name,
			boolean is_tag, DialogFragment fragment) {

		//
		// find text view for old file path text view
		//
		TextView text_view = (TextView) view.findViewById(R.id.old_file_name);
		if (text_view != null) {
			//
			// update old file path
			//
			text_view.setText(item_name);
		}

		String name = item_name;
		if (!is_tag) {
			//
			// remove directory
			//
			name = new File(item_name).getName();
		}

		//
		// get application object
		//
		TagstoreApplication app = (TagstoreApplication) fragment.getActivity().getApplication();
		
		
		EditText edit_text = (EditText) view.findViewById(R.id.new_file_name);
		if (edit_text != null) {
			//
			// set item name
			//
			edit_text.setText(name);

			//
			// add editor action listener
			//
			edit_text.setOnEditorActionListener(new UIEditorActionListener());

			//
			// construct text watcher
			//
			UITagTextWatcher watcher = new UITagTextWatcher();
			watcher.initializeUITagTextWatcher(app.getTagValidator(), app.getToastManager(), false);

			//
			// add text changed listener
			//
			edit_text.addTextChangedListener(watcher);
		}

		
		
		//
		// get rename button
		//
		Button button = (Button) view.findViewById(R.id.button_rename);
		if (button != null && edit_text != null) {
			//
			// create rename listener
			//
			RenameDialogButtonListener listener = new RenameDialogButtonListener();
			listener.initializeRenameDialogButtonListener(edit_text, item_name,
					is_tag, fragment, app.getEventDispatcher(), m_utility,
					app.getTagValidator(), app.getToastManager());

			//
			// add listener
			//
			button.setOnClickListener(listener);
		}
	}

	public Dialog buildOpenAsDialog(FragmentActivity m_activity_group,
			String item_name, DialogFragment fragment) {
		//
		// construct new alert builder
		//
		AlertDialog.Builder builder = new AlertDialog.Builder(m_activity_group);

		//
		// get application object
		//
		TagstoreApplication app = (TagstoreApplication) fragment.getActivity().getApplication();
		
		
		//
		// get localized title
		//
		String title = m_activity_group.getApplicationContext().getString(
				R.string.open_as);

		//
		// set title
		//
		builder.setTitle(title + "...");

		//
		// construct array list for options
		//
		ArrayList<String> menu_options = new ArrayList<String>();
		ArrayList<MENU_ITEM_ENUM> action_ids = new ArrayList<MENU_ITEM_ENUM>();

		//
		// add default menu items
		//
		String text_string = m_activity_group.getApplicationContext()
				.getString(R.string.text);
		menu_options.add(text_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_TEXT);

		String audio_string = m_activity_group.getApplicationContext()
				.getString(R.string.audio);
		menu_options.add(audio_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_AUDIO);

		String video_string = m_activity_group.getApplicationContext()
				.getString(R.string.video);
		menu_options.add(video_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_VIDEO);

		String image_string = m_activity_group.getApplicationContext()
				.getString(R.string.image);
		menu_options.add(image_string);
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_OPEN_AS_IMAGE);

		//
		// convert to string array
		//
		String[] menu_option_list = menu_options.toArray(new String[1]);

		//
		// create menu click listener
		//
		FileMenuClickListener listener = new FileMenuClickListener();
		listener.initFileMenuClickListener(action_ids, fragment,
				app.getEventDispatcher());

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
	 * constructs a tag detail dialog
	 * 
	 * @param activity
	 *            activity this dialog belongs to
	 * @param fragment
	 * @return Dialog object
	 */
	public Dialog buildTagDetailDialog(FragmentActivity activity,
			String tag_name, DialogFragment fragment) {

		//
		// construct layout
		//
		View layout = constructViewWithId(activity, R.layout.tag_details);

		//
		// update details
		//
		updateTagDetailDialog(layout, tag_name, fragment);

		//
		// complete builder
		//
		return constructDialogWithView(activity, layout);

	}

	/**
	 * updates the tag detail dialog
	 * 
	 * @param view
	 *            to use for updating the dialog
	 * @param tag_name
	 *            name of the tag
	 * @param fragment
	 */
	private void updateTagDetailDialog(View view, String tag_name,
			DialogFragment fragment) {

		// alert_dialog.requestWindowFeature(Window.)

		//
		// find tag name item
		//
		TextView text = (TextView) view.findViewById(R.id.tag_name_value);
		if (text != null) {
			//
			// set text
			//
			text.setText(tag_name);
		}

		//
		// build pesudo tag stack
		//
		ArrayList<String> tags = new ArrayList<String>();
		tags.add(tag_name);

		//
		// find tag link item
		//
		text = (TextView) view.findViewById(R.id.tag_link_value);
		if (text != null) {
			//
			// get associated tags
			//
			ArrayList<String> links = m_db_man.getLinkedTags(tags);

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
		text = (TextView) view.findViewById(R.id.tag_files_value);
		if (text != null) {
			//
			// get number of linked files
			//
			ArrayList<String> files = m_db_man.getLinkedFiles(tags);

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
	 * 
	 * @param fragmentActivity
	 *            activity this dialog belongs to
	 * @param dialog_id
	 *            resource id of the dialog
	 * @return
	 */
	private View constructViewWithId(FragmentActivity fragmentActivity,
			int dialog_id) {

		//
		// get layout inflater service
		//
		LayoutInflater inflater = (LayoutInflater) fragmentActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//
		// construct view
		//
		View layout = inflater.inflate(dialog_id, null);// (ViewGroup)
														// findViewById(R.id.layout_root));

		//
		// done
		//
		return layout;
	}

	private Dialog constructDialogWithView(FragmentActivity fragmentActivity,
			View layout) {

		//
		// construct builder
		//
		AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);

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
