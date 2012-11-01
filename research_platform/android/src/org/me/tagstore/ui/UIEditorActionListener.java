package org.me.tagstore.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * This class implements the UIEditorActionListener. It is used to collapse the keyboard when enter is pressed
 * @author Johannes Anderwald
 *
 */
public class UIEditorActionListener implements OnEditorActionListener {

	
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		if (event != null) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				//
				// collapse keyboard
				//
				InputMethodManager imm = (InputMethodManager) v
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

				//
				// event is consumed
				//
				return true;
			}
		}

		//
		// event is not consumed
		//
		return false;
	}
}
