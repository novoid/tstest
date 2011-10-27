package org.me.TagStore.ui;

import java.util.ArrayList;

import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.ui.FileDialogBuilder.MENU_ITEM_ENUM;

import android.content.DialogInterface;

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
	private ArrayList<MENU_ITEM_ENUM> m_action_ids;
	
	/**
	 * stores the callback object
	 */
	GeneralDialogCallback m_callback;
	
	/**
	 * constructor of class FileMenuClickListener
	 * @param file_name file name of item to be launched
	 * @param action_ids array of actions
	 * @param callback 
	 */
	
	public FileMenuClickListener(ArrayList<MENU_ITEM_ENUM> action_ids, GeneralDialogCallback callback) {

		//
		// store members
		//
		m_action_ids = action_ids;
		m_callback = callback;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		//
		// perform callback
		//
		if (m_callback != null)
		{
			m_callback.processMenuFileSelection(m_action_ids.get(which));
		}				
	}
}