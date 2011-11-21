package org.me.TagStore.ui;

import org.me.TagStore.interfaces.IconViewClickListenerCallback;

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
	 * stores call-back
	 */
	private final IconViewClickListenerCallback m_callback;

	/**
	 * stores item index
	 */
	private final int m_item_index;

	/**
	 * constructor of class IconViewListItemClickListener
	 * @param callback call-back which is invoked when a item is clicked
	 * @param item_index item index which is passed to the call-back
	 */
	public IconViewListItemClickListener(IconViewClickListenerCallback callback,
			int item_index) {

		//
		// store members
		//
		m_callback = callback;
		m_item_index = item_index;
	}

	@Override
	public void onClick(View v) {

		//
		// initiate call back
		//
		if (m_callback != null)
		{
			m_callback.onListItemClick(m_item_index);
		}
	}
}