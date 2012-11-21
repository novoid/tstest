package org.me.tagstore.ui;

import java.io.File;
import java.util.Set;

import org.me.tagstore.R;
import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.FileTagUtility;
import org.me.tagstore.core.TagValidator;
import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.annotation.TargetApi;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * This class is used to verify the entered file name of rename dialog. If the
 * file name is unique, the file is renamed and the relevant database entries
 * are updated. Afterwards an event is dispatched.
 * 
 */
public class RenameDialogButtonListener implements OnClickListener {
	/**
	 * stores the dialog
	 */
	private EditText m_edit_text;

	/**
	 * stores the current item name / path
	 */
	private String m_item;

	/**
	 * stores the setting if its an tag
	 */
	private boolean m_is_tag;

	/**
	 * stores the dialog fragment
	 */
	private DialogFragment m_fragment;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event_dispatcher;

	private FileTagUtility m_utility;

	private TagValidator m_validator;

	private ToastManager m_toast_man;

	public void initializeRenameDialogButtonListener(EditText edit_text,
			String item, boolean is_tag, DialogFragment fragment,
			EventDispatcherInterface event_dispatcher, FileTagUtility utility,
			TagValidator validator, ToastManager toast_man) {

		//
		// init members
		//
		m_edit_text = edit_text;
		m_item = item;
		m_is_tag = is_tag;
		m_fragment = fragment;
		m_event_dispatcher = event_dispatcher;
		m_utility = utility;
		m_validator = validator;
		m_toast_man = toast_man;
	}

	/**
	 * this function marks a specific item
	 * 
	 * @param edit_text
	 *            edit text
	 * @param new_item
	 *            item containing the specific string
	 * @param sub_item
	 *            item to be marked
	 */
	private void markString(EditText edit_text, String new_item, String sub_item) {

		//
		// get position
		//
		int index = new_item.indexOf(sub_item);
		if (index < 0) {
			//
			// sub item not found
			//
			return;
		}

		//
		// mark item
		//
		edit_text.setSelection(index, index + sub_item.length());

	}

	/**
	 * handles rename of the tag
	 * 
	 * @param new_item_name
	 *            new tag name
	 * @param edit_text
	 */
	private void handleTag(String new_item_name, EditText edit_text) {

		//
		// lets split the tags first
		//
		Set<String> tags = m_utility.splitTagText(new_item_name);

		if (tags.isEmpty()) {
			//
			// one tag minimum
			//
			m_toast_man.displayToastWithString(R.string.one_tag_minimum);
			return;
		}

		//
		// iterate through all items
		//
		for (String current_tag : tags) {
			//
			// check if that tag already exists
			//
			boolean tag_id = m_utility.isTagExisting(current_tag);
			if (tag_id) {
				//
				// tag already exists
				//
				m_toast_man.displayToastWithString(R.string.error_tag_exists);
				markString(edit_text, new_item_name, current_tag);
				return;
			}

			//
			// check if the tag is a reserved keyword
			//
			if (m_validator.isReservedKeyword(current_tag)) {
				//
				// not allowed
				//
				m_toast_man.displayToastWithString(R.string.reserved_keyword);
				markString(edit_text, new_item_name, current_tag);
				return;
			}

			//
			// check if the file name contains reserved characters
			//
			if (m_validator.containsReservedCharacters(current_tag)) {
				//
				// not allowed
				//
				m_toast_man
						.displayToastWithString(R.string.reserved_character_tag);
				return;
			}
		}

		if (tags.size() > 1) {
			//
			// can't rename one tag to multiple tags
			//
			m_toast_man.displayToastWithString(R.string.error_one_tag_maximum);
			return;
		}

		//
		// get first tag
		//
		String new_tag = tags.iterator().next();

		//
		// now rename the tag
		//
		boolean renamed = m_utility.renameTag(m_item, new_tag);

		//
		// check if it worked
		//
		if (renamed) {
			//
			// prepare event args
			//
			Object[] event_args = new Object[] { m_item, new_tag };

			//
			// signal event
			//
			m_event_dispatcher.signalEvent(
					EventDispatcher.EventId.TAG_RENAMED_EVENT, event_args);
		} else {
			//
			// failed to rename file SHIT
			//
			m_toast_man.displayToastWithString(R.string.error_failed_rename);
		}

		if (m_fragment != null) {

			//
			// dismiss the dialog
			//
			m_fragment.dismiss();
		}

	}

	/**
	 * handles rename of the file
	 * 
	 * @param new_file_name
	 *            new file name
	 */
	@TargetApi(9)
	private void handleFile(String new_file_name) {
		if (new_file_name.length() == 0) {
			//
			// file name is empty
			//
			m_toast_man.displayToastWithString(R.string.error_no_file_name);
			return;
		}

		//
		// construct file object
		//
		File file = new File(m_item);

		if (new_file_name.compareTo(file.getName()) == 0) {
			//
			// new file name equals old name
			//
			m_toast_man.displayToastWithString(R.string.error_same_file_name);
			return;
		}

		//
		// check if the file name is a reserved keyword
		//
		if (m_validator.isReservedKeyword(new_file_name)) {
			//
			// not allowed
			//
			m_toast_man
					.displayToastWithString(R.string.reserved_keyword_file_name);
			return;
		}

		//
		// check if the file name contains reserved characters
		//
		if (m_validator.containsReservedCharacters(new_file_name)) {
			//
			// not allowed
			//
			m_toast_man
					.displayToastWithString(R.string.reserved_character_file_name);
			return;
		}

		//
		// check if there is already a file with that name entered
		//
		if (m_utility.isFilenameAlreadyTaken(new_file_name)) {
			//
			// file name is not unique
			//
			m_toast_man
					.displayToastWithString(R.string.error_file_name_not_unique);
			return;
		}

		//
		// check first if the card is accessible
		//
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			//
			// card is not accessible
			//
			m_toast_man
					.displayToastWithString(R.string.error_media_not_mounted);
			return;
		}

		//
		// final check if a file with the new name already exists
		//
		String new_file_path = file.getParent() + File.separator
				+ new_file_name;
		File new_file_obj = new File(new_file_path);
		if (new_file_obj.exists()) {
			//
			// the name is already taken
			//
			m_toast_man
					.displayToastWithString(R.string.error_file_name_present);
			return;
		}

		//
		// now rename the file
		//
		boolean renamed = m_utility.renameFile(file.getPath(),
				new_file_obj.getPath());
		if (renamed) {
			//
			// prepare event args
			//
			Object[] event_args = new Object[] { file.getPath(),
					new_file_obj.getPath() };

			//
			// signal event
			//
			m_event_dispatcher.signalEvent(
					EventDispatcher.EventId.FILE_RENAMED_EVENT, event_args);
		} else {
			//
			// failed to rename file
			//
			m_toast_man.displayToastWithString(R.string.error_failed_rename);
		}

		if (m_fragment != null) {

			//
			// dismiss the dialog
			//
			m_fragment.dismiss();
		}

	}

	public void onClick(View v) {

		//
		// get new item name
		//
		String new_item_name = m_edit_text.getText().toString();

		if (m_is_tag)
			handleTag(new_item_name, m_edit_text);
		else
			handleFile(new_item_name);

	}

}