package org.me.tagstore.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.me.tagstore.R;
import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * this class implements the list adapter which is used to display the items in
 * the list view
 * 
 */
public class IconViewListAdapter extends BaseAdapter {

	/**
	 * number of items to display per row
	 */
	private Context m_context;

	/**
	 * stores the item data to display
	 */
	private ArrayList<HashMap<String, Object>> m_list_map;

	/*
	 * layout builder
	 */
	private LayoutInflater m_LayoutInflater;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event_dispatcher;

	/**
	 * constructor of class TagStoreListAdapter
	 * 
	 * @param list_map
	 *            stores the details to display
	 */

	public void initIconViewListAdapter(
			ArrayList<HashMap<String, Object>> list_map, Context context,
			EventDispatcherInterface event_dispatcher) {

		//
		// initialize members
		//
		m_context = context;
		m_list_map = list_map;
		m_event_dispatcher = event_dispatcher;

		//
		// construct layout inflater from context
		//
		m_LayoutInflater = LayoutInflater.from(context);
	}

	public int getCount() {

		//
		// return number of list elements
		//
		return m_list_map.size();
	}

	public Object getItem(int position) {

		//
		// return object
		//
		return m_list_map.get(position);
	}

	public long getItemId(int position) {

		//
		// position is id
		//
		return position;
	}

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
			element = new IconViewListItemUIElements();

			//
			// initialize element
			//
			element.initializeIconViewListItemUIElements(convertView,
					m_context, m_event_dispatcher);

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
			element.initializeIconViewListItemUIElements(convertView,
					m_context, m_event_dispatcher);
		}

		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_list_map.get(position);

		//
		// get item name
		//
		String display_name = (String) map_entry
				.get(IconViewItemBuilder.ITEM_NAME);

		String item_name = display_name;

		//
		// is the item a tag
		//
		boolean item_tag = !map_entry
				.containsKey(IconViewItemBuilder.ITEM_PATH);
		if (!item_tag) {
			//
			// get real path
			//
			item_name = (String) map_entry.get(IconViewItemBuilder.ITEM_PATH);
		}

		//
		// get item icon
		//
		Integer item_image = (Integer) map_entry
				.get(IconViewItemBuilder.ITEM_ICON);

		//
		// set element item
		//
		element.setItemNameAndImage(display_name, item_name, item_image,
				item_tag);

		//
		// done
		//
		return convertView;
	}
}