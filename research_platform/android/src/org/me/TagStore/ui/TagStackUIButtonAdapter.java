package org.me.TagStore.ui;

import java.util.Iterator;

import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.interfaces.TagStackUIButtonCallback;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This class manages the stack of tag items which have been used
 * @author Johnseyii
 *
 */
public class TagStackUIButtonAdapter
{
	/**
	 * stores the buttons
	 */
	Button[] m_buttons;
	
	/**
	 * stores the callback
	 */
	private TagStackUIButtonCallback m_callback;
	
	/**
	 * constructor of class TagStackAdapter
	 */
	public TagStackUIButtonAdapter(TagStackUIButtonCallback callback, Button[] buttons) {
		
		//
		// allocate button array
		//
		m_buttons = buttons;
		
		//
		// store the callback
		//
		m_callback = callback;
		
		//
		// initialize buttons
		//
		initialize();
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
	 * initialize the members
	 */
	private void initialize() {
		
		//
		// set all empty
		//
		for(Button button : m_buttons)
		{
			if (button != null)
			{
				//
				// clear text and hide
				//
				button.setText("");
				button.setVisibility(View.INVISIBLE);
				
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						
						//
						// get button
						//
						Button button = (Button)arg0;
						
						//
						// get button text
						//
						String tag = (String)button.getText();
						
						if (m_callback != null)
						{
							//
							// refresh view
							//
							m_callback.tagButtonClicked(tag);
						}
					}
				});
				
			}
		}
	}
}