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

	/**
	 * stores the setting if a toast should be displayed
	 */
	private boolean m_display_toast = true;

	/**
	 * constructor of class UITagTextWatcher
	 * 
	 * @param context
	 *            which is used to construct notifications
	 */
	public void initializeUITagTextWatcher(boolean is_tag, boolean display_toast) {

		//
		// init members
		//
		m_is_tag = is_tag;
	}

	public void afterTextChanged(Editable s) {

		//
		// convert to string
		//
		String tag_text = s.toString();

		//
		// construct tag validator
		//
		TagValidator validator = new TagValidator();

		//
		// check if it contains reserved characters
		//
		if (validator.containsReservedCharacters(tag_text)) {
			if (m_is_tag && m_display_toast) {
				//
				// reserved character error message for tag
				//
				ToastManager.getInstance().displayToastWithString(
						R.string.reserved_character_tag);
			} else if (m_display_toast) {
				//
				// reserved character error message for file name
				//
				ToastManager.getInstance().displayToastWithString(
						R.string.reserved_character_file_name);
			}

			//
			// clear the old text
			//
			s.clear();

			//
			// append the 'cleaned' text
			//
			s.append(validator.removeReservedCharacters(tag_text));

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
