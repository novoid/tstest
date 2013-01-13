package org.me.tagstore.ui;

import org.me.tagstore.R;
import org.me.tagstore.core.TagValidator;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * This class is used to check if the current entered tagline confirms to the
 * restrictions of tags. It checks for disallowed characters and in case it
 * finds them removes them. Afterwards a notification is displayed
 */
public class UITagTextWatcher implements TextWatcher {

	/**
	 * stores the setting if it is a tag
	 */
	private boolean m_is_tag;

	private ToastManager m_toast;

	private TagValidator m_validator;

	/**
	 * constructor of class UITagTextWatcher
	 * 
	 * @param context
	 *            which is used to construct notifications
	 */
	public void initializeUITagTextWatcher(TagValidator validator, ToastManager manager, boolean is_tag) {

		//
		// init members
		//
		m_is_tag = is_tag;
		m_validator = validator;
		m_toast = manager;
	}

	public void afterTextChanged(Editable s) {

		//
		// convert to string
		//
		String tag_text = s.toString();

		//
		// check if it contains reserved characters
		//
		if (m_validator.containsReservedCharacters(tag_text)) {
			if (m_is_tag && m_toast != null) {
				//
				// reserved character error message for tag
				//
				m_toast.displayToastWithString(
						R.string.reserved_character_tag);
			} else if (m_toast != null) {
				//
				// reserved character error message for file name
				//
				m_toast.displayToastWithString(
						R.string.reserved_character_file_name);
			}

			//
			// clear the old text
			//
			s.clear();

			//
			// append the 'cleaned' text
			//
			s.append(m_validator.removeReservedCharacters(tag_text));

			//
			// done
			//
			return;
		}

	}

	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
