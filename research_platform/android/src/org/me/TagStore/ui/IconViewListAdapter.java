package org.me.TagStore.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.me.TagStore.R;
import org.me.TagStore.interfaces.IconViewClickListenerCallback;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * this class implements the list adapter which is used to display the items
 * in the list view
 * 
 * @author Johannes Anderwald
 */
public class IconViewListAdapter extends BaseAdapter {

	/**
	 * number of items to display per row
	 */
	private int m_num_items_per_row;

	/**
	 * stores the item data to display
	 */
	private ArrayList<HashMap<String, Object>> m_list_map;

	/*
	 * layout builder
	 */
	private LayoutInflater m_LayoutInflater;

	/**
	 * stores the callback
	 */
	IconViewClickListenerCallback m_callback;

	/**
	 * constructor of class TagStoreListAdapter
	 * 
	 * @param num_items_per_row
	 *            number of items per row
	 * @param list_map
	 *            stores the details to display
	 */

	public IconViewListAdapter(int num_items_per_row,
			ArrayList<HashMap<String, Object>> list_map,
			IconViewClickListenerCallback callback,
			Activity activity) {

		//
		// initialize members
		//
		m_num_items_per_row = num_items_per_row;
		m_list_map = list_map;
		m_callback = callback;

		//
		// construct layout inflater from context
		//
		m_LayoutInflater = LayoutInflater.from(activity);
	}

	@Override
	public int getCount() {

		//
		// return number of list elements divided by number of items
		//
		int list_size = m_list_map.size();

		if (list_size == 0) {
			//
			// no items
			//
			return 0;
		}

		//
		// get number of items
		//
		int count = list_size / m_num_items_per_row;

		if (list_size % m_num_items_per_row != 0) {
			//
			// there is unfinished row
			//
			count++;
		}

		return count;
	}

	@Override
	public Object getItem(int position) {

		//
		// return object
		//
		int offset = position * m_num_items_per_row;

		//
		// return object at that position
		//
		return m_list_map.get(offset);
	}

	@Override
	public long getItemId(int position) {

		//
		// position is id
		//
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		IconViewListItemUIElements element = null;

		//
		// calculate number of elements present in this row
		//
		int offset = position * m_num_items_per_row;

		//
		// calculate item length
		//
		int num_items = Math.min(m_list_map.size() - offset,
				m_num_items_per_row);

		if (convertView == null) {

			int[] tag_list_view_rows = new int[] {
					R.layout.tagstore_list_row_one,
					R.layout.tagstore_list_row_two,
					R.layout.tagstore_list_row_three,
					R.layout.tagstore_list_row_four };

			//
			// construct a view which will be re-used for the specific item
			//
			convertView = m_LayoutInflater.inflate(
					tag_list_view_rows[m_num_items_per_row - 1], null);

			//
			// create new element
			//
			element = new IconViewListItemUIElements(num_items);

			//
			// initialize element
			//
			element.initializeWithView(m_callback, offset, convertView,
					m_num_items_per_row);

			//
			// store element in view
			//
			convertView.setTag(element);
		} else {
			//
			// get ui element
			//
			element = (IconViewListItemUIElements) convertView.getTag();
		}

		//
		// initialize view
		//
		for (int index = 0; index < num_items; index++) {
			//
			// get map entry
			//
			HashMap<String, Object> map_entry = m_list_map.get(offset
					+ index);

			//
			// get item name
			//
			String item_name = (String) map_entry
					.get(IconViewItemBuilder.ITEM_NAME);

			//
			// get item icon
			//
			Integer item_image = (Integer) map_entry
					.get(IconViewItemBuilder.ITEM_ICON);

			//
			// set element item
			//
			element.setItemNameAndImage(index, item_name, item_image);
		}

		//
		// done
		//
		return convertView;
	}
}