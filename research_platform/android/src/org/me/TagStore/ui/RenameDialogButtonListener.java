package org.me.TagStore.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.me.TagStore.R;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TagValidator;
import org.me.TagStore.interfaces.RenameDialogCallback;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This class is used to verify the entered file name of rename dialog. If the file name is unique, the file
 * is renamed and the relevant database entries are updated. Afterwards then the registered call back is invoked
 * @author Johannes Anderwald
 *
 */
public class RenameDialogButtonListener implements OnClickListener
{
	/**
	 * stores the dialog
	 */
	private AlertDialog m_dialog;
	
	/**
	 * stores the context
	 */
	private Context m_ctx;
	
	/**
	 * stores toast
	 */
	private Toast m_toast;
	
	/**
	 * stores the current item name / path
	 */
	private String m_item;
	
	/**
	 * stores the setting if its an tag
	 */
	private final boolean m_is_tag;
	
	/**
	 * stores the call back
	 */
	private RenameDialogCallback m_callback;
	
	/**
	 * constructor of class RenameDialogButtonListener
	 * @param dialog parent dialog
	 * @param file file name
	 * @param callback callback to be invoked
	 */
	public RenameDialogButtonListener(AlertDialog dialog, String item, boolean is_tag, RenameDialogCallback callback) {
		
		
		//
		// init members
		//
		m_dialog = dialog;
		m_item = item;
		m_callback = callback;
		m_toast = null;
		m_is_tag = is_tag;
		m_ctx = m_dialog.getContext();
	}
	
	
	/**
	 * displays the toast
	 * @param resource_id string resource id to be shown as the text
	 */
	private void displayToast(int resource_id) {
		
		//
		// get translated resource
		//
		String text = m_ctx.getString(resource_id);
		
		if (m_toast == null)
		{
			//
			// construct toast
			//
			m_toast = Toast.makeText(m_ctx, text, Toast.LENGTH_SHORT);
		}
		else
		{
			//
			// update toast
			//
			m_toast.setText(text);
		}
		
		//
		// show the toast
		//
		m_toast.show();
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
		// acquire database manager instance
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// lets split the tags first
		//
		Set<String> tags = FileTagUtility.splitTagText(new_item_name);
		
		if (tags.isEmpty())
		{
			//
			// one tag minimum
			//
			displayToast(R.string.one_tag_minimum);
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
			long tag_id = db_man.getTagId(current_tag);
			if (tag_id != -1)
			{
				//
				// tag already exists
				// 
				displayToast(R.string.error_tag_exists);
				markString(edit_text, new_item_name, current_tag);
				return;
			}
			
			//
			// check if the file name is a reserved keyword
			//
			if (TagValidator.isReservedKeyword(current_tag))
			{
				//
				// not allowed
				//
				displayToast(R.string.reserved_keyword);
				markString(edit_text, new_item_name, current_tag);
				return;
			}
		}

		if (tags.size() > 1)
		{
			//
			// can't rename one tag to multiple tags
			//
			displayToast(R.string.error_one_tag_maximum);
			return;
		}
		
		//
		// get first tag
		//
		String new_tag = tags.iterator().next();
		
		//
		// now rename the tag
		//
		boolean renamed = db_man.renameTag(m_item, new_tag);
		
		//
		// check if it worked
		//
		if (renamed)
		{
			//
			// invoke call back
			//
			if (m_callback != null)
			{
				m_callback.renamedTag(m_item, new_tag);
			}
			
			//
			// dismiss the dialog
			//
			m_dialog.dismiss();
		}
		else
		{
			//
			// failed to rename file SHIT
			//
			displayToast(R.string.error_failed_rename);
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
			displayToast(R.string.error_no_file_name);
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
			displayToast(R.string.error_same_file_name);
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
			displayToast(R.string.reserved_keyword_file_name);
			return;
		}
		
		//
		// check if there is already a file with that name entered
		//
		DBManager db_man = DBManager.getInstance();
		ArrayList<String> same_files = db_man.getSimilarFilePaths(new_file_name);
		if (same_files != null)
		{
			//
			// file name is not unique
			//
			displayToast(R.string.error_file_name_not_unique);
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
			displayToast(R.string.error_file_name_present);
			return;
		}
		
		//
		// now update the database
		//
		db_man.renameFile(file.getPath(), new_file_obj.getPath());
		
		//
		// now really rename the file
		//
		boolean renamed = file.renameTo(new_file_obj);
		if (renamed)
		{
			//
			// now update the database
			//
			db_man.renameFile(file.getPath(), new_file_obj.getPath());
			
			
			//
			// invoke call back
			//
			if (m_callback != null)
			{
				m_callback.renamedFile(file.getPath(), new_file_obj.getPath());
			}
			
			//
			// dismiss the dialog
			//
			m_dialog.dismiss();
		}
		else
		{
			//
			// failed to rename file SHIT
			//
			displayToast(R.string.error_failed_rename);
		}
	}
	
	@Override
	public void onClick(View v) {

		//
		// first get edit text
		//
		EditText edit_text = (EditText)m_dialog.findViewById(R.id.new_file_name);
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