package org.me.TagStore.interfaces;

/**
 * This class is used to inform the icon view that an item was pressed
 * @author Johannes Anderwald
 *
 */
public interface IconViewClickListenerCallback {

	/**
	 * this call back is invoked when a item is clicked
	 * @param item_index index of item which was clicked
	 */
	public abstract void onListItemClick(int item_index);
	
	/**
	 * this call-back is invoked when a long key press on the item is performed
	 * @param item_index item index of the item which was clicked
	 */
	public abstract boolean onLongListItemClick(int item_index);
	
}
