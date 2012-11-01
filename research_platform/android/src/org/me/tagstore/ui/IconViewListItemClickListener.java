package org.me.tagstore.ui;

import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.Logger;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * implements click listener for the icon view
 * 
 * @author Johannes Anderwald
 * 
 */
public class IconViewListItemClickListener implements OnClickListener {

	
	/**
	 * name of item
	 */
	private final String m_item_name;
	
	/**
	 * if the item is a tag
	 */
	private final boolean m_is_tag;
	
	/**
	 * constructor of class IconvViewListItemClickListener
	 * @param listener 
	 * @param item_name name of the item
	 * @param is_tag item is tag
	 */
	public IconViewListItemClickListener(String item_name, boolean is_tag) {

		m_item_name = item_name;
		m_is_tag = is_tag;
	}

	
	public void onClick(View v) {

		Logger.i("onClick: " + m_item_name + " is_tag: " + m_is_tag);
		
		//
		// init event args
		//
		Object [] event_args = new Object[]{m_item_name, m_is_tag};
		
		//
		// dispatch event via event dispatcher
		//
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.ITEM_CLICK_EVENT, event_args);
	}
}