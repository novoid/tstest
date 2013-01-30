package org.me.tagstore.interfaces;

/**
 * This interface is used to deliver the title to the TabPageIndicator.
 *
 */
public interface TabPageIndicatorCallback {

	/**
	 * returns the title of the tab with that index
	 * @param index index of that tab
	 * @return String
	 */
	public abstract String getTitle(int index);
}
