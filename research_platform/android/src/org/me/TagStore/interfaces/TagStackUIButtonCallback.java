package org.me.TagStore.interfaces;

/**
 * This callback is used to inform the a view that one of tag stack buttons have been clicked
 * @author Johannes Anderwald
 *
 */
public interface TagStackUIButtonCallback {

	/**
	 * informs the view that a tag button has been clicked
	 * @param tag
	 */
	public abstract void tagButtonClicked(String tag);
	
}
