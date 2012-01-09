package org.me.TagStore.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.EventDispatcher;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TagStackManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import android.util.AttributeSet;
import android.view.SurfaceView;

public class CloudViewSurfaceAdapter extends SurfaceView{

	/**
	 * stores all information related to a tag in the cloud
	 * 
	 * @author Johannes Anderwald
	 * 
	 */
	public class CloudTag {
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
	private ArrayList<CloudTag> m_tags;

	/**
	 * width of view
	 */
	private int m_width;

	/**
	 * height of view
	 */
	private int m_height;

	/**
	 * stores the maximum reference count of tag
	 */
	private int m_max_ref;

	/**
	 * stores the minimum reference count of a tag
	 */
	private int m_min_ref;

	/**
	 * stores the font sizes of the tags / file objects
	 */
	private int[] m_font_size = new int[4];

	/**
	 * stores the colors of the tags / file objects
	 */
	private int[] m_colors = new int[4];

	/**
	 * stores the tag reference count which determine the font size / colors
	 */
	private int[] m_tag_refs = new int[4];

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
	private CloudViewScreenBoxManager m_boxmgr = null;
	
	/**
	 * currently selected tag
	 */
	private CloudTag m_selected_tag;
	
	/**
	 * bitmap where tags are painted into
	 */
	private Bitmap m_bitmap;
	
	/**
	 * target rectangle
	 */
	private Rect m_target_rect;
	
	/**
	 * source view port rectangle
	 */
	private Rect m_source_rect;
	
	
	/**
	 * constructor of class TabPageIndicator 
	 * @param context
	 */
	public CloudViewSurfaceAdapter(Context context) {
		this(context, null);

	}

    public CloudViewSurfaceAdapter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public CloudViewSurfaceAdapter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);	
	

		Logger.i("CloudViewSurfaceAdapter constructor");
		
		m_colors[0] = Color.WHITE;
		m_colors[1] = Color.rgb(255, 102, 0);
		m_colors[2] = Color.rgb(212, 85, 0);
		m_colors[3] = Color.rgb(160, 90, 44);

		m_font_size[0] = MAX_FONT_SIZE;
		m_font_size[1] = MAX_FONT_SIZE - FONT_STEP_SIZE;
		m_font_size[2] = MAX_FONT_SIZE - FONT_STEP_SIZE * 2;
		m_font_size[3] = MAX_FONT_SIZE - FONT_STEP_SIZE * 3;

		m_width = 0;
		m_height = 0;

		//
		// change background
		//
		changeBG(0, 0, 0);
	}
	
	/**
	 * returns the current selected item
	 * @return String
	 */
	public String getCurrentItem() {
		
		if (m_selected_tag != null)
			return m_selected_tag.m_tag;
		else
			return null;
	}
	
	/**
	 * returns true when the current selected item is a tag. if no item is selected or it is not a tag then it returns false
	 * @return boolean
	 */
	public boolean isCurrentItemTag()  {
		
		if (m_selected_tag != null)
			return m_selected_tag.m_is_tag;
		else
			return false;
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

		ArrayList<CloudTag> tags = getIntersectingTags(x - CloudViewConstants.POINTER_SIZE_RECTANGLE, 
													   x + CloudViewConstants.POINTER_SIZE_RECTANGLE, 
													   y - CloudViewConstants.POINTER_SIZE_RECTANGLE + m_source_rect.top,
													   y + CloudViewConstants.POINTER_SIZE_RECTANGLE + m_source_rect.top);

		Logger.i("buttonPressed: " + current_x + " y: " + current_y + " time: " + time + " tags: " + tags.size());
		
		for(CloudTag tag : tags)
			Logger.i(tag.m_tag);
		
		if (tags.size() == 1) {
			//
			// get tag
			//
			CloudTag tag = tags.get(0);

			if (time > CloudViewConstants.CONTEXT_MENU_DELAY)
			{
				//
				// set as active tag
				//
				m_selected_tag = tag;
				
				//
				// prepare event params
				//
				Object[] event_args = new Object[] {tag.m_tag, tag.m_is_tag};
				
				//
				// invoke callback
				//
				EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.ITEM_LONG_CLICK_EVENT, event_args);
				return;
			}
			
			//
			// is it a tag
			//
			if (tag.m_is_tag) {
				
				//
				// get tagstack manager
				//
				TagStackManager tag_stack = TagStackManager.getInstance();
				
				//
				// add tag to tag stack
				//
				tag_stack.addTag(tag.m_tag);

				//
				// clear tags
				//
				m_tags.clear();

				//
				// fill list map with tags and files
				//
				fillListMapWithTag();

				//
				// draw bitmap
				//
				drawBitmap();
				
				//
				// invalidate view
				//
				postInvalidate();

			} 
				
			//
			// prepare event params
			//
			Object[] event_args = new Object[] {tag.m_tag, tag.m_is_tag};
			
			//
			// invoke callback
			//
			EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.ITEM_CLICK_EVENT, event_args);			
		}
	}

	protected void fillListMapWithTag() {

		//
		// get associated tags
		//
		ArrayList<String> linked_tags = FileTagUtility.getLinkedTags();

		//
		// calculate minimum and maximum
		//
		findMinimumAndMaximum(linked_tags);

		//
		// calculate tag reference array boundaries
		//
		calculateTagReferenceArray();

		//
		// add tags
		//
		addTags(linked_tags, true);

		//
		// get linked files
		//
		ArrayList<String> linked_files = FileTagUtility.getLinkedFiles();
		addTags(linked_files, false);
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
		// get tagstack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
		
		//
		// check if tag stack is empty
		//
		if (!tag_stack.isEmpty())
		{
			//
			// clear tags
			//
			m_tags.clear();

			//
			// fill list map with tags and files
			//
			fillListMapWithTag();
		}
		else
		{
			//
			// add those tags
			//
			addTags(popular_tags, true);
		}

		if (m_width != 0 && m_height != 0)
		{
			//
			// draw bitmap when the size has already been determined
			//
			drawBitmap();
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

		Logger.i("onSizeChanged:: m_width: " + m_width + "m_height:" + m_height);

		//
		// draws the bitmap
		//
		drawBitmap();
		
		//
		// invalidate
		//
		postInvalidate();
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
	
	/**
	 * performs drawing into the bitmap
	 */
	private void drawBitmap() {
		
		//
		// initialize source & target rectangle
		//
		m_target_rect = new Rect(0, 0, m_width, m_height);
		m_source_rect = new Rect(0, 0, m_width, m_height);

		
		//
		// construct new box screen manager
		//
		m_boxmgr = new CloudViewScreenBoxManager(m_width, MAX_FONT_SIZE+15);

		//
		// now reserve all rectangles for the cloud tags
		//
		reserveRectangleForCloudTags();
		
		//
		// get height of box manager
		//
		int current_height = m_height; //m_boxmgr.getCurrentHeight();
		Logger.i("paintCloudTags height: " + current_height);
		
		//
		// construct new bitmap
		//
		m_bitmap = Bitmap.createBitmap(m_width, current_height, Bitmap.Config.RGB_565 );
		
		//
		// now construct new canvas which draws into that bitmap
		//
		Canvas new_canvas = new Canvas(m_bitmap);

		//
		// construct new paint
		//
		Paint paint = new Paint();
		
		//
		// now paint into the bitmap
		//
		for(CloudTag tag : m_tags)
			paintTag(new_canvas, paint, tag);
		
		//
		// DBG code
		//
		//m_boxmgr.paintFreeRect(new_canvas, paint);
/*
		try {
		       FileOutputStream out = new FileOutputStream("/mnt/sdcard/Ablage/test.png");
		       m_bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
		       e.printStackTrace();
		}
*/

	}

	/**
	 * paints a tag using a canvas
	 * @param canvas canvas to use for painting
	 * @param paint paint object specifying font color and size
	 * @param tag tag item
	 */
	private void paintTag(Canvas canvas, Paint paint, CloudTag tag) {
		
		//
		// init paint
		//
		paint.setTextSize(tag.m_font_size);
		paint.setColor(tag.m_color);

		
		//
		// get text
		//
		String text = getCloudItemText(tag);
		
		//
		// draw text
		//
		canvas.drawText(text, tag.m_rect.left, tag.m_rect.bottom, paint);
		
		//
		// DBG: draw surrounding rectangle
		//
		//canvas.drawLine(tag.m_rect.left, tag.m_rect.top,  tag.m_rect.right, tag.m_rect.top, paint);
		//canvas.drawLine(tag.m_rect.left, tag.m_rect.bottom, tag.m_rect.left, tag.m_rect.top, paint);
		//canvas.drawLine(tag.m_rect.right, tag.m_rect.bottom, tag.m_rect.right, tag.m_rect.top, paint);
		//canvas.drawLine(tag.m_rect.left, tag.m_rect.bottom, tag.m_rect.right, tag.m_rect.bottom, paint);
	}
	/*
	private void reserveRectangleForCloudTags1() {
		
		//
		// get num of tags
		//
		int max_tags = m_tags.size();
		double phi = 1.0;
		double theta = 0.0;
		double radius = Math.min((m_width/2) * 0.95f , (m_height/2) * 0.95f );
		
		int index = 1;

		//
		// allocate new paint
		//
		Paint paint = new Paint();
				
		
		for(CloudTag tag : m_tags)
		{
				phi = Math.acos(-1.0 + (2.0*index -1.0)/max_tags);
				theta = Math.sqrt(max_tags*Math.PI) * phi;
			
			//coordinate conversion:			
			int pos_x = (int)(radius * Math.cos(theta) * Math.sin(phi));
			int pos_y = (int)(radius * Math.sin(theta) * Math.sin(phi));
			
			
			//
			// get cloud text
			//
			String text = getCloudItemText(tag);
			
			//
			// init paint
			//
			paint.setTextSize(tag.m_font_size);
			tag.m_rect.setEmpty();
			paint.getTextBounds(text, 0, text.length(), tag.m_rect);
			int font_height = (int)(paint.descent() - paint.ascent());
			

			tag.m_rect.left += m_width/2 + pos_x;
			tag.m_rect.right += m_width/2 + pos_x;
			tag.m_rect.top += m_height/2 + pos_y;
			tag.m_rect.bottom += m_height/2 + pos_y + font_height;
			index++;
		}
		
		
	}
	*/
	
	
	/**
	 * reserves the rectangle for cloud tags
	 */
	private void reserveRectangleForCloudTags() {
		
		//
		// allocate new paint
		//
		Paint paint = new Paint();
		
		//
		// reserve rectangles for cloud tags
		//
		for(CloudTag tag : m_tags)
		{
			reserveRectangleForCloudTag(paint, tag);
		}
	}
	
	/**
	 * reserves a rectangle for a cloud tag
	 * @param paint paint object to use for painting 
	 * @param tag whose rectangle should be reserved
	 */
	private void reserveRectangleForCloudTag(Paint paint, CloudTag tag) {
		
		//
		// set paint color size
		//
		paint.setColor(tag.m_color);
		int font_size_reduction = 0;
		int min_width;
		int min_height;
		
		//
		// get cloud text
		//
		String text = getCloudItemText(tag);
		
		Rect free_rect = null;
		do
		{
			paint.setTextSize(tag.m_font_size - font_size_reduction);
		
		
			//
			// get text bounds
			//
			tag.m_rect.setEmpty();
			paint.getTextBounds(text, 0, text.length(), tag.m_rect);

			//
			// get text dimension in absolute coordinates
			//
			min_width = Math.abs(tag.m_rect.right - tag.m_rect.left) + (tag.m_font_size - font_size_reduction) / 2;
			min_height = Math.abs(tag.m_rect.bottom - tag.m_rect.top) + (tag.m_font_size - font_size_reduction) / 2;
		
			if (min_width < m_width && min_height < m_height)
			{
				//
				// get free rectangle
				//
				free_rect = m_boxmgr.getRectangleWithDimension(min_width, min_height);
			}
			
			if (free_rect == null)
			{
				//
				// decrease font size
				//
				font_size_reduction += FONT_STEP_SIZE;
				Logger.i("tag: " + tag.m_tag + " reduced: " + font_size_reduction);
			}
			
		}while(free_rect == null);
		
		//
		// set empty
		//
		tag.m_rect.setEmpty();
		
		
		//
		// add offsets
		//
		tag.m_font_size -= font_size_reduction;
		tag.m_rect.left = free_rect.left;
		tag.m_rect.right = free_rect.left + min_width;
		tag.m_rect.top = free_rect.top;
		tag.m_rect.bottom = free_rect.top + min_height;
	}
	
	/**
	 * callback when a pointer has been moved
	 * @param pointer_index index of the pointer
	 * @param diff_x diff in x coordinates
	 * @param diff_y diff in y coordinates
	 */
	public void onPointerMove(int pointer_index, float diff_x, float diffy) {
		
		if (pointer_index != 0)
		{
			//
			// not primary pointer, ignore
			//
			return;
		}
		
		//
		// get current maximum height of the screen box manager
		//
		int max_height = m_boxmgr.getCurrentHeight();
		int diff_y = (int)diffy;
		
		if (diff_y > 0)
		{
			//
			// scroll down operation
			//
			Logger.i("DOWN: top: " + m_source_rect.top + " diff_y:" + Math.abs(diff_y) + " height:" + m_height + " max_height: " + max_height);
			if (m_source_rect.bottom + Math.abs(diff_y) <= max_height)
			{
				m_source_rect.top += Math.min(Math.abs(diff_y), max_height - m_source_rect.bottom);
				m_source_rect.bottom += Math.min(Math.abs(diff_y), max_height - m_source_rect.bottom);
				
				//
				// force repaint
				//
				postInvalidate();
			}
		}
		else
		{
			//
			// scroll up operation
			//
			Logger.i("UP: top: " + m_source_rect.top + " diff_y:" + Math.abs(diff_y) + " height:" + m_height + " max_height: " + max_height);
			if (m_source_rect.top + diff_y >= 0)
			{
				//
				// don't scroll up beyond beginning
				//
				m_source_rect.top = (int)Math.max(m_source_rect.top + diff_y, 0.0);
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
		// construct new paint
		//
		Paint paint = new Paint();

		//
		// draw bitmap
		//
		canvas.drawBitmap(m_bitmap, m_source_rect, m_target_rect, paint);
	}

	public void changeBG(int r, int g, int b) {

		System.out.println("change bg: " + r + "," + g + "," + b);

		setBackgroundColor(Color.rgb(r, g, b));

		invalidate();

	}

}