package org.me.tagstore.ui;

import java.util.HashMap;

import org.me.tagstore.core.Logger;

import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

public class IconViewScrollListener implements OnScrollListener {

	/**
	 * stores the list adapter
	 */
	private IconViewListAdapter m_adapter;

	/**
	 * toast manager
	 */
	private ToastManager m_toast_man;

	/**
	 * constructor of class IconViewScrollListener
	 * 
	 * @param tagStoreListViewActivity
	 */
	public void initIconViewScrollListener(IconViewListAdapter list_adapter,
			ToastManager toast_man) {
		m_adapter = list_adapter;
		m_toast_man = toast_man;
	}

	/**
	 * old toast position
	 */
	int m_item_offset = 0;

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		Logger.i("onScroll: item " + firstVisibleItem + " visibleItemCount: "
				+ visibleItemCount + " totalItemCount: " + totalItemCount);

		//
		// get offset
		//
		int offset = firstVisibleItem;

		//
		// sanity check
		//
		if (offset >= m_adapter.getCount() || offset < 0) {
			//
			// index out of bounds
			//
			Logger.e("OnScrollListener::onScroll firstVisibleItem "
					+ firstVisibleItem + " item per row " + " out of bounds");
			return;
		}

		if (m_item_offset == offset) {
			//
			// superflous scroll event
			//
			return;
		}

		//
		// get map entry
		//
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map_entry = (HashMap<String, Object>) m_adapter
				.getItem(offset);
		if (map_entry == null) {
			Logger.e("no map entry");
			return;
		}

		//
		// get item name
		//
		String item_name = (String) map_entry
				.get(IconViewItemBuilder.ITEM_NAME);

		//
		// trim name
		//
		item_name = item_name.substring(0, 1).toUpperCase();

		//
		// display toast
		//
		m_toast_man.displayToastWithStringAndGravity(item_name,
				Toast.LENGTH_SHORT, Gravity.RIGHT | Gravity.CENTER_VERTICAL,
				10, 0);

		//
		// store item offset
		//
		m_item_offset = offset;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}