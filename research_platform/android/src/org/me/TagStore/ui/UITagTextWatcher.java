package org.me.TagStore.ui;

import org.me.TagStore.R;
import org.me.TagStore.core.TagValidator;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;


/**
 * This class is used to check if the current entered tagline confirms to the restrictions of tags. It checks for disallowed characters and in case
 * it finds them removes them. Afterwards a notification is displayed
 * @author Johannes Anderwald
 *
 */
public class UITagTextWatcher implements TextWatcher {

	/**
	 * stores the context
	 */
	private final Context m_context;
	
	/**
	 * stores the setting if it is a tag
	 */
	private final boolean m_is_tag;
	
	/**
	 * constructor of class UITagTextWatcher
	 * @param context which is used to construct notifications
	 */
	public UITagTextWatcher(Context context, boolean is_tag) {
		
		//
		// init members
		//
		m_context = context;
		m_is_tag = is_tag;
	}
	
	
	@Override
	public void afterTextChanged(Editable s) {
		
		//
		// convert to string
		//
		String tag_text = s.toString();
		
		//
		// check if it contains reserved characters
		//
		if (TagValidator.containsReservedCharacters(tag_text))
		{
			if (m_is_tag)
			{
				//
				// reserved character error message for tag
				//
				ToastManager.getInstance().displayToastWithString(R.string.reserved_character_tag);
			}
			else
			{
				//
				// reserved character error message for file name
				//
				ToastManager.getInstance().displayToastWithString(R.string.reserved_character_file_name);
			}
			
			//
			// clear the old text
			//
			s.clear();
			
			//
			// append the 'cleaned' text
			//
			s.append(TagValidator.removeReservedCharacters(tag_text));
			
			//
			// done
			//
			return;
		}
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
