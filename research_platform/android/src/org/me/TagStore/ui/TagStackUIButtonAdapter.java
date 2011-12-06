package org.me.TagStore.ui;

import java.util.Iterator;



import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.interfaces.TagStackUIButtonCallback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;


/**
 * This class manages the stack of tag items which have been used
 * @author Johannes Anderwald
 *
 */
public class TagStackUIButtonAdapter extends RelativeLayout
{
	/**
	 * stores the buttons
	 */
	private final Button[] m_buttons;
	
	/**
	 * stores the click listener
	 */
	private final ButtonClickListener m_button_listener;
	
	/**
	 * stores the long click listener
	 */
	private final ButtonLongClickListener m_button_long_listener;
	
	/**
	 * stores the callback
	 */
	private TagStackUIButtonCallback m_callback;
	
	/**
	 * constructor of class TagStackUIButtonAdapter 
	 * @param context
	 */
	public TagStackUIButtonAdapter(Context context) {
		this(context, null);

	}

    public TagStackUIButtonAdapter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public TagStackUIButtonAdapter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        //
        // init objects
        //
    	m_buttons = new Button[5];
    	m_button_listener = new ButtonClickListener();
    	m_button_long_listener = new ButtonLongClickListener();
    	
    	
    	//
    	// init adapter
    	//
        init();
    }

    /**
     * sets the callback which is invoked when one of tag buttons is pressed
     * @param callback
     */
    public void setTagStackUIButtonCallback(TagStackUIButtonCallback callback) {
    	
    	m_callback = callback;
    }
    
    /**
     * inits the adapter
     */
    private void init() {

    	//
    	// initializes all buttons
    	//
    	for(int index = 0; index < 5; index++)
    	{
    		//
    		// init button
    		//
    		m_buttons[index] = new Button(getContext());
			m_buttons[index].setText("1");
			m_buttons[index].setVisibility(View.VISIBLE);
        	m_buttons[index].setId(index + 1);
			m_buttons[index].setOnClickListener(m_button_listener);
			m_buttons[index].setOnLongClickListener(m_button_long_listener);
			
    		//
    		// new layout param
    		//
        	LayoutParams layout_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,  
    				LayoutParams.WRAP_CONTENT);
    		
        	if (index == 0)
        	{
        		//
        		// align button left
        		//
            	layout_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        	}
        	else
        	{
        		//
        		// align button to left of previous button
        		//
        		layout_params.addRule(RelativeLayout.RIGHT_OF, m_buttons[index-1].getId());
        	}
        	
        	layout_params.leftMargin = 5;
        	layout_params.topMargin = 10;

        	
        	
        	//
        	// align top
        	//
        	layout_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        	
    		//
    		// add button to view
    		//
    		addView(m_buttons[index], layout_params);
    	}
		requestLayout();
		invalidate();
    }
    
	/**
	 * refreshes the available tag navigation button
	 */
	public void refresh() {
		
		//
		// get iterator
		//
		Iterator<String> it = TagStackManager.getInstance().getIterator();
		
		//
		// iterate until 5 buttons have been set or set is empty
		//
		int index = 0;
		
		while(it.hasNext() && index < m_buttons.length)
		{
			//
			// get tag
			//
			String tag = it.next();
			
			//
			// set button text
			//
			if (m_buttons[index] != null)
			{
				m_buttons[index].setText(tag);
				m_buttons[index].setVisibility(View.VISIBLE);
			}
			
			//
			// update index
			//
			index++;
		}
		
		//
		// hide rest of the buttons
		//
		if (index < m_buttons.length)
		{
			for(; index < m_buttons.length; index++)
			{
				if (m_buttons[index] != null)
				{
					m_buttons[index].setText("");
					m_buttons[index].setVisibility(View.INVISIBLE);
				}
			}
		}
	}
	
	/**
	 * implements click listener for the tag buttons
	 * @author Johannes Anderwald
	 *
	 */
	private class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {

			//
			// get button text
			//
			Button button = (Button)arg0;
			String tag = (String)button.getText();
			
			if (m_callback != null)
			{
				//
				// refresh view
				//
				m_callback.tagButtonClicked(tag);
			}
		}
	};

	/**
	 * implements long click listener for the tag buttons
	 * @author Johannes Anderwald
	 *
	 */
	private class ButtonLongClickListener implements OnLongClickListener {

		@Override
		public boolean onLongClick(View arg0) {
			//
			// get button text
			//
			Button button = (Button)arg0;
			String tag = (String)button.getText();
			
			if (m_callback != null)
			{
				//
				// refresh view
				//
				return m_callback.tagButtonLongClicked(tag);
			}
			
			//
			// not handled
			//
			return false;
		}
		
	};
	
}