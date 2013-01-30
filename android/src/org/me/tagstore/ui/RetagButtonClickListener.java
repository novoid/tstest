package org.me.tagstore.ui;

import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.FileTagUtility;
import org.me.tagstore.core.Logger;
import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * This class implements the re-tagging of files
 * 
 */
public class RetagButtonClickListener implements OnClickListener {
	/**
	 * stores the file which is re-tagged
	 */
	private String m_file;

	/**
	 * stores the edit field
	 */
	private EditText m_tag_field;

	/**
	 * stores the dialog fragment
	 */
	private DialogFragment m_fragment;

	private EventDispatcherInterface m_event_dispatcher;

	private FileTagUtility m_utility;

	public void initializeRetagButtonClickListener(EditText edit_text,
			String current_file, DialogFragment fragment,
			EventDispatcherInterface event_dispatcher, FileTagUtility utility) {
		//
		// init members
		//
		m_file = current_file;
		m_fragment = fragment;
		m_event_dispatcher = event_dispatcher;
		m_tag_field = edit_text;
		m_utility = utility;
	}

	public void onClick(View v) {

		//
		// is there an edit field
		//
		if (m_tag_field == null) {
			//
			// bug bug bug
			//
			Logger.e("Error: RetagButtonClickListener no tag field found");
			return;
		}

		if (m_file == null) {

			//
			// bug bug bug
			//
			Logger.e("RetagButtonClickListener no file provided");
			return;

		}

		//
		// get text of tag field
		//
		String tag_text = m_tag_field.getText().toString();

		//
		// validate the tags now
		//
		if (!m_utility.validateTags(tag_text, m_tag_field)) {
			//
			// failed
			//
			return;
		}

		//
		// retag the file
		//
		boolean result = m_utility.retagFile(m_file, tag_text, true);
		Logger.e("retagFile: " + result);
		if (result) {
			//
			// prepare event args
			//
			Object[] event_args = new Object[] { m_file, tag_text };

			//
			// signal event
			//
			m_event_dispatcher.signalEvent(
					EventDispatcher.EventId.FILE_RETAG_EVENT, event_args);
		}

		if (m_fragment != null) {
			//
			// dismiss the dialog
			//
			m_fragment.dismiss();
		}
	}
}
