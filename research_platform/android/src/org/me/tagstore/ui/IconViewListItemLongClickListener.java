package org.me.tagstore.ui;

import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.Logger;
import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.os.Looper;
import android.view.View;
import android.view.View.OnLongClickListener;

/**
 * implements long click listener
 * 
 * 
 */
public class IconViewListItemLongClickListener implements OnLongClickListener {

	/**
	 * name of the item
	 */
	private String m_item_name;

	/**
	 * if item is a tag
	 */
	private boolean m_item_tag;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event_dispatcher;

	public void initIconViewListItemLongClickListener(String item_name,
			boolean item_tag, EventDispatcherInterface event_dispatcher) {
		//
		// store members
		//
		m_item_name = item_name;
		m_item_tag = item_tag;
		m_event_dispatcher = event_dispatcher;
	}

	public boolean onLongClick(View v) {

		Logger.i("onLongClick: " + m_item_name + " is_tag: " + m_item_tag);

		//
		// build event params
		//
		Object[] event_args = new Object[] { m_item_name, m_item_tag };
		Logger.i("ThreadID before signal ITEM_LONG_CLICK_EVENT: "
				+ Thread.currentThread().getId() + "LooperID:"
				+ Looper.myLooper().getThread().getId());

		//
		// dispatch event
		//
		m_event_dispatcher.signalEvent(
				EventDispatcher.EventId.ITEM_LONG_CLICK_EVENT, event_args);

		//
		// done
		//
		return true;
	}

}