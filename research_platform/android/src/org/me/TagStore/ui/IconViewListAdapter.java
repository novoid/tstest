package org.me.TagStore.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.me.TagStore.R;
import org.me.TagStore.interfaces.IconViewClickListenerCallback;

import android.app.Activity;
import android.content.Context;
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
	private final Context m_context;

	/**
	 * stores the item data to display
	 */
	private final ArrayList<HashMap<String, Object>> m_list_map;

	/*
	 * layout builder
	 */
	private final LayoutInflater m_LayoutInflater;

	/**
	 * stores the callback
	 */
	private final IconViewClickListenerCallback m_callback;

	/**
	 * constructor of class TagStoreListAdapter
	 * 
	 * @param list_map
	 *            stores the details to display
	 */

	public IconViewListAdapter(ArrayList<HashMap<String, Object>> list_map,
			IconViewClickListenerCallback callback,
			Activity activity) {

		//
		// initialize members
		//
		m_context = activity;
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
		// return number of list elements
		//
		return m_list_map.size();
	}

	@Override
	public Object getItem(int position) {

		//
		// return object
		//
		return m_list_map.get(position);
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

		if (convertView == null) {

			//
			// construct a view which will be re-used for the specific item
			//
			convertView = m_LayoutInflater.inflate(
					R.layout.tagstore_list_row_one, null);

			//
			// create new element
			//
			element = new IconViewListItemUIElements(m_context);

			//
			// initialize element
			//
			element.initializeWithView(m_callback, position, convertView);

			//
			// store element in view
			//
			convertView.setTag(element);
			
		} else {
			//
			// get ui element
			//
			element = (IconViewListItemUIElements) convertView.getTag();
			
			//
			// initialize element
			//
			element.initializeWithView(m_callback, position, convertView);
		}

		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_list_map.get(position);
			
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
		element.setItemNameAndImage(item_name, item_image);
		
		//
		// done
		//
		return convertView;
	}
}