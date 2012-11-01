package org.me.TagStore.ui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.me.TagStore.core.Logger;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class CloudViewTouchListener implements OnTouchListener {

	private class Pointer {
		public float m_x;
		public float m_y;
		public float m_current_x;
		public float m_current_y;
		public long m_time;
		public boolean m_moved;
		private int m_id;
	};

	/**
	 * stores list of pointers on the screen
	 */
	private ArrayList<Pointer> m_pointers;

	/**
	 * stores cloud view
	 */
	private CloudViewSurfaceAdapter m_view;
	
	/**
	 * timer object
	 */
	private Timer m_timer;
	
	/**
	 * constructor of class CloudTouchListener
	 * 
	 * @param view
	 * @param m_tag_stack
	 */
	public CloudViewTouchListener(CloudViewSurfaceAdapter view) {

		//
		// construct array list storing Pointer class
		//
		m_pointers = new ArrayList<Pointer>();

		//
		// store view for handling notifications
		//
		m_view = view;
		
		//
		// construct new timer
		//
		m_timer = new Timer();
	}

	/**
	 * calculates the distance between two points
	 * 
	 * @param x1
	 *            x coordinate of first point
	 * @param x2
	 *            x coordinate of second point
	 * @param y1
	 *            y coordinate of first point
	 * @param y2
	 *            y coordinate of second point
	 * @return distance between two points
	 */
	private double getDistance(float x1, float x2, float y1, float y2) {
		float xdist = Math.abs(x1 - x2);
		float ydist = Math.abs(y1 - y2);

		double y_square = Math.pow(xdist, 2.0);
		double x_square = Math.pow(ydist, 2.0);

		return Math.sqrt(y_square + x_square);

	}

	
	public boolean onTouch(View v, MotionEvent event) {

		//
		// get action cmd
		//
		int action = event.getAction();

		//
		// or the action
		//
		action = action & MotionEvent.ACTION_MASK;

		//
		// pointer id
		//
		int pointer_id = (event.getAction() >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);

		if (action == MotionEvent.ACTION_DOWN) {
			//
			// first pointer entered game
			//
			Pointer pointer = new Pointer();
			pointer.m_x = pointer.m_current_x = event.getX(pointer_id);
			pointer.m_y = pointer.m_current_y = event.getY(pointer_id);
			pointer.m_id = pointer_id;
			pointer.m_time = System.currentTimeMillis();
			pointer.m_moved = false;
			
			Logger.e("m_time:" + pointer.m_time);
			
			//
			// add to pointer list
			//
			m_pointers.add(pointer);

			//
			// initialize timer
			//
			m_timer.schedule(new PointerTimerTask(), CloudViewConstants.CONTEXT_MENU_DELAY);

			Logger.i("onTouch> primary pointer down  ID: " + pointer_id
					+ " pointer index: "
					+ event.findPointerIndex(pointer_id) + " X: "
					+ event.getX(pointer_id) + " Y: "
					+ event.getY(pointer_id));
		} else if (action == MotionEvent.ACTION_POINTER_DOWN) {
			//
			// another pointer in the game
			//
			Pointer pointer = new Pointer();
			pointer.m_x = pointer.m_current_x = event.getX(pointer_id);
			pointer.m_y = pointer.m_current_y = event.getY(pointer_id);
			pointer.m_id = pointer_id;
			pointer.m_moved = false;

			//
			// add to pointer list
			//
			m_pointers.add(pointer);

			Logger.i("onTouch> pointer down  ID: " + pointer_id
					+ " pointer index: "
					+ event.findPointerIndex(pointer_id) + " X: "
					+ event.getX(pointer_id) + " Y: "
					+ event.getY(pointer_id));
		}

		if (action == MotionEvent.ACTION_UP) {
			//
			// check if two pointers were present
			//
			if (m_pointers.size() == 2) {
				//
				// check if it is either zoom in / out gesture
				//
				Pointer p1 = m_pointers.get(0);
				Pointer p2 = m_pointers.get(1);

				//
				// calculate original distance
				//
				double distance1 = getDistance(p1.m_x, p2.m_x, p1.m_y,
						p2.m_y);

				//
				// calculate new distance
				//
				double distance2 = getDistance(p1.m_current_x,
						p2.m_current_x, p1.m_current_y, p2.m_current_y);

				//
				// is distance smaller
				//
				if (distance2 < distance1) {
					//
					// zoom out gesture
					//
					Logger.i("estimating zoom out> original distance: "
							+ distance1 + " new distance " + distance2);
				} else {
					//
					// zoom in gesture
					//
					Logger.i("estimating zoom in> original distance: "
							+ distance1 + " new distance " + distance2);
				}

				//
				// FIXME: handle zoom
				//
			} else if (m_pointers.size() == 1) {
				//
				// single button press
				//
				Pointer p1 = m_pointers.get(0);
				Logger.e("m_time:" + p1.m_time + " now: " + System.currentTimeMillis() + " diff: " + (System.currentTimeMillis() - p1.m_time));
				
				//
				// check if the mouse has been moved
				//
				if (p1.m_moved == false)
				{
					//
					// move has not been moved
					//
					m_view.buttonPressed(p1.m_current_x, p1.m_current_y, System.currentTimeMillis() - p1.m_time);
				}
			}

			//
			// clear pointer list as list pointer has been removed
			//
			m_pointers.clear();

			Logger.i("onTouch> primary pointer up  ID: " + pointer_id
					+ " pointer index: "
					+ event.findPointerIndex(pointer_id) + " X: "
					+ event.getX(pointer_id) + " Y: "
					+ event.getY(pointer_id));
		}

		if (action == MotionEvent.ACTION_POINTER_UP) {
			
			//
			// secondary pointer up
			//
			Logger.i("onTouch> pointer up  ID: " + pointer_id
					+ " pointer index: "
					+ event.findPointerIndex(pointer_id) + " X: "
					+ event.getX(pointer_id) + " Y: "
					+ event.getY(pointer_id));
		}

		if (action == MotionEvent.ACTION_MOVE) {
			
			for (Pointer pointer : m_pointers) {
				//
				// get pointer index
				//
				int pointer_index = event.findPointerIndex(pointer.m_id);

				if (pointer_index < 0) {
					//
					// pointer index not valid
					//
					continue;
				}

				//
				// get current x&y
				//
				float current_x = event.getX(pointer_index);
				float current_y = event.getY(pointer_index);
				
				//
				// calculate difference to old position
				//
				float diff_x = pointer.m_current_x - current_x;
				float diff_y = pointer.m_current_y - current_y;
				
				//
				// is there a siginificant move
				//
				if (Math.abs(diff_x) >= CloudViewConstants.POINTER_MOVE_THRESHOLD || Math.abs(diff_y) >= CloudViewConstants.POINTER_MOVE_THRESHOLD)
				{
					//
					// pointer has been moved
					//
					pointer.m_moved = true;	
					
					//
					// update pointer details
					//
					pointer.m_current_x = current_x;
					pointer.m_current_y = current_y;	
					
					//
					// notify view on move
					//
					m_view.onPointerMove(pointer_index, diff_x, diff_y);
				}
				
				//Logger.i("move: pointer index: " + pointer_index + " x:" + pointer.m_current_x + " y:" + pointer.m_current_y + " time diff: " + (System.currentTimeMillis() - pointer.m_time));
			}
			
			
			
		}
		return true;
	}
	
	/**
	 * this class is used to inform the view that a long key press is taking place
	 * @author Johannes Anderwald
	 *
	 */
	private class PointerTimerTask extends TimerTask
	{

		@Override
		public void run() {
			if (m_pointers.size() == 1)
			{
				//
				// inform view of a long press
				//
				Pointer p1 = m_pointers.get(0);
				
				//
				// check if the mouse has been moved
				//
				if (p1.m_moved == false)
				{
					//
					// the mouse is not moved, it is a key press
					//
					m_view.buttonPressed(p1.m_current_x, p1.m_current_y, System.currentTimeMillis() - p1.m_time);
				
					//
					// clear pointer list to avoid redundant notification
					//
					m_pointers.clear();
				}
			}
		}
		
	}
	
}