package org.me.TagStore.ui;

import org.me.TagStore.interfaces.IconViewClickListenerCallback;

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
	 * stores call-back
	 */
	private IconViewClickListenerCallback m_callback;

	/**
	 * stores item index
	 */
	private int m_item_index;

	public IconViewListItemLongClickListener(IconViewClickListenerCallback callback,
			int item_index) {

		//
		// store members
		//
		m_callback = callback;
		m_item_index = item_index;
	}

	@Override
	public boolean onLongClick(View v) {

		//
		// initiate call back
		//
		if (m_callback != null)
		{
			return m_callback.onLongListItemClick(m_item_index);
		}
		else
		{
			//
			// event was not consumed
			//
			return false;
		}
	}

}