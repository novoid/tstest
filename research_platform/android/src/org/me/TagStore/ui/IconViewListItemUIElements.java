package org.me.TagStore.ui;


import org.me.TagStore.R;
import org.me.TagStore.interfaces.IconViewClickListenerCallback;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * stores the user interface elements
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
	 * sets the item text and image
	 * 
	 * @param item_name
	 *            text of the item
	 * @param item_image
	 *            image of the item
	 */
	public void setItemNameAndImage(String item_name,
			Integer item_image) {

		if (m_ItemName1 != null) {
			m_ItemName1.setText(item_name);
		}

		if (m_ItemButton1 != null) {
			
			Drawable icon = m_context.getResources().getDrawable(item_image.intValue());
			m_ItemButton1.setImageDrawable(icon);
			m_ItemButton1.setBackgroundColor(Color.BLACK);
		} 
	}

	/**
	 * initializes all view elements
	 * 
	 * @param convertView
	 *            elements which are retrieved from this view
	 * @param num_elements_per_row
	 *            num elements per row
	 */
	public void initializeWithView(IconViewClickListenerCallback callback,
			int item_offset, View convertView) {

		//
		// initialize first element
		//
		m_ItemName1 = (TextView) convertView
				.findViewById(R.id.tag_name_one);
		m_ItemButton1 = (ImageButton) convertView
				.findViewById(R.id.tag_image_one);

		if (m_ItemButton1 != null) {
			m_ItemButton1.setOnClickListener(new IconViewListItemClickListener(
					callback, item_offset));
			m_ItemButton1
					.setOnLongClickListener(new IconViewListItemLongClickListener(
							callback, item_offset));
		}

		if (m_ItemName1 != null) {
			m_ItemName1.setOnClickListener(new IconViewListItemClickListener(
					callback, item_offset));
			m_ItemName1
					.setOnLongClickListener(new IconViewListItemLongClickListener(
							callback, item_offset));
		}
	}
}