package org.me.TagStore.ui;

import java.util.HashMap;

import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

public class IconViewScrollListener implements OnScrollListener {

	/**
	 * stores the list adapter
	 */
	private final IconViewListAdapter m_adapter;

	/**
	 * stores the context
	 */
	private final Context m_context;
	
	/**
	 * constructor of class IconViewScrollListener
	 * @param tagStoreListViewActivity
	 */
	public IconViewScrollListener(IconViewListAdapter list_adapter, Context context) 
	{
		m_adapter = list_adapter;
		m_context = context;
	}

	/**
	 * old toast position
	 */
	int m_item_offset = 0;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		Logger.i("onScroll: item " + firstVisibleItem + " visibleItemCount: " + visibleItemCount + " totalItemCount: " + totalItemCount);

		//
		// acquire shared settings
		//
		SharedPreferences settings = m_context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get number per row
		//
		int num_items_per_row = settings.getInt(
				ConfigurationSettings.NUMBER_OF_ITEMS_PER_ROW,
				ConfigurationSettings.DEFAULT_ITEMS_PER_ROW);

		//
		// get offset
		//
		int offset = firstVisibleItem;

		//
		// sanity check
		//
		if (offset >= m_adapter.getCount()) {
			//
			// index out of bounds
			//
			Logger.e("OnScrollListener::onScroll firstVisibleItem "
					+ firstVisibleItem + " item per row "
					+ num_items_per_row + " map size: " + num_items_per_row
					+ " out of bounds");
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
		HashMap<String, Object> map_entry = (HashMap<String, Object>) m_adapter.getItem(offset);
		if (map_entry == null) {
			Logger.e("no map entry");
			return;
		}

		//
		// get item name
		//
		String item_name = (String) map_entry.get(IconViewItemBuilder.ITEM_NAME);

		//
		// trim name
		//
		item_name = item_name.substring(0, 1).toUpperCase();

		//
		// display toast
		//
		ToastManager.getInstance().displayToastWithStringAndGravity(item_name, Toast.LENGTH_SHORT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 10, 0);

		//
		// store item offset
		//
		m_item_offset = offset;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}