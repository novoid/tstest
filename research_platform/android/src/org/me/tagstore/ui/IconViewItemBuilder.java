package org.me.tagstore.ui;

import java.util.HashMap;

import org.me.tagstore.core.ConfigurationSettings;

import android.content.Context;
import android.content.SharedPreferences;

public class IconViewItemBuilder {

	/**
	 * hash map key for name of the item
	 */
	public static final String ITEM_NAME = "item_name";

	/**
	 * hash map key for icon of the item
	 */
	public static final String ITEM_ICON = "item_icon";

	/**
	 * hash map key for path of a file item
	 */
	public final static String ITEM_PATH = "item_path";

	/**
	 * adds an item to the list map
	 * 
	 * @param item_name
	 *            name of the item
	 * @param item_icon
	 *            icon of the item
	 */
	public static HashMap<String, Object> buildIconViewItem(Context context,
			String item_name, boolean is_tag) {

		//
		// create map entry
		//
		HashMap<String, Object> map_entry = new HashMap<String, Object>();

		//
		// acquire shared settings
		//
		SharedPreferences settings = context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		Integer default_icon;

		if (is_tag) {
			//
			// get default tag icon
			//
			default_icon = settings.getInt(
					ConfigurationSettings.CURRENT_LIST_TAG_ICON,
					ConfigurationSettings.DEFAULT_LIST_TAG_ICON);

			//
			// add item name
			//
			map_entry.put(ITEM_NAME, item_name);
		} else {

			//
			// TODO: get icon from file type
			//
			default_icon = settings.getInt(
					ConfigurationSettings.CURRENT_LIST_ITEM_ICON,
					ConfigurationSettings.DEFAULT_LIST_ITEM_ICON);

			//
			// add item name
			//
			map_entry.put(ITEM_PATH, item_name);

			//
			// remove path from item name
			//
			String name = item_name.substring(item_name.lastIndexOf("/") + 1);

			//
			// add item name
			//
			map_entry.put(ITEM_NAME, name);
		}

		//
		// add item icon
		//
		map_entry.put(ITEM_ICON, default_icon);

		//
		// return item
		//
		return map_entry;
	}

}
