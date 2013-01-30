package org.me.tagstore.core;

import android.util.Log;

/**
 * This class is used to send messages to the log output. Use i method for
 * informal messages, d for debugging messages and e for error messages
 * 
 */
public class Logger {

	/**
	 * log tag
	 */
	public final static String LOGGER_TAG = "tagstore";

	/**
	 * logs an informal message
	 * 
	 * @param msg
	 *            to be logged
	 */
	public static void i(String msg) {

		Log.i(LOGGER_TAG, msg);
	}

	/**
	 * logs a debugging message
	 * 
	 * @param msg
	 *            to be logged
	 */
	public static void d(String msg) {
		Log.d(LOGGER_TAG, msg);
	}

	/**
	 * logs an error message
	 * 
	 * @param msg
	 *            to be logged
	 */
	public static void e(String msg) {
		Log.e(LOGGER_TAG, msg);
	}
}
