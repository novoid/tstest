package org.me.tagstore.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * helper class to display toasts
 * 
 */
public class ToastManager {

	/**
	 * reference to toast
	 */
	private Toast m_toast;

	/**
	 * reference to context
	 */
	private Context m_context;

	/**
	 * constructor of toast manager
	 * 
	 * @param context
	 *            application context
	 */
	public void initializeToastManager(Context context) {

		//
		// store context
		//
		m_context = context;

		//
		// build toast
		//
		m_toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
	}

	/**
	 * displays a toast with a given string resource id
	 * 
	 * @param resource_id
	 *            resource id of string
	 * @param duration
	 *            duration of the toast
	 */
	public void displayToastWithString(int resource_id, int duration) {

		//
		// get translated resource
		//
		String text = m_context.getString(resource_id);
		if (text != null) {
			//
			// show toast
			//
			displayToastWithString(text, duration);
		}

	}

	/**
	 * displays a short toast with the resource id
	 * 
	 * @param resource_id
	 */
	public void displayToastWithString(int resource_id) {

		displayToastWithString(resource_id, Toast.LENGTH_SHORT);
	}

	/**
	 * cancels a toast
	 */
	public void cancelToast() {

		//
		// cancels the toast
		//
		m_toast.cancel();
	}

	/**
	 * duration of the toast
	 * 
	 * @param msg
	 *            message of toast
	 * @param duration
	 *            duration of toast
	 */
	public void displayToastWithString(String msg, int duration) {

		displayToastWithStringAndGravity(msg, duration, Gravity.CENTER, 0, 0);
	}

	/**
	 * displays a short toast with the given message
	 * 
	 * @param msg
	 */
	public void displayToastWithString(String msg) {

		displayToastWithString(msg, Toast.LENGTH_SHORT);
	}

	/**
	 * displays a toast with given resource id which is a format string
	 * 
	 * @param format_id
	 *            resource string with format id which takes as an argument a
	 *            string
	 * @param arg1
	 *            string argument to feed the resource string
	 * @param duration
	 *            duration of the toast
	 */
	public void displayToastWithFormat(int format_id, String arg1, int duration) {

		//
		// get format string
		//
		String format_string = m_context.getString(format_id);
		if (format_string != null) {
			//
			// now format it
			//
			String msg = String.format(format_string, arg1);

			//
			// now display it
			//
			displayToastWithString(msg, duration);
		}
	}

	/**
	 * displays a short toast with format string
	 * 
	 * @param format_id
	 *            id resource id of format string
	 * @param arg1
	 *            argument for toast
	 */
	public void displayToastWithFormat(int format_id, String arg1) {

		displayToastWithFormat(format_id, arg1, Toast.LENGTH_SHORT);
	}

	/**
	 * displays a toast with the specified string and gravity
	 * 
	 * @param msg
	 *            message of the toast
	 * @param duration
	 *            duration of the toast
	 * @param gravity
	 *            gravity of the toast
	 * @param x_offset
	 *            x offset from gravity
	 * @param y_offset
	 *            y offset from gravity
	 */
	public void displayToastWithStringAndGravity(String msg, int duration,
			int gravity, int x_offset, int y_offset) {

		//
		// update toast
		//
		m_toast.setText(msg);

		//
		// set gravity
		//
		m_toast.setGravity(gravity, x_offset, y_offset);

		//
		// set duration
		//
		m_toast.setDuration(duration);

		//
		// show the toast
		//
		m_toast.show();
	}

	/**
	 * returns the internal toast object
	 * 
	 * @return
	 */
	public Toast getToast() {
		return m_toast;
	}

}
