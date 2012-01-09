package org.me.TagStore.ui;

import junit.framework.Assert;

import org.me.TagStore.R;
import org.me.TagStore.core.Logger;
import org.me.TagStore.interfaces.TabPageIndicatorCallback;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;


/**
 * Implements a horizontal page adapter
 * Based on Jake Wharton sample
 * @author Johannes Anderwald
 *
 */
public class TabPageIndicator extends View implements ViewPager.OnPageChangeListener {

	/**
	 * stores the view pager
	 */
	private ViewPager m_view_pager;
	
	/**
	 * stores the callback which is queried to retrieve the titles
	 */
	private TabPageIndicatorCallback m_callback;
	
	/**
	 * footer padding
	 */
	private float m_footer_padding;
	
	/**
	 * footer line color
	 */
	private int m_footer_line_color;
	
	/**
	 * footer line height
	 */
	private float m_footer_line_height;
	
	/**
	 * footer triangle color
	 */
	private int m_footer_triangle_color;
	
	/**
	 * footer triangle height
	 */
	private float m_footer_triangle_height;
	
	/**
	 * text font color
	 */
	private int m_text_font_color;
	
	/**
	 * text font size
	 */
	private float m_text_font_size;
	
	
	/**
	 * stores paint object for footer line
	 */
	private final Paint m_paint_footer_line;
	
	
	/**
	 * stores paint object for the text
	 */
	private final Paint m_paint_text;
	
	/**
	 * stores paint object for the footer triangle
	 */
	private final Paint m_paint_footer_triangle;
	
	/**
	 * stores the path object for the footer line
	 */
	private Path m_path_footer_line;
	
	/**
	 * stores the path object of the footer triangle
	 */
	private Path m_path_footer_triangle;
	
	/**
	 * stores the current position
	 */
	int m_current_position;
	
	/**
	 * stores the page change listener
	 */
	ViewPager.OnPageChangeListener m_listener;
	
	
	/**
	 * constructor of class TabPageIndicator 
	 * @param context
	 */
	public TabPageIndicator(Context context) {
		this(context, null);

	}

    public TabPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public TabPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
		//
		// load resources
		//
        Resources res = getResources();

        //
        // load defaults
        //
        m_footer_line_color = res.getColor(R.color.default_footer_line_color);
        m_footer_line_height = res.getDimension(R.dimen.default_footer_line_height);
        m_footer_padding = res.getDimension(R.dimen.default_footer_padding);        
        m_footer_triangle_color = res.getColor(R.color.default_footer_triangle_color);
        m_footer_triangle_height = res.getDimension(R.dimen.default_footer_triangle_height);
        m_text_font_color = res.getColor(R.color.default_text_font_color);
        m_text_font_size = res.getDimension(R.dimen.default_text_font_size);

        
        //
        // init paint properties for footer line
        //
        m_paint_footer_line = new Paint();
        m_paint_footer_line.setColor(m_footer_line_color);
        m_paint_footer_line.setStyle(Paint.Style.FILL_AND_STROKE);
        m_paint_footer_line.setStrokeWidth(m_footer_line_height);
        
        //
        // init paint properties for the footer triangle
        //
        m_paint_footer_triangle = new Paint();
        m_paint_footer_triangle.setColor(m_footer_triangle_color);
        m_paint_footer_triangle.setStyle(Paint.Style.FILL_AND_STROKE);        
     
        //
        // init paint properties for text
        //
        m_paint_text = new Paint();
        m_paint_text.setColor(m_text_font_color);
        m_paint_text.setAntiAlias(true);
        m_paint_text.setTextSize(m_text_font_size);
        
        //
        // listener is null yet
        //
        m_listener = null;
    }
	
    /**
     * paints the text
     * @param canvas to paint the text
     */
    protected void paintText(Canvas canvas) {
    	
    	int width = getWidth();
    	int height = (int)(getHeight() / 2 + m_footer_padding);
    	
    	//Logger.e("Count: " + m_view_pager.getAdapter().getCount() + " Position: " + m_current_position);
    	
    	if (m_current_position > 0)
    	{
    		if (m_callback == null)
    		{
    			Logger.e("Error: no callback registered");
    			return;
    		}
    		
    		//
    		// get text of former view
    		//
    		String text = m_callback.getTitle(m_current_position - 1);
    		
    		//
    		// unset bold
    		//
    		m_paint_text.setFakeBoldText(false);
    		
    		//
    		// draw text
    		//
    		canvas.drawText(text, 0, height, m_paint_text);
    	}
    	
    	if (m_callback == null)
    	{
    		Logger.e("Error: no callback registered");
    		return;
    	}
    	
    	//
    	// get text of current view
    	//
    	String current_title = m_callback.getTitle(m_current_position);
    	
    	//
    	// set it bold
    	//
    	m_paint_text.setFakeBoldText(true);

    	//
    	// measure length
    	//
    	float current_length = m_paint_text.measureText(current_title);
    	
    	//
    	// calculate offset
    	//
    	int offset = width/2 - (int)current_length/2;
    	
		//
		// paint text
		//
		canvas.drawText(current_title, offset, height, m_paint_text);
		
		//
		// are there any items left
		//
		if (m_current_position + 1 <= m_view_pager.getAdapter().getCount() - 1)
		{
			//
			// get text of next view
			//
			String next_title = m_callback.getTitle(m_current_position + 1);
			
			
    		//
    		// unset bold
    		//
    		m_paint_text.setFakeBoldText(false);
    		
    		//
    		// measure text length
    		//
    		float text_length = m_paint_text.measureText(next_title);
    		
    		//
    		// offset is minus text length
    		//
    		offset = width - (int)text_length;
    		
    		//
    		// draw text
    		//
    		canvas.drawText(next_title, offset, height, m_paint_text);
		}
    }
    
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		
		int height = getHeight();
		float width = getWidth();
		
		
		//
		// paint text
		//
		paintText(canvas);
		
		//
		// paint footer
		//
		m_path_footer_line = new Path();
		m_path_footer_line.moveTo(0, height - m_footer_line_height);
		m_path_footer_line.lineTo(width, height - m_footer_line_height);
		m_path_footer_line.close();
        canvas.drawPath(m_path_footer_line, m_paint_footer_line);

		//
        // paint triangle
        //
        m_path_footer_triangle = new Path();
        m_path_footer_triangle.moveTo(width/2, height - m_footer_line_height - m_footer_triangle_height);
        m_path_footer_triangle.lineTo(width/2 + m_footer_triangle_height, height - m_footer_line_height);
        m_path_footer_triangle.lineTo(width/2 - m_footer_triangle_height, height - m_footer_line_height);
        m_path_footer_triangle.close();
        canvas.drawPath(m_path_footer_triangle, m_paint_footer_triangle);
	}


	public void setViewPager(ViewPager view_pager) {

		if (view_pager == null)
		{
			//
			// BUG view_pager is null
			//
			Logger.e("TabPageIndicator::setViewPager> view_pager is null");
			return;
		}
		
		//
		// get adapter
		//
		PagerAdapter adapter = view_pager.getAdapter();
		if (adapter == null)
		{
			//
			// no adapter was set
			//
			Logger.e("TabPageIndicator::setViewPager> no adapter set");
			return;
		}
		
		//
		// check if the interface is supported
		//
		if (!(adapter instanceof TabPageIndicatorCallback))
		{
			//
			// adapter must implement this
			//
			Logger.e("adapter does not implement callback");
			return;
		}
		
		//
		// store pager and callback
		//
		m_view_pager = view_pager;

		//
		// add on page change listener
		//
		m_view_pager.setOnPageChangeListener(this);
		
		//
		// save callback
		//
		m_callback = (TabPageIndicatorCallback)adapter;
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		
		int height_mode = MeasureSpec.getMode(heightMeasureSpec);
		int height_size = MeasureSpec.getMode(heightMeasureSpec);
        int width_mode = MeasureSpec.getMode(widthMeasureSpec);
        int width_size = MeasureSpec.getSize(widthMeasureSpec);

        //
        // requires spec mode exactly
        //
        Assert.assertTrue(width_mode == MeasureSpec.EXACTLY);

		if (height_mode != MeasureSpec.EXACTLY)
		{
			//
			// calculate height
			//
            float text_height = m_paint_text.descent() - m_paint_text.ascent();
			text_height += m_footer_line_height + m_footer_padding + m_footer_triangle_height;
			
			height_size = (int)text_height;
		}
        
		Logger.e("width:" +  width_size + " height:" + height_size);
		setMeasuredDimension(width_size, height_size);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		
		if (m_listener != null)
		{
			//
			// notify listener
			//
			m_listener.onPageScrollStateChanged(state);
		}
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		
		if (m_listener != null)
		{
			//
			// notify listener
			//
			m_listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}

		invalidate();
	}

	@Override
	public void onPageSelected(int position) {
		
		Logger.e("onPageSelected: " + position);
		
		if (m_listener != null)
		{
			//
			// notify listener
			//
			m_listener.onPageSelected(position);
		}
		
		//
		// save current page position
		//
		m_current_position = position;

		//
		// trigger repaint
		//
		invalidate();		
		
	}

	/**
	 * sets the current item as new selected index
	 * @param position to be set
	 */
	
	public void setCurrentItem(int position) {
		
		//
		// change tab
		//
		m_view_pager.setCurrentItem(position);
		
		//
		// save new index
		//
		m_current_position = position;
		
		//
		// trigger repaint
		//
		invalidate();
	}
	
	/**
	 * sets the on page change listener
	 * @param listener
	 */
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
		
		//
		// stores the listener
		//
		m_listener = listener;
	}
    
	/**
	 * returns the current position
	 * @return
	 */
	public int getCurrentItem() {
		
		//
		// returns the current position
		//
		return m_current_position;
	}
}
