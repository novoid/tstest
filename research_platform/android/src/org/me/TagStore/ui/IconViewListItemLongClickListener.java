package org.me.TagStore.ui;

import org.me.TagStore.core.EventDispatcher;
import org.me.TagStore.core.Logger;

import android.os.Looper;
import android.view.View;
import android.view.View.OnLongClickListener;

/**
 * implements long click listener
 * 
 * @author Johannes Anderwald
 * 
 */
public class IconViewListItemLongClickListener implements OnLongClickListener {

	/**
	 * name of the item
	 */
	private final String m_item_name;
	
	/**
	 * if item is a tag
	 */
	private final boolean m_item_tag;

	/**
	 * constructor of class IconvViewListItemLongClickListener
	 * @param m_listener 
	 * @param item_name name of the item
	 * @param item_tag if the item is a tag
	 */
	public IconViewListItemLongClickListener(String item_name, boolean item_tag)
	{
		//
		// store members
		//
		m_item_name = item_name;
		m_item_tag = item_tag;
	}

	@Override
	public boolean onLongClick(View v) {

		Logger.i("onLongClick: " + m_item_name + " is_tag: " + m_item_tag);
		
		//
		// build event params
		//
		Object [] event_args = new Object[] {m_item_name, m_item_tag};
		Logger.i("ThreadID before signal ITEM_LONG_CLICK_EVENT: " + Thread.currentThread().getId() + "LooperID:" +
		Looper.myLooper().getThread().getId());
		
		//
		// dispatch event
		//
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.ITEM_LONG_CLICK_EVENT, event_args);
		
		//
		// done
		//
		return true;
	}

}