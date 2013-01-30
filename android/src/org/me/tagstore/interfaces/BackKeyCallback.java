package org.me.tagstore.interfaces;

/**
 * this interface is used to inform a view that a key was pressed
 *
 */
public interface BackKeyCallback {

	/**
	 * invoked when a key is pressed
	 * @param keyCode key code which was pressed
	 * @param event event code
	 * @return true if it was handled
	 */
	public abstract void backKeyPressed();
}
