package org.me.tagstore.ui;

import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.Logger;
import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * implements click listener for the icon view
 * 
 * 
 */
public class IconViewListItemClickListener implements OnClickListener {

	/**
	 * name of item
	 */
	private String m_item_name;

	/**
	 * if the item is a tag
	 */
	private boolean m_is_tag;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event_dispatcher;

	public void initIconViewListItemClickListener(String item_name,
			boolean is_tag, EventDispatcherInterface event_dispatcher) {

		m_item_name = item_name;
		m_is_tag = is_tag;
		m_event_dispatcher = event_dispatcher;
	}

	public void onClick(View v) {

		Logger.i("onClick: " + m_item_name + " is_tag: " + m_is_tag);

		//
		// init event args
		//
		Object[] event_args = new Object[] { m_item_name, m_is_tag };

		//
		// dispatch event via event dispatcher
		//
		m_event_dispatcher.signalEvent(
				EventDispatcher.EventId.ITEM_CLICK_EVENT, event_args);
	}
}