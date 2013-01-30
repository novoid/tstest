package org.me.tagstore.interfaces;

public interface CloudViewTouchListenerCallback {

	/**
	 * This is the call-back function for the CloudTouchlistener
	 * @param current_x x coordinate of the key press
	 * @param current_y y coordinate of the key press
	 * @param time
	 */
	public abstract void buttonPressed(float current_x, float current_y,
			long time);

	/**
	 * callback when a pointer has been moved
	 * @param pointer_index index of the pointer
	 * @param diff_x diff in x coordinates
	 * @param diff_y diff in y coordinates
	 */
	public abstract void onPointerMove(int pointer_index, float diff_x,
			float diffy);

}