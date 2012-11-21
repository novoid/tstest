package org.me.tagstore.ui;

import java.util.ArrayList;

import org.me.tagstore.core.Logger;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * This class is used to manage the free space on the screen. It divides the
 * free space into rectangles which can be consumed by tags
 * 
 */
public class CloudViewScreenBoxManager {
	/**
	 * width of the screen
	 */
	private int m_width;

	/**
	 * max font size
	 */
	private int m_max_font_size;

	/**
	 * current height
	 */
	private int m_current_height;

	/**
	 * holds free space rectangles
	 */
	private ArrayList<Rect> m_free_rect;

	/**
	 * constructor of class ScreenBox Manager
	 * 
	 * @param width
	 *            width of the screen
	 */
	public void initCloudViewScreenBoxManager(int width, int max_font_size) {
		m_width = width;
		m_max_font_size = max_font_size;
		m_free_rect = new ArrayList<Rect>();

		//
		// construct rectangle with max font size
		//
		constructRectangle(0, m_max_font_size);
	}

	/**
	 * constructs new free rectangles which can be used for cloud tags
	 * 
	 * @param offset
	 *            height offset to start from
	 * @param height
	 *            increment offset
	 * @param box_size
	 *            determines the box size in which the new space is split into
	 */
	private void constructRectangle(int offset, int box_size) {

		//
		// create bounding rectangle
		//
		Rect rect = new Rect();
		rect.left = 0;
		rect.right = m_width;
		rect.top = offset;
		rect.bottom = offset + box_size;

		//
		// last height
		//
		m_current_height = rect.bottom;

		//
		// add the rectangle
		//
		m_free_rect.add(rect);
	}

	/**
	 * returns the current height
	 * 
	 * @return
	 */
	public int getCurrentHeight() {
		return m_current_height;
	}

	public void paintFreeRect(Canvas canvas, Paint paint) {
		for (Rect rect : m_free_rect) {
			canvas.drawRect(rect, paint);
		}

	}

	/**
	 * returns a rectangle which has the minimum dimension given
	 * 
	 * @param min_width
	 *            minimum width of the rectangle
	 * @param min_height
	 *            minimum height of the rectangle
	 * @return Rect object when successful
	 */
	public Rect getRectangleWithDimension(int min_width, int min_height) {

		//
		// find a free rectangle first
		//
		Rect free_rect = findFreeRectangle(min_width, min_height);
		if (free_rect == null) {
			//
			// increase space
			//
			constructRectangle(m_current_height, m_max_font_size);

			//
			// retry search
			//
			free_rect = findFreeRectangle(min_width, min_height);
		}

		if (free_rect != null) {
			//
			// copy old rectangle
			//
			Rect new_rect1 = new Rect(free_rect);

			//
			// move left border to the end of free rect
			//
			new_rect1.left = free_rect.left + min_width;

			//
			// adjust size
			//
			free_rect.right = free_rect.left + min_width;
			free_rect.bottom = free_rect.top + min_height;

			//
			// get old index
			//
			int old_index = m_free_rect.indexOf(free_rect);

			//
			// remove old rectangle
			//
			m_free_rect.remove(free_rect);

			//
			// add new rectangles to the free list
			//
			m_free_rect.add(old_index, new_rect1);

			//
			// done
			//
			return free_rect;
		}

		//
		// no free rectangle found
		//
		Logger.e("getRectangleWithDimension> bug detected");
		return null;
	}

	/**
	 * scans the list of free rectangle to find a rectangle which has the given
	 * dimensions
	 * 
	 * @param min_width
	 * @param min_height
	 * @return
	 */
	private Rect findFreeRectangle(int min_width, int min_height) {

		int min_diff = Integer.MAX_VALUE;
		Rect cur_rect = null;

		for (Rect rect : m_free_rect) {
			//
			// get width
			//
			int current_width = Math.abs(rect.width());

			if (current_width >= min_width) {
				//
				// calculate difference
				//
				int cur_diff = (current_width - min_width);
				if (cur_diff < min_diff) {
					cur_rect = rect;
					min_diff = cur_diff;
				}
			}
		}

		//
		// return rectangle which has the best fit
		//
		return cur_rect;
	}
}