package org.me.tagstore.ui;

import java.util.ArrayList;

import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.ui.FileDialogBuilder.MENU_ITEM_ENUM;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

/**
 * Implements a helper class which invokes a callback when a menu item of dialog is clicked
 * @author Johannes Anderwald
 *
 */
public class FileMenuClickListener implements
		DialogInterface.OnClickListener {

	/**
	 * stores the menu item actions
	 */
	private final ArrayList<MENU_ITEM_ENUM> m_action_ids;
	
	/**
	 * dialog fragment
	 */
	private final DialogFragment m_fragment;
	
	/**
	 * constructor of class FileMenuClickListener
	 * @param file_name file name of item to be launched
	 * @param action_ids array of actions
	 * @param callback 
	 */
	
	public FileMenuClickListener(ArrayList<MENU_ITEM_ENUM> action_ids, DialogFragment fragment) {

		//
		// store members
		//
		m_action_ids = action_ids;
		m_fragment = fragment;
	}

	
	public void onClick(DialogInterface dialog, int which) {
		
		//
		// get action id
		//
		MENU_ITEM_ENUM selection = m_action_ids.get(which);
		
		//
		// prepare event args
		//
		Object[] event_args = new Object[] {selection};
		
		//
		// dispatch event
		//
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.ITEM_MENU_EVENT, event_args);
		
		//
		// dismiss dialog fragment
		//
		m_fragment.dismiss();
	}
}