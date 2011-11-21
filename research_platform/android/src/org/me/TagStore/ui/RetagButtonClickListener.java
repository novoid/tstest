package org.me.TagStore.ui;

import org.me.TagStore.R;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.interfaces.RetagDialogCallback;

import android.support.v4.app.DialogFragment;
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
	private final View m_view;
	
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
	 * stores the dialog fragment
	 */
	private final DialogFragment m_fragment;
	
	/**
	 * constructor of class RetagButtonClickListener
	 * @param alert_dialog alert dialog
	 * @param current_file file which is retagged
	 * @param callback callback interface
	 */
	public RetagButtonClickListener(View view,
			String current_file, RetagDialogCallback callback, DialogFragment fragment) 
	{
		//
		// init members
		//
		m_callback = callback;
		m_view = view;
		m_file = current_file;
		m_fragment = fragment;
		
		//
		// get tag field
		//
		m_tag_field = (EditText)view.findViewById(R.id.tag_field);
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
		if (!FileTagUtility.validateTags(tag_text, m_view.getContext(), m_tag_field))
		{
			//
			// failed
			//
			return;
		}
		
		//
		// retag the file
		//
		boolean result = FileTagUtility.retagFile(m_file, tag_text, true, m_view.getContext());
		
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
		m_fragment.dismiss();
	}
}
