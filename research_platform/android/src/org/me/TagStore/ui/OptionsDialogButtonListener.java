package org.me.TagStore.ui;

import org.me.TagStore.R;
import org.me.TagStore.interfaces.OptionsDialogCallback;

import android.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class is used to forward the calls of options dialog to the registered callback
 * @author Johannes Anderwald
 *
 */
public class OptionsDialogButtonListener implements OnClickListener
{
	/**
	 * stores the dialog
	 */
	private AlertDialog m_dialog;
	
	/**
	 * stores the callback
	 */
	private OptionsDialogCallback m_callback;

	/**
	 * constructor of class OptionsDialogButtonListener
	 * @param callback callback which is invoked when a button of the options dialog has been pressed
	 * @param dialog dialog containing the buttons
	 */
	public OptionsDialogButtonListener(OptionsDialogCallback callback, AlertDialog dialog) {
		m_callback = callback;
		m_dialog = dialog;
	}
	
	@Override
	public void onClick(View v) {

		//
		// cast to button
		//
		Button button = (Button)v;
		
		//
		// check which button has been pressed
		//
		if (button.getId() == R.id.button_ignore_current)
		{
			performCallback(R.id.file_path_current, true);
		}
		else if (button.getId() == R.id.button_rename_current)
		{
			performCallback(R.id.file_path_current, false);
		}
		else if (button.getId() == R.id.button_ignore_new)
		{
			performCallback(R.id.file_path_new, true);
		}
		else if (button.getId() == R.id.button_rename_new)
		{
			performCallback(R.id.file_path_new, false);
		}
		
		//
		// dismiss the dialog
		//
		m_dialog.dismiss();
	}
	
	/**
	 * invokes the callback
	 * @param id resource id of text view whose text is gathered for the call back
	 * @param ignore if the ignore button was pressed
	 */
	private void performCallback(int id, boolean ignore) {
		
		//
		// get text field
		//
		TextView file_name = (TextView)m_dialog.findViewById(id);
		if (file_name != null)
		{
			//
			// get text
			//
			String file_path = (String)file_name.getText();
			
			if (m_callback != null)
			{
				//
				// invoke the callback
				//
				m_callback.processOptionsDialogCommand(file_path, ignore);
			}
		}
	}
}