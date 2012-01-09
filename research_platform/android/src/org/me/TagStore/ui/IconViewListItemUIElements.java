package org.me.TagStore.ui;


import org.me.TagStore.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * stores the user interface elements for the icon view
 * 
 * @author Johannes Anderwald
 * 
 */
public class IconViewListItemUIElements {

	/**
	 * stores user interface elements
	 */
	private TextView m_ItemName1;
	private ImageButton m_ItemButton1;

	/**
	 * stores context
	 */
	private final Context m_context;
	
	public IconViewListItemUIElements(Context context) {
		m_context = context;
		
	}

	/**
	 * sets the item name and image and sets the callbacks
	 * @param display_name display item name - text which is shown on the screen
	 * @param item_name text which is returned during event handling
	 * @param item_image image resource id
	 * @param is_tag if the item is a tag
	 */
	public void setItemNameAndImage(String display_name, String item_name,
			Integer item_image,
			boolean is_tag) {

		if (m_ItemName1 != null) {
			
			m_ItemName1.setText(display_name);
			m_ItemName1.setOnClickListener(new IconViewListItemClickListener(item_name, is_tag));
			m_ItemName1.setOnLongClickListener(new IconViewListItemLongClickListener(item_name, is_tag));			
			
		}

		if (m_ItemButton1 != null) {
			
			Drawable icon = m_context.getResources().getDrawable(item_image.intValue());
			m_ItemButton1.setImageDrawable(icon);
			m_ItemButton1.setBackgroundColor(Color.BLACK);
			
			m_ItemButton1.setOnClickListener(new IconViewListItemClickListener(item_name, is_tag));
			m_ItemButton1.setOnLongClickListener(new IconViewListItemLongClickListener(item_name, is_tag));
		} 
	}

	/**
	 * initializes with the provided view
	 * @param convertView
	 */
	public void initializeWithView(View convertView) {

		//
		// initialize first element
		//
		m_ItemName1 = (TextView) convertView
				.findViewById(R.id.tag_name_one);
		m_ItemButton1 = (ImageButton) convertView
				.findViewById(R.id.tag_image_one);

		
	}
}