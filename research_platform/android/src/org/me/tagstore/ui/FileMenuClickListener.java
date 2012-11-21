package org.me.tagstore.ui;

import java.util.ArrayList;

import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.interfaces.EventDispatcherInterface;
import org.me.tagstore.ui.FileDialogBuilder.MENU_ITEM_ENUM;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

/**
 * Implements a helper class which invokes a callback when a menu item of dialog
 * is clicked
 * 
 */
public class FileMenuClickListener implements DialogInterface.OnClickListener {

	/**
	 * stores the menu item actions
	 */
	private ArrayList<MENU_ITEM_ENUM> m_action_ids;

	/**
	 * dialog fragment
	 */
	private DialogFragment m_fragment;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event;

	public void initFileMenuClickListener(ArrayList<MENU_ITEM_ENUM> action_ids,
			DialogFragment fragment, EventDispatcherInterface event) {
		//
		// store members
		//
		m_action_ids = action_ids;
		m_fragment = fragment;
		m_event = event;
	}

	public void onClick(DialogInterface dialog, int which) {

		//
		// get action id
		//
		if (which < 0 || which >= m_action_ids.size())
			return;

		MENU_ITEM_ENUM selection = m_action_ids.get(which);

		//
		// prepare event args
		//
		Object[] event_args = new Object[] { selection };

		//
		// dispatch event
		//
		m_event.signalEvent(EventDispatcher.EventId.ITEM_MENU_EVENT, event_args);

		if (m_fragment != null) {
			//
			// dismiss dialog fragment
			//
			m_fragment.dismiss();
		}
	}
}