package org.me.tagstore.ui;

import org.me.tagstore.R;
import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class is used to forward the calls of options dialog to the registered
 * callback
 * 
 */
public class OptionsDialogButtonListener implements OnClickListener {
	/**
	 * stores the view
	 */
	private View m_view;

	/**
	 * stores the fragment which holds the dialog
	 */
	private DialogFragment m_fragment;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event_dispatcher;

	/**
	 * constructor of class OptionsDialogButtonListener
	 * 
	 * @param callback
	 *            callback which is invoked when a button of the options dialog
	 *            has been pressed
	 * @param view
	 *            view containing the buttons
	 */
	public void initializeOptionsDialogButtonListener(View view,
			DialogFragment fragment, EventDispatcherInterface event_dispatcher) {
		m_view = view;
		m_fragment = fragment;
		m_event_dispatcher = event_dispatcher;
	}

	public void onClick(View v) {

		//
		// cast to button
		//
		Button button = (Button) v;

		//
		// check which button has been pressed
		//
		if (button.getId() == R.id.button_ignore_current) {
			performCallback(R.id.file_path_current, true);
		} else if (button.getId() == R.id.button_rename_current) {
			performCallback(R.id.file_path_current, false);
		} else if (button.getId() == R.id.button_ignore_new) {
			performCallback(R.id.file_path_new, true);
		} else if (button.getId() == R.id.button_rename_new) {
			performCallback(R.id.file_path_new, false);
		}

		//
		// dismiss the dialog
		//
		m_fragment.dismiss();
	}

	/**
	 * invokes the callback
	 * 
	 * @param id
	 *            resource id of text view whose text is gathered for the call
	 *            back
	 * @param ignore
	 *            if the ignore button was pressed
	 */
	private void performCallback(int id, boolean ignore) {

		//
		// get text field
		//
		TextView file_name = (TextView) m_view.findViewById(id);
		if (file_name != null) {
			//
			// get text
			//
			String file_path = (String) file_name.getText();

			//
			// prepare event args
			//
			Object[] event_args = new Object[] { file_path, ignore };

			//
			// signal event
			//
			m_event_dispatcher.signalEvent(
					EventDispatcher.EventId.ITEM_CONFLICT_EVENT, event_args);
		}
	}
}