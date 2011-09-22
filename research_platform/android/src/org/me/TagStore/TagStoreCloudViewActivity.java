package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.me.TagStore.FileDialogBuilder.MENU_ITEM_ENUM;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

public class TagStoreCloudViewActivity extends Activity implements FileDialogBuilder.GeneralDialogCallback{

	private static final int DIALOG_GENERAL_FILE_MENU = 1;
	private static final int DIALOG_DETAILS = 2;

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
	TagStackManager m_tag_stack;
	
	protected static TagStoreCloudViewActivity s_Instance;
	

	@Override
	public void onResume() {

		//
		// call base class
		//
		super.onResume();

		if (m_tag_stack != null)
		{
			//
			// verify the tag stack
			//
			m_tag_stack.verifyTags();
			
			//
			// refresh the view
			//
			m_view.initializeView();			
			m_view.invalidate();
		}
		
	}

	protected void onPrepareDialog (int id, Dialog dialog) {
		
		//
		// get the current tag
		//
		String file_name = m_view.getCurrentTag();	

		Logger.i("TagStoreListViewActivity::onPrepareDialog dialog id: " + id + " file_name: " + file_name);
		
		//
		// check which dialog is requested
		//
		switch (id) {
			case DIALOG_DETAILS:
				FileDialogBuilder.updateDetailDialogFileView(dialog, TagActivityGroup.s_Instance, file_name);
				break;
		}
	}

	
	/**
	 * called when there is a need to construct a dialog
	 */
	protected Dialog onCreateDialog(int id) {

		Logger.i("TagStoreListViewActivity::onCreateDialog dialog id: " + id);
		
		//
		// construct dialog
		//
		Dialog dialog = null;

		//
		// get the current tag
		//
		String file_name = m_view.getCurrentTag();
		Logger.e("onCreateDialog: " + file_name);
		
		//
		// check which dialog is requested
		//
		switch (id) {

		case DIALOG_GENERAL_FILE_MENU:
			dialog = FileDialogBuilder.buildGeneralDialogFile(TagActivityGroup.s_Instance, file_name, this);
			break;
		case DIALOG_DETAILS:
			dialog = FileDialogBuilder.buildDetailDialogFile(TagActivityGroup.s_Instance, file_name);
			break;
			
		}
		return dialog;
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

		s_Instance = this;
		
		//
		// construct new tag stack
		//
		m_tag_stack = TagStackManager.getInstance();

		//
		// refresh view
		//
		initView();
	}

	/**
	 * refreshes the view
	 */
	private void initView() {
		
		
		//
		// construct cloud view
		//
		m_view = new CloudView(this, m_tag_stack);
		
		//
		// initialize view
		//		
		m_view.initializeView();

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
		
		//
		// http://stackoverflow.com/questions/974680/android-onkeydown-problem
		// we need to be focusable otherwise we don't receive key events
		//
		m_view.requestFocus();
		m_view.setFocusableInTouchMode(true);
		
	}

	@Override
	public void onBackPressed() {
		Logger.i("TagStoreCloudViewActivity::onBackPressed tag stack size: "
				+ m_tag_stack.getSize());
	}

	private class CloudTouchListener implements OnTouchListener {

		private class Pointer {
			public float m_x;
			public float m_y;
			public float m_current_x;
			public float m_current_y;
			public long m_time;
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
				pointer.m_time = System.currentTimeMillis();
				Logger.e("m_time:" + pointer.m_time);
				
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
					Logger.e("m_time:" + p1.m_time + " now: " + System.currentTimeMillis());
					m_view.buttonPressed(p1.m_current_x, p1.m_current_y, System.currentTimeMillis() - p1.m_time);
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

	public class CloudView extends SurfaceView {

		/**
		 * stores all information related to a tag in the cloud
		 * 
		 * @author Johnseyii
		 * 
		 */
		private class CloudTag {
			public String m_tag;
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
			public CloudTag(String tag, int font_size,
					int color, boolean is_tag) {
				//
				// initialize tag
				//
				m_tag = tag;
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
		private TagStackManager m_tag_stack;

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
		 * screen  box manager
		 */
		private ScreenBoxManager m_boxmgr = null;
		
		/**
		 * currently selected tag
		 */
		private CloudTag m_selected_tag;
		
		/**
		 * constructor of class CloudView
		 * 
		 * @param context
		 * @param m_tag_stack
		 */
		public CloudView(Context context, TagStackManager tag_stack) {

			//
			// construct base class
			//
			super(context);

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
			// change background
			//
			changeBG(0, 0, 0);
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
					+ m_tag_stack.getSize());

			//
			// check if object stack is empty
			//
			if (m_tag_stack.isEmpty()) {
				//
				// exit application
				//
				TagActivityGroup.s_Instance.back();
				return true;
			}

			if (m_tag_stack.getSize() == 1) {
				//
				// clear tag stack
				//
				m_tag_stack.clearTags();

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
			String last_element = m_tag_stack.getLastTag();
			m_tag_stack.removeTag(last_element);
			

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
		 * launches an item
		 * 
		 * @param position
		 */
		private void launchItem(String item_path) {

			//
			// get current storage state
			//
			String storage_state = Environment.getExternalStorageState();
			if (!storage_state.equals(Environment.MEDIA_MOUNTED) && !storage_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
			{
				//
				// the media is currently not accessible
				//
				String media_available = getApplicationContext().getString(R.string.error_media_not_mounted);
				
				//
				// create toast
				//
				Toast toast = Toast.makeText(getApplicationContext(), media_available, Toast.LENGTH_SHORT);
				
				//
				// display toast
				//
				toast.show();
				
				//
				// done
				//
				return;
			}
			
			//
			// construct file object
			//
			File file = new File(item_path);

			if (file.exists() == false) {
				
				//
				// get localized error format
				//
				String error_format = getApplicationContext().getString(R.string.error_format_file_removed);
				
				//
				// format the error
				//
				String msg = String.format(error_format, item_path);
				
				//
				// create the toast
				//
				Toast toast = Toast.makeText(getApplicationContext(),
						msg,
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

		/**
		 * returns the current selected tag
		 * @return String
		 */
		public String getCurrentTag() {
			
			if (m_selected_tag != null)
				return m_selected_tag.m_tag;
			else
				return null;
		}
		/**
		 * This is the call-back function for the CloudTouchlistener
		 * @param current_x x coordinate of the key press
		 * @param current_y y coordinate of the key press
		 * @param time
		 */
		public void buttonPressed(float current_x, float current_y, long time) {

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
					m_tag_stack.addTag(tag.m_tag);

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
					// if key press is longer than 300ms, it is a long press
					//
					if (time > 300)
					{
						//
						// set as active tag
						//
						m_selected_tag = tag;
						//
						// display dialog
						//
						showDialog(DIALOG_GENERAL_FILE_MENU);
						return;
					}
					
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
					if (m_tag_stack.containsTag(linked_tag) == false) {

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
			// m_tag_stack.toArray(new String[1])
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
			CloudTag cloud_tag = new CloudTag(tag, font_size, color,
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
				ref_span /= 2;

			}

			//
			// check if ref_span is < 0
			//
			if (ref_span < 1)
				ref_span = 1;

			
			m_tag_refs[0] = m_max_ref;
			m_tag_refs[1] = m_max_ref - ref_span;
			m_tag_refs[2] = m_max_ref - ref_span * 2;
			m_tag_refs[3] = m_min_ref;
			
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
					{
						tag_index++;
					}
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
		public void initializeView() {
			
			//
			// construct cloud tag list
			//
			m_tags = new ArrayList<CloudTag>();
			
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
			// check if tag stack is empty
			//
			if (!m_tag_stack.isEmpty())
			{
				//
				// pop last tag from tag stack
				//
				String last_element = m_tag_stack.getLastTag();

				//
				// clear tags
				//
				m_tags.clear();

				//
				// fill list map with tags and files
				//
				fillListMapWithTag(last_element);
			}
			else
			{
				//
				// add those tags
				//
				addTags(popular_tags, true);
			}
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
		protected void paintTag(Canvas canvas, Paint paint, CloudTag tag, String text,
				int offset_x, int offset_y, boolean elipse) {

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

		/**
		 * returns the cloud item text
		 * @param tag cloud item
		 * @return String
		 */
		protected String getCloudItemText(CloudTag tag) {
			
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
			return text;
		}
		
		
		protected void paintCloudTag(Canvas canvas, Paint paint, CloudTag tag) {
			
			//
			// create rectangle
			//
			Rect rect = new Rect();

			//
			// get cloud text
			//
			String text = getCloudItemText(tag);
			
			//
			// get text bounds
			//
			paint.getTextBounds(text, 0, text.length(), rect);

			//
			// get text dimension in absolute coordinates
			//
			int min_width = Math.abs(rect.right - rect.left) + 10;
			int min_height = Math.abs(rect.bottom - rect.top) + 10;
			
			rect = m_boxmgr.getRectangleWithDimension(min_width, min_height);
			if (rect != null)
			{
				//
				// paint the tag
				//
				paintTag(canvas, paint, tag, text, rect.left, rect.top + 40, false);
				//Logger.e("painting tag: " + text + " left : " + rect.left + " top: " + rect.top);
			}
			else
			{
				Logger.e("No place for box with dimension " + min_width + "/" + min_height + " text : " + text);
			}
		}
		
		
		
		@Override
		protected void onDraw(Canvas canvas) {

			//
			// call base implementation
			//
			super.onDraw(canvas);

			//
			// construct new paint
			//
			Paint paint = new Paint();

			//
			// construct new box screen manager
			//
			m_boxmgr = new ScreenBoxManager(m_width, m_height, MAX_FONT_SIZE+10);
			
			//
			// paint all tags
			//
			for(CloudTag tag : m_tags)
			{
				paint.setColor(tag.m_color);
				paint.setTextSize(tag.m_font_size);
				paintCloudTag(canvas, paint, tag);
			}
			
			//
			// set color
			//
			paint.setColor(Color.RED);
			paint.setStyle(Style.STROKE);
			m_boxmgr.paintFreeRect(canvas, paint);
		}

		public void changeBG(int r, int g, int b) {

			System.out.println("change bg: " + r + "," + g + "," + b);

			setBackgroundColor(Color.rgb(r, g, b));

			invalidate();

		}
		
		/**
		 * This class is used to manage the free space on the screen. It divides the free space into rectangles which can be consumed by tags
		 * @author Johannes Anderwald
		 *
		 */
		private class ScreenBoxManager
		{
			/**
			 * width of the screen
			 */
			private int m_width;
			
			/**
			 * height of the screen
			 */
			private int m_height;
			
			/**
			 * max font size
			 */
			int m_max_font_size;
			
			/**
			 * holds free space rectangles
			 */
			private ArrayList<Rect> m_free_rect;
			
			/**
			 * constructor of class ScreenBox Manager
			 * @param width width of the screen
			 * @param height height of the screen
			 */
			public ScreenBoxManager(int width, int height, int max_font_size)
			{
				m_width = width;
				m_height = height;
				m_max_font_size = max_font_size;
				m_free_rect = new ArrayList<Rect>();
				
				//
				// split the drawing area into boxes of maximum font size width
				//
				for(int y = 0; y < m_height; y+= m_max_font_size)
				{
					//
					// create bounding rectangle
					//
					Rect rect = new Rect();
					rect.left = 0;
					rect.right = m_width;
					rect.top = y;
					rect.bottom = Math.min(m_max_font_size, m_height - y) + y;
				
					//
					// add the rectangle
					//
					m_free_rect.add(rect);
				}
			}
			
			public void paintFreeRect(Canvas canvas, Paint paint) {
				for(Rect rect : m_free_rect)
				{
					canvas.drawRect(rect, paint);
				}
				
			}

			/**
			 * returns a rectangle which has the minimum dimension given
			 * @param min_width minimum width of the rectangle
			 * @param min_height minimum height of the rectangle
			 * @return Rect object when successful
			 */
			public Rect getRectangleWithDimension(int min_width, int min_height) {
				
				//
				// find a free rectangle first
				//
				Rect free_rect = findFreeRectangle(min_width, min_height);
			
				if (free_rect != null)
				{
					//
					// the rectangle has already been split once
					//
					Rect new_rect1 = new Rect(free_rect);
					new_rect1.left = free_rect.left + min_width;
					
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
				Logger.e("no rectangle");
				return null;
			}
			
			/**
			 * scans the list of free rectangle to find a rectangle which has the given dimensions
			 * @param min_width
			 * @param min_height
			 * @return
			 */
			private Rect findFreeRectangle(int min_width, int min_height) {
				
				int min_diff = Integer.MAX_VALUE;
				Rect cur_rect = null;
				
				for(Rect rect : m_free_rect)
				{
					//
					// get width
					//
					int current_width = Math.abs(rect.width());
					
					if (current_width >= min_width)
					{
						//
						// calculate difference
						//
						int cur_diff = (current_width - min_width);
						if (cur_diff < min_diff)
						{
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
	}

	@Override
	public void processMenuFileSelection(String file_name,
			MENU_ITEM_ENUM action_id) {

		if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DETAILS) {
			//
			// launch details view
			//
			showDialog(DIALOG_DETAILS);
		} 
		else if (action_id == FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_DELETE)
			
		{
			//
			// remove the file
			//
			FileTagUtility.removeFile(m_view.getCurrentTag(), this);			
			
			//
			// verify the tag stack
			//
			m_tag_stack.verifyTags();
			
			//
			// refresh the view
			//
			m_view.initializeView();			
			m_view.invalidate();	
		}
	}
}
