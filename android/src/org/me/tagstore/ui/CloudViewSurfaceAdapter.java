package org.me.tagstore.ui;

import java.util.ArrayList;

import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.TagStackManager;
import org.me.tagstore.interfaces.CloudViewTouchListenerCallback;
import org.me.tagstore.interfaces.EventDispatcherInterface;
import org.me.tagstore.ui.CloudViewTagAdapter.CloudTag;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class CloudViewSurfaceAdapter extends SurfaceView implements
		CloudViewTouchListenerCallback {

	/**
	 * cloud view adapter
	 */
	private CloudViewTagAdapter m_tag_adapter;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event_dispatcher;

	/**
	 * tag stack manager
	 */
	private TagStackManager m_tag_stack;

	/**
	 * target rectangle
	 */
	private Rect m_target_rect;

	/**
	 * source view port rectangle
	 */
	private Rect m_source_rect;

	/**
	 * view height
	 */
	private int m_height;

	/**
	 * paint object
	 */
	private Paint m_paint;

	/**
	 * constructor of class TabPageIndicator
	 * 
	 * @param context
	 */
	public CloudViewSurfaceAdapter(Context context) {
		this(context, null);

	}

	public CloudViewSurfaceAdapter(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CloudViewSurfaceAdapter(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		Logger.i("CloudViewSurfaceAdapter constructor");

		//
		// change background
		//
		changeBG(0, 0, 0);

		//
		// construct new paint
		//
		m_paint = new Paint();
	}

	public void initCloudViewSurfaceAdapter(CloudViewTagAdapter tag_adapter,
			EventDispatcherInterface event_dispatcher, TagStackManager tag_stack) {

		// store tag adapter
		m_tag_adapter = tag_adapter;
		m_event_dispatcher = event_dispatcher;
		m_tag_stack = tag_stack;
	}

	public void refreshView() {
		
		if (m_tag_adapter != null)
			m_tag_adapter.initializeView();
		
		this.invalidate();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.me.tagstore.ui.CloudViewTouchListenerCallback#buttonPressed(float,
	 * float, long)
	 */
	public void buttonPressed(float current_x, float current_y, long time) {

		int x = (int) current_x;
		int y = (int) current_y;

		// get intersecting tags
		ArrayList<CloudTag> tags = m_tag_adapter.getIntersectingTags(x, y,
				m_source_rect.top);
		if (tags == null)
			return;

		Logger.i("buttonPressed: " + current_x + " y: " + current_y + " time: "
				+ time + " tags: " + tags.size());

		if (tags.size() == 1) {
			//
			// get tag
			//
			CloudTag tag = tags.get(0);

			if (time >= CloudViewConstants.CONTEXT_MENU_DELAY) {
				//
				// prepare event params
				//
				Object[] event_args = new Object[] { tag.m_tag, tag.m_is_tag };

				//
				// invoke callback
				//
				m_event_dispatcher.signalEvent(
						EventDispatcher.EventId.ITEM_LONG_CLICK_EVENT,
						event_args);
				return;
			}

			//
			// is it a tag
			//
			if (tag.m_is_tag) {

				//
				// add tag to tag stack
				//
				m_tag_stack.addTag(tag.m_tag);

				//
				// refresh view
				//
				m_tag_adapter.refreshView();

				//
				// invalidate view
				//
				postInvalidate();

			}

			//
			// prepare event params
			//
			Object[] event_args = new Object[] { tag.m_tag, tag.m_is_tag };

			//
			// invoke callback
			//
			m_event_dispatcher.signalEvent(
					EventDispatcher.EventId.ITEM_CLICK_EVENT, event_args);
		}
	}

	@Override
	public void onSizeChanged(int width, int height, int oldwidth, int oldheight) {

		//
		// call base implementation
		//
		super.onSizeChanged(width, height, oldwidth, oldheight);

		//
		// store height
		//
		m_height = height;

		//
		// recreate rectangles
		//
		m_target_rect = new Rect(0, 0, width, m_height);
		m_source_rect = new Rect(0, 0, width, m_height);

		//
		// draws the bitmap
		//
		m_tag_adapter.drawBitmap(width, height);

		//
		// invalidate
		//
		postInvalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.me.tagstore.ui.CloudViewTouchListenerCallback#onPointerMove(int,
	 * float, float)
	 */
	public void onPointerMove(int pointer_index, float diff_x, float diffy) {

		if (pointer_index != 0) {
			//
			// not primary pointer, ignore
			//
			return;
		}

		//
		// get current maximum height of the screen box manager
		//
		int max_height = m_tag_adapter.getScreenBoxManagerHeight();

		int diff_y = (int) diffy;

		if (diff_y > 0) {
			//
			// scroll down operation
			//
			if (m_source_rect.bottom + Math.abs(diff_y) <= max_height) {
				m_source_rect.top += Math.min(Math.abs(diff_y), max_height
						- m_source_rect.bottom);
				m_source_rect.bottom += Math.min(Math.abs(diff_y), max_height
						- m_source_rect.bottom);

				//
				// force repaint
				//
				postInvalidate();
			}
		} else {
			//
			// scroll up operation
			//
			if (m_source_rect.top + diff_y >= 0) {
				//
				// don't scroll up beyond beginning
				//
				m_source_rect.top = (int) Math.max(m_source_rect.top + diff_y,
						0.0);
				m_source_rect.bottom = m_source_rect.top + m_height;

				//
				// force repaint
				//
				postInvalidate();
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		//
		// call base implementation
		//
		super.onDraw(canvas);

		//
		// draw bitmap
		//
		m_tag_adapter.drawBitmap(canvas, m_paint, m_source_rect, m_target_rect);
	}

	public void changeBG(int r, int g, int b) {

		System.out.println("change bg: " + r + "," + g + "," + b);

		setBackgroundColor(Color.rgb(r, g, b));

		invalidate();

	}

}