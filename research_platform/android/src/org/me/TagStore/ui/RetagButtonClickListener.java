package org.me.TagStore.ui;

import org.me.TagStore.R;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.interfaces.RetagDialogCallback;

import android.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * This class implements the re-tagging of files
 * @author Johannes Anderwald
 *
 */
public class RetagButtonClickListener  implements OnClickListener
{
	/**
	 * stores the dialog
	 */
	private final AlertDialog m_dialog;
	
	/**
	 * stores the file which is re-tagged
	 */
	private final String m_file;
	
	/**
	 * stores the callback
	 */
	private final RetagDialogCallback m_callback;

	/**
	 * stores the edit field
	 */
	private final EditText m_tag_field;
	

	/**
	 * constructor of class RetagButtonClickListener
	 * @param alert_dialog alert dialog
	 * @param current_file file which is retagged
	 * @param callback callback interface
	 */
	public RetagButtonClickListener(AlertDialog alert_dialog,
			String current_file, RetagDialogCallback callback) 
	{
		//
		// init members
		//
		m_callback = callback;
		m_dialog = alert_dialog;
		m_file = current_file;
		
		//
		// get tag field
		//
		m_tag_field = (EditText)alert_dialog.findViewById(R.id.tag_field);
	}

	@Override
	public void onClick(View v) {

		//
		// is there an edit field
		//
		if (m_tag_field == null)
		{
			//
			// bug bug bug
			//
			Logger.e("Error: RetagButtonClickListener no tag field found");
			return;
		}
		
		//
		// get text of tag field
		//
		String tag_text = m_tag_field.getText().toString();
		
		//
		// validate the tags now
		//
		if (!FileTagUtility.validateTags(tag_text, m_dialog.getContext(), m_tag_field))
		{
			//
			// failed
			//
			return;
		}
		
		//
		// instantiate database manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// remove file from tag store
		//
		db_man.removeFile(m_file, false, true);
		
		//
		// HACK: mark file as pending
		//
		db_man.addPendingFile(m_file);
		
		//
		// re-add them to the store
		//
		boolean result = FileTagUtility.addFile(m_file, tag_text, m_dialog.getContext());
		
		if (result)
		{
			//
			// is there a call-back registered
			//
			m_callback.retaggedFile(m_file, tag_text);
		}
		
		//
		// dismiss the dialog
		//
		m_dialog.dismiss();
	}
}
