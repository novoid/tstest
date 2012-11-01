package org.me.tagstore.ui;

import java.io.File;
import java.util.Set;

import org.me.tagstore.R;
import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.FileTagUtility;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.TagValidator;

import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * This class is used to verify the entered file name of rename dialog. If the file name is unique, the file
 * is renamed and the relevant database entries are updated. Afterwards an event is dispatched.
 * @author Johannes Anderwald
 *
 */
public class RenameDialogButtonListener implements OnClickListener
{
	/**
	 * stores the dialog
	 */
	private final View m_view;
	
	/**
	 * stores the current item name / path
	 */
	private final String m_item;
	
	/**
	 * stores the setting if its an tag
	 */
	private final boolean m_is_tag;
	
	/**
	 * stores the dialog fragment
	 */
	private final DialogFragment m_fragment;
	
	/**
	 * constructor of class RenameDialogButtonListener
	 * @param dialog parent dialog
	 * @param file file name
	 * @param callback callback to be invoked
	 */
	public RenameDialogButtonListener(View view, String item, boolean is_tag, DialogFragment fragment) {
		
		
		//
		// init members
		//
		m_view = view;
		m_item = item;
		m_is_tag = is_tag;
		m_fragment = fragment;
	}
	
	/**
	 * this function marks a specific item
	 * @param edit_text edit text
	 * @param new_item item containing the specific string
	 * @param sub_item item to be marked
	 */
	private void markString(EditText edit_text, String new_item, String sub_item) {
		
		//
		// get position
		//
		int index = new_item.indexOf(sub_item);
		if (index < 0)
		{
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
	 * @param new_item_name new tag name
	 * @param edit_text 
	 */
	private void handleTag(String new_item_name, EditText edit_text) {
		
		//
		// lets split the tags first
		//
		Set<String> tags = FileTagUtility.splitTagText(new_item_name);
		
		if (tags.isEmpty())
		{
			//
			// one tag minimum
			//
			ToastManager.getInstance().displayToastWithString(R.string.one_tag_minimum);
			return;
		}
		
		
		//
		// iterate through all items
		//
		for(String current_tag : tags)
		{
			//
			// check if that tag already exists
			//
			boolean tag_id = FileTagUtility.isTagExisting(current_tag);
			if (tag_id)
			{
				//
				// tag already exists
				// 
				ToastManager.getInstance().displayToastWithString(R.string.error_tag_exists);
				markString(edit_text, new_item_name, current_tag);
				return;
			}
			
			//
			// check if the tag is a reserved keyword
			//
			if (TagValidator.isReservedKeyword(current_tag))
			{
				//
				// not allowed
				//
				ToastManager.getInstance().displayToastWithString(R.string.reserved_keyword);
				markString(edit_text, new_item_name, current_tag);
				return;
			}
			
			//
			// check if the file name contains reserved characters
			//
			if (TagValidator.containsReservedCharacters(current_tag))
			{
				//
				// not allowed
				//
				ToastManager.getInstance().displayToastWithString(R.string.reserved_character_tag);
				return;			
			}
		}

		if (tags.size() > 1)
		{
			//
			// can't rename one tag to multiple tags
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_one_tag_maximum);
			return;
		}
		
		//
		// get first tag
		//
		String new_tag = tags.iterator().next();
		
		//
		// now rename the tag
		//
		boolean renamed = FileTagUtility.renameTag(m_item, new_tag);
		
		//
		// check if it worked
		//
		if (renamed)
		{
			//
			// prepare event args
			//
			Object [] event_args = new Object[] {m_item, new_tag};
			
			//
			// signal event
			//
			EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.TAG_RENAMED_EVENT, event_args);
			
			//
			// dismiss the dialog
			//
			m_fragment.dismiss();
		}
		else
		{
			//
			// failed to rename file SHIT
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_failed_rename);
			
			//
			// dismiss dialog
			//
			m_fragment.dismiss();
		}
	}
	/**
	 * handles rename of the file
	 * @param new_file_name new file name
	 */
	private void handleFile(String new_file_name)
	{
		if (new_file_name.isEmpty())
		{
			//
			// file name is empty
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_no_file_name);
			return;
		}
		
		//
		// construct file object
		//
		File file = new File(m_item);
		
		if (new_file_name.compareTo(file.getName()) == 0)
		{
			//
			// new file name equals old name
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_same_file_name);
			return;
		}
		
		//
		// check if the file name is a reserved keyword
		//
		if (TagValidator.isReservedKeyword(new_file_name))
		{
			//
			// not allowed
			//
			ToastManager.getInstance().displayToastWithString(R.string.reserved_keyword_file_name);
			return;
		}
		
		//
		// check if the file name contains reserved characters
		//
		if (TagValidator.containsReservedCharacters(new_file_name))
		{
			//
			// not allowed
			//
			ToastManager.getInstance().displayToastWithString(R.string.reserved_character_file_name);
			return;			
		}
		
		
		//
		// check if there is already a file with that name entered
		//
		if (FileTagUtility.isFilenameAlreadyTaken(new_file_name))
		{
			//
			// file name is not unique
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_file_name_not_unique);
			return;
		}

		//
		// check first if the card is accessible
		//
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state))
		{
			//
			// card is not accessible
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_media_not_mounted);
			return;
		}
				
		//
		// final check if a file with the new name already exists
		//
		String new_file_path = file.getParent() + File.separator + new_file_name;
		File new_file_obj = new File(new_file_path);
		if (new_file_obj.exists())
		{
			//
			// the name is already taken
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_file_name_present);
			return;
		}
		
		//
		// now rename the file
		//
		boolean renamed = FileTagUtility.renameFile(file.getPath(), new_file_obj.getPath());
		if (renamed)
		{
			//
			// prepare event args
			//
			Object [] event_args = new Object[] {file.getPath(), new_file_obj.getPath()};
			
			//
			// signal event
			//
			EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.FILE_RENAMED_EVENT, event_args);
			
			//
			// dismiss the dialog
			//
			m_fragment.dismiss();
		}
		else
		{
			//
			// failed to rename file
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_failed_rename);
			
			//
			// dismiss dialog
			//
			m_fragment.dismiss();
		}
	}
	
	
	public void onClick(View v) {

		//
		// first get edit text
		//
		EditText edit_text = (EditText)m_view.findViewById(R.id.new_file_name);
		if (edit_text == null)
		{
			//
			// internal error
			//
			Logger.e("Error: failed to find R.id.new_file_name in rename dialog");
			return;
		}
		
		//
		// get new item name
		//
		String new_item_name = edit_text.getText().toString();

		if (m_is_tag)
			handleTag(new_item_name, edit_text);
		else
			handleFile(new_item_name);
		
	}
	
}