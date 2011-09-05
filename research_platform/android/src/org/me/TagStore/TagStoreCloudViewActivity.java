package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class TagStoreCloudViewActivity extends Activity {

	/**
	 * stores the cloud view
	 */
	private CloudView m_view;

	/**
	 * stores the cloud touch listener
	 */
	private CloudTouchListener m_listener;

	/**
	 * stores all tags
	 */
	LinkedHashSet<String> m_tag_stack;

	@Override
	public void onResume() {

		//
		// call base class
		//
		super.onResume();

	}

	protected void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("TagStoreCloudViewActivity::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// construct new tag stack
		//
		m_tag_stack = new LinkedHashSet<String>();

		//
		// refresh view
		//
		refreshView();
	}

	/**
	 * refreshes the view
	 */
	private void refreshView() {
		//
		// construct cloud view
		//
		m_view = new CloudView(this, m_tag_stack);

		//
		// construct cloud touch listener
		//
		m_listener = new CloudTouchListener(m_view);

		//
		// add on touch listener
		//
		m_view.setOnTouchListener(m_listener);

		//
		// set content view of activity
		//
		setContentView(m_view);
	}

	@Override
	public void onBackPressed() {
		Logger.i("TagStoreCloudViewActivity::onBackPressed tag stack size: "
				+ m_tag_stack.size());
	}

	private class CloudTouchListener implements OnTouchListener {

		private class Pointer {
			public float m_x;
			public float m_y;
			public float m_current_x;
			public float m_current_y;

			private int m_id;
		};

		/**
		 * stores list of pointers on the screen
		 */
		ArrayList<Pointer> m_pointers;

		/**
		 * stores cloud view
		 */
		CloudView m_view;

		/**
		 * constructor of class CloudTouchListener
		 * 
		 * @param view
		 * @param m_tag_stack
		 */
		public CloudTouchListener(CloudView view) {

			//
			// construct array list storing Pointer class
			//
			m_pointers = new ArrayList<Pointer>();

			//
			// store view for handling notifications
			//
			m_view = view;
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

		@Override
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

				//
				// add to pointer list
				//
				m_pointers.add(pointer);

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
					m_view.buttonPressed(p1.m_current_x, p1.m_current_y);
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
					// update pointer details
					//
					pointer.m_current_x = event.getX(pointer_index);
					pointer.m_current_y = event.getY(pointer_index);
				}
			}
			return true;
		}
	}

	class CloudView extends SurfaceView {

		/**
		 * stores all information related to a tag in the cloud
		 * 
		 * @author Johnseyii
		 * 
		 */
		private class CloudTag {
			public String m_tag;
			public int m_ref_count;
			public Rect m_rect;
			public int m_font_size;
			public int m_color;
			public boolean m_is_tag;

			/**
			 * initializes a cloud tag
			 * 
			 * @param tag
			 *            of item
			 * @param ref_count
			 *            tag usage count
			 * @param color
			 */
			public CloudTag(String tag, int ref_count, int font_size,
					int color, boolean is_tag) {
				//
				// initialize tag
				//
				m_tag = tag;
				m_ref_count = ref_count;
				m_font_size = font_size;
				m_color = color;
				m_is_tag = is_tag;
				m_rect = new Rect();

			}

		};

		/**
		 * list of cloud tags
		 */
		ArrayList<CloudTag> m_tags;

		/**
		 * width of view
		 */
		int m_width;

		/**
		 * height of view
		 */
		int m_height;

		/**
		 * stores the maximum reference count of tag
		 */
		int m_max_ref;

		/**
		 * stores the minimum reference count of a tag
		 */
		int m_min_ref;

		/**
		 * stores the tags
		 */
		private LinkedHashSet<String> m_tag_stack;

		/**
		 * stores the font sizes of the tags / file objects
		 */
		int[] m_font_size = new int[4];

		/**
		 * stores the colors of the tags / file objects
		 */
		int[] m_colors = new int[4];

		/**
		 * stores the tag reference count which determine the font size / colors
		 */
		int[] m_tag_refs = new int[4];

		/**
		 * maximum font size
		 */
		private final static int MAX_FONT_SIZE = 40;

		/**
		 * font step size
		 */
		private final static int FONT_STEP_SIZE = 5;

		/**
		 * default font size for file items
		 */
		public int FILE_ITEM_FONT_SIZE = 30;

		/**
		 * constructor of class CloudView
		 * 
		 * @param context
		 * @param m_tag_stack
		 */
		public CloudView(Context context, LinkedHashSet<String> tag_stack) {

			//
			// construct base class
			//
			super(context);

			//
			// construct cloud tag list
			//
			m_tags = new ArrayList<CloudTag>();

			//
			// store tag stack
			//
			m_tag_stack = tag_stack;

			m_colors[0] = Color.WHITE;
			m_colors[1] = Color.rgb(255, 102, 0);
			m_colors[2] = Color.rgb(212, 85, 0);
			m_colors[3] = Color.rgb(160, 90, 44);

			m_font_size[0] = MAX_FONT_SIZE;
			m_font_size[1] = MAX_FONT_SIZE - FONT_STEP_SIZE;
			m_font_size[2] = MAX_FONT_SIZE - FONT_STEP_SIZE * 2;
			m_font_size[3] = MAX_FONT_SIZE - FONT_STEP_SIZE * 3;

			//
			// initialize the view
			//
			initializeView();

			//
			// change background
			//
			changeBG(0, 0, 0);

			//
			// http://stackoverflow.com/questions/974680/android-onkeydown-problem
			// we need to be focusable otherwise we dont receive key events
			//
			this.requestFocus();
			this.setFocusableInTouchMode(true);

		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			//
			// check if not back button was pressed
			//
			if (keyCode != KeyEvent.KEYCODE_BACK) {
				//
				// pass on to default handler
				//
				return super.onKeyDown(keyCode, event);
			}

			Logger.i("CloudView::onKeyDown tag stack size: "
					+ m_tag_stack.size());

			//
			// check if object stack is empty
			//
			if (m_tag_stack.size() == 0) {
				//
				// exit application
				//
				TagActivityGroup.s_Instance.back();
				return true;
			}

			if (m_tag_stack.size() == 1) {
				//
				// clear tag stack
				//
				m_tag_stack.clear();

				//
				// clear tags
				//
				m_tags.clear();

				//
				// initialize view
				//
				initializeView();

				//
				// done
				//
				invalidate();
				return true;
			}

			//
			// pop last tag from tag stack
			//
			String last_element = removeLastTagFromTagStack();

			//
			// clear tags
			//
			m_tags.clear();

			//
			// fill list map with tags and files
			//
			fillListMapWithTag(last_element);

			//
			// invalidate view
			//
			invalidate();

			//
			// mission accomplished
			//
			return true;
		}

		/**
		 * removes the last tag from the tag stack
		 */
		private String removeLastTagFromTagStack() {

			//
			// get all objects stored on the stack
			//
			String[] object_stack = m_tag_stack.toArray(new String[1]);

			//
			// this is a bit retarded ;)
			//
			String last_element = object_stack[object_stack.length - 1];

			//
			// remove last element
			//
			m_tag_stack.remove(last_element);

			//
			// return last element
			//
			return last_element;
		}

		/**
		 * launches an item
		 * 
		 * @param position
		 */
		private void launchItem(String item_path) {

			//
			// construct file object
			//
			File file = new File(item_path);

			if (file.exists() == false) {
				//
				// FIXME: non-nls compatible
				//
				Toast toast = Toast.makeText(getApplicationContext(),
						"Error: file " + item_path + " no longer exists",
						Toast.LENGTH_SHORT);

				//
				// display toast
				//
				toast.show();

				//
				// FIXME: refresh perhaps?
				//
				return;
			}

			//
			// create new intent
			//
			Intent intent = new Intent();

			//
			// set intent action
			//
			intent.setAction(android.content.Intent.ACTION_VIEW);

			//
			// create uri from file
			//
			Uri uri_file = Uri.fromFile(file);

			//
			// get mime type map instance
			//
			MimeTypeMap mime_map = MimeTypeMap.getSingleton();

			//
			// get file extension
			//
			String file_extension = item_path.substring(item_path
					.lastIndexOf(".") + 1);

			//
			// guess file extension
			//
			String mime_type = mime_map
					.getMimeTypeFromExtension(file_extension);

			//
			// set intent data type
			//
			intent.setDataAndType(uri_file, mime_type);

			//
			// start activity
			//
			TagActivityGroup.s_Instance.startActivity(intent);
		}

		public void buttonPressed(float current_x, float current_y) {

			int x = (int) current_x;
			int y = (int) current_y;

			ArrayList<CloudTag> tags = getIntersectingTags(x - 8, x + 8, y - 8,
					y + 8);

			if (tags.size() == 1) {
				//
				// get tag
				//
				CloudTag tag = tags.get(0);

				//
				// is it a tag
				//
				if (tag.m_is_tag) {
					//
					// add tag to tag stack
					//
					m_tag_stack.add(tag.m_tag);

					//
					// clear tags
					//
					m_tags.clear();

					//
					// fill list map with tags and files
					//
					fillListMapWithTag(tag.m_tag);

					//
					// invalidate view
					//
					invalidate();

				} else {
					//
					// launch file
					//
					launchItem(tag.m_tag);
				}
			}
		}

		protected void fillListMapWithTag(String item_name) {

			//
			// acquire instance of database manager
			//
			DBManager db_man = DBManager.getInstance();

			//
			// get associated tags
			//
			ArrayList<String> linked_tags = db_man.getLinkedTags(item_name);

			//
			// construct new array for tags
			//
			ArrayList<String> tags = new ArrayList<String>();

			if (linked_tags != null) {

				//
				// add them to list
				//
				for (String linked_tag : linked_tags) {
					//
					// check if tag has already been visited
					//
					if (m_tag_stack.contains(linked_tag) == false) {

						//
						// add tag
						//
						tags.add(linked_tag);
						continue;
					}
				}

				//
				// calculate minimum and maximum
				//
				findMinimumAndMaximum(tags);

				//
				// calculate tag reference array boundaries
				//
				calculateTagReferenceArray();

				//
				// add tags
				//
				addTags(tags, true);
			}

			//
			// get associated files
			//
			ArrayList<String> linked_files = db_man.getLinkedFiles(item_name);

			if (linked_files != null) {

				//
				// add linked files
				//
				addTags(linked_files, false);
			}
		}

		/**
		 * finds minimum and maximum of the tag
		 * 
		 * @param linked_tags
		 */
		private void findMinimumAndMaximum(ArrayList<String> linked_tags) {

			m_max_ref = 0;
			m_min_ref = 10000;

			//
			// acquire instance of database manager
			//
			DBManager db_man = DBManager.getInstance();

			//
			// get tag reference count
			//
			HashMap<String, Integer> ref_count = db_man.getTagReferenceCount();

			for (String tag : linked_tags) {
				//
				// get tag reference count
				//
				int ref = ref_count.get(tag).intValue();

				if (ref > m_max_ref) {
					//
					// new maximum
					//
					m_max_ref = ref;
				}

				if (ref < m_min_ref) {
					//
					// new minimum
					//
					m_min_ref = ref;
				}
			}

		}

		/**
		 * adds an item to the cloud tag vector
		 * 
		 * @param tag
		 *            to be added
		 * @param ref_count
		 *            reference count of tag
		 * @param font_size
		 *            font size of tag
		 * @param color
		 *            color of tag
		 * @param is_tag
		 *            if its a tag or an file path
		 */
		private void addItem(String tag, int ref_count, int font_size,
				int color, boolean is_tag) {

			//
			// construct new cloud tag
			//
			CloudTag cloud_tag = new CloudTag(tag, ref_count, font_size, color,
					is_tag);

			//
			// add to cloud tag vector
			//
			m_tags.add(cloud_tag);
		}

		/**
		 * calculates the boundaries how many refs (usages) have
		 */
		protected void calculateTagReferenceArray() {

			//
			// get max - min span
			//
			int ref_span = m_max_ref - m_min_ref;

			if (ref_span > 0) {
				//
				// divide range into four partitions
				//
				ref_span /= 4;

			}

			//
			// check if ref_span is < 0
			//
			if (ref_span < 1)
				ref_span = 1;

			//
			// compute tag reference array
			//
			for (int index = 0; index < 4; index++) {
				m_tag_refs[index] = m_max_ref
						- (index > 0 ? index * ref_span : 0);
			}

		}

		/**
		 * adds all tags to cloud tag vector
		 * 
		 * @param tags
		 */
		protected void addTags(ArrayList<String> tags, boolean is_tag) {
			//
			// acquire instance of database manager
			//
			DBManager db_man = DBManager.getInstance();

			//
			// get tag reference count
			//
			HashMap<String, Integer> ref_count = db_man.getTagReferenceCount();

			for (String tag : tags) {
				if (is_tag) {
					//
					// get reference count
					//
					Integer tag_ref = ref_count.get(tag);

					//
					// get index
					//
					int tag_index = 0;
					while (tag_index < 4
							&& tag_ref.intValue() < m_tag_refs[tag_index])
						tag_index++;

					//
					// add tag
					//
					addItem(tag, tag_ref.intValue(), m_font_size[tag_index],
							m_colors[tag_index], is_tag);
				} else {
					//
					// add file item
					//
					addItem(tag, 1, FILE_ITEM_FONT_SIZE, Color.BLUE, false);
				}
			}
		}

		/**
		 * initializes the view
		 */
		protected void initializeView() {
			//
			// acquire instance of database manager
			//
			DBManager db_man = DBManager.getInstance();

			//
			// get popular tags
			//
			ArrayList<String> popular_tags = db_man.getPopularTags();

			//
			// get tag reference count
			//
			HashMap<String, Integer> ref_count = db_man.getTagReferenceCount();

			if (popular_tags == null || ref_count == null
					|| popular_tags.size() == 0 || ref_count.size() == 0) {
				//
				// empty cloud view
				//
				return;
			}

			//
			// get maximum tag usage
			//
			m_max_ref = ref_count.get(popular_tags.get(0)).intValue();

			//
			// get minimum tag usage
			//
			m_min_ref = ref_count
					.get(popular_tags.get(popular_tags.size() - 1)).intValue();

			//
			// calculate tag reference array boundaries
			//
			calculateTagReferenceArray();

			//
			// add those tags
			//
			addTags(popular_tags, true);
		}

		@Override
		protected void onSizeChanged(int width, int height, int oldwidth,
				int oldheight) {

			//
			// call base implementation
			//
			super.onSizeChanged(width, height, oldwidth, oldheight);

			//
			// store new width / height
			//
			m_width = width;
			m_height = height;
		}

		/**
		 * checks if the specified tag intersects the tag
		 * 
		 * @param tag
		 *            with an rectangle associated
		 * @param left
		 *            corner
		 * @param right
		 *            corner
		 * @param top
		 *            position
		 * @param bottom
		 *            position
		 * @return true when it intersects
		 */
		protected boolean intersectsTag(CloudTag tag, int left, int right,
				int top, int bottom) {

			//
			// copy old rectangle
			//
			Rect rect1 = new Rect(tag.m_rect);

			//
			// construct new rectangle
			//
			Rect rect2 = new Rect(left, top, right, bottom);

			//
			// is it intersecting
			//
			boolean ret = Rect.intersects(rect1, rect2);

			return ret;
		}

		/**
		 * returns list of cloud tags which intersect with the given square
		 * 
		 * @param left
		 *            corner
		 * @param right
		 *            corner
		 * @param top
		 *            position
		 * @param bottom
		 *            position
		 * @return ArrayList<CloudTag>
		 */
		protected ArrayList<CloudTag> getIntersectingTags(int left, int right,
				int top, int bottom) {

			//
			// allocate result vector
			//
			ArrayList<CloudTag> tags = new ArrayList<CloudTag>();

			for (CloudTag tag : m_tags) {
				//
				// check if rectangle is empty
				//
				if (tag.m_rect.isEmpty()) {
					//
					// rectangle is empty -> not present yet in view
					//
					continue;
				}

				//
				// does it intersect the tag
				//
				if (intersectsTag(tag, left, right, top, bottom)) {
					//
					// add to tag vector
					//
					tags.add(tag);
				}
			}

			//
			// return list of tags which intersect with this triangle
			//
			return tags;
		}

		/**
		 * paints the given tag
		 * 
		 * @param canvas
		 *            paint into specified canvas
		 * @param paint
		 *            to be used for painting
		 * @param tag
		 *            to be used for painting
		 * @param x
		 *            left position to start painting
		 * @param y
		 *            height to start painting
		 */
		protected void paintTag(Canvas canvas, Paint paint, CloudTag tag,
				int offset_x, int offset_y, boolean elipse) {

			//
			// get tag
			//
			String text = tag.m_tag;

			if (tag.m_is_tag == false) {
				//
				// find last positionof '/'
				//
				int pos = text.lastIndexOf("/");
				if (pos > 0) {
					//
					// truncate directory
					//
					text = text.substring(pos + 1);
				}
			}

			//
			// set text size
			//
			paint.setTextSize(tag.m_font_size);

			//
			// set color
			//
			paint.setColor(tag.m_color);

			//
			// get text bounds
			//
			paint.getTextBounds(text, 0, text.length(), tag.m_rect);

			//
			// add offsets
			//
			tag.m_rect.left += offset_x;
			tag.m_rect.right += offset_x;
			tag.m_rect.top += offset_y;
			tag.m_rect.bottom += offset_y;

			//
			// draw text
			//
			canvas.drawText(text, offset_x, offset_y, paint);

			//
			// DBG: draw surrounding rectangle
			//
			// canvas.drawLine(tag.m_rect.left, tag.m_rect.top,
			// tag.m_rect.right, tag.m_rect.top, paint);
			// canvas.drawLine(tag.m_rect.left, tag.m_rect.bottom,
			// tag.m_rect.left, tag.m_rect.top, paint);
			// canvas.drawLine(tag.m_rect.right, tag.m_rect.bottom,
			// tag.m_rect.right, tag.m_rect.top, paint);
			// canvas.drawLine(tag.m_rect.left, tag.m_rect.bottom,
			// tag.m_rect.right, tag.m_rect.bottom, paint);

			if (elipse) {
				//
				// construct new rectangle
				//
				RectF rect = new RectF();

				//
				// increase target rectangle
				//
				rect.left = tag.m_rect.left - 50;
				rect.right = tag.m_rect.right + 50;
				rect.top = tag.m_rect.top - 50;
				rect.bottom = tag.m_rect.bottom + 50;

				//
				// draw elipse
				//
				// paint.setStyle(Paint.Style.STROKE);
				// canvas.drawOval(rect, paint);
			}

		}

		protected void paintFirstTag(Canvas canvas, Paint paint, CloudTag tag,
				boolean elipse) {

			//
			// get tag
			//
			String text = tag.m_tag;

			if (tag.m_is_tag == false) {
				//
				// find last positionof '/'
				//
				int pos = text.lastIndexOf("/");
				if (pos > 0) {
					//
					// truncate directory
					//
					text = text.substring(pos + 1);
				}
			}

			//
			// create rectangle
			//
			Rect rect = new Rect();

			//
			// get text bounds
			//
			paint.getTextBounds(text, 0, text.length(), rect);

			//
			// get text dimension in absolute coordinates
			//
			int width = Math.abs(rect.right - rect.left);
			int height = Math.abs(rect.bottom - rect.top);

			Logger.i("Tag: " + text + " length: " + text + " width: " + width
					+ " m_width: " + m_width + " heigth: " + height
					+ " m_heigth: " + m_height);

			//
			// calculate offset where to start painting
			//
			int start_x = (m_width / 2) - (width / 2);
			int start_y = (m_height / 2) + (height / 2);

			//
			// paint the tag
			//
			paintTag(canvas, paint, tag, start_x, start_y, elipse);
		}

		@Override
		protected void onDraw(Canvas canvas) {

			//
			// call base implementation
			//
			super.onDraw(canvas);

			Paint paint = new Paint();
			paint.setColor(android.graphics.Color.WHITE);
			paint.setTextSize(30);

			boolean first_tag = true;

			Random rand = new Random();
			int collision_count = 0;

			for (CloudTag tag : m_tags) {
				if (first_tag) {
					//
					// first tag
					//
					paintFirstTag(canvas, paint, m_tags.get(0), true);
					first_tag = false;
					continue;
				}
				do {
					int width = rand.nextInt(m_width);
					int height = rand.nextInt(m_height);

					Rect rect = new Rect();

					String text = tag.m_tag;
					if (tag.m_is_tag == false) {
						//
						// find last positionof '/'
						//
						int pos = text.lastIndexOf("/");
						if (pos > 0) {
							//
							// truncate directory
							//
							text = text.substring(pos + 1);
						}
					}

					//
					// get text bounds
					//
					paint.getTextBounds(text, 0, text.length(), rect);

					//
					// add offsets
					//
					rect.left += width;
					rect.right += width;
					rect.top += height;
					rect.bottom += height;

					Logger.i("rect: " + rect);

					if (rect.right >= m_width || rect.bottom >= m_height
							|| rect.left <= 0 || rect.top <= 0)
						continue;

					ArrayList<CloudTag> intersects = getIntersectingTags(
							rect.left, rect.right, rect.top, rect.bottom);
					if (intersects.size() == 0) {
						paintTag(canvas, paint, tag, width, height, false);
						break;
					}
					collision_count++;
				} while (true);
			}

			Logger.e("TagCount: " + m_tags.size() + " Collisions: "
					+ collision_count);

		}

		public void changeBG(int r, int g, int b) {

			System.out.println("change bg: " + r + "," + g + "," + b);

			setBackgroundColor(Color.rgb(r, g, b));

			invalidate();

		}
	}
}
