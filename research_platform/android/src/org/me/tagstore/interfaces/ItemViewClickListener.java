package org.me.tagstore.interfaces;

/**
 * This class is used to inform the view that an item was clicked. 
 * @author Johannes Anderwald
 *
 */
public interface ItemViewClickListener {

	/**
	 * this function is called when a item was clicked
	 * @param item_name name / path of the item
	 * @param is_tag true if it is a tag
	 */
	public abstract void onListItemClick(String item_name, boolean is_tag);
	
	/**
	 * this function is called when an item is pressed for a long time
	 * @param item_name name / path of the item
	 * @param is_tag true if it is a tag
	 */
	public abstract void onLongListItemClick(String item_name, boolean is_tag);
}
