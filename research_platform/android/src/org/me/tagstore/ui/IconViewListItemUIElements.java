package org.me.tagstore.ui;

import org.me.tagstore.R;
import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * stores the user interface elements for the icon view
 * 
 * 
 */
public class IconViewListItemUIElements {

	/**
	 * stores user interface elements
	 */
	private TextView m_ItemName1;
	private ImageButton m_ItemButton1;
	private Context m_context;
	private EventDispatcherInterface m_event_interface;

	/**
	 * sets the item name and image and sets the callbacks
	 * 
	 * @param display_name
	 *            display item name - text which is shown on the screen
	 * @param item_name
	 *            text which is returned during event handling
	 * @param item_image
	 *            image resource id
	 * @param is_tag
	 *            if the item is a tag
	 */
	public void setItemNameAndImage(String display_name, String item_name,
			Integer item_image, boolean is_tag) {

		if (m_ItemName1 != null) {

			m_ItemName1.setText(display_name);

			// set click listener
			IconViewListItemClickListener click_listener = new IconViewListItemClickListener();
			click_listener.initIconViewListItemClickListener(item_name, is_tag,
					m_event_interface);
			m_ItemName1.setOnClickListener(click_listener);

			// set long click listener
			IconViewListItemLongClickListener long_click_listener = new IconViewListItemLongClickListener();
			long_click_listener.initIconViewListItemLongClickListener(
					item_name, is_tag, m_event_interface);
			m_ItemName1.setOnLongClickListener(long_click_listener);

		}

		if (m_ItemButton1 != null) {

			Drawable icon = m_context.getResources().getDrawable(
					item_image.intValue());
			m_ItemButton1.setImageDrawable(icon);
			m_ItemButton1.setBackgroundColor(Color.BLACK);

			// set click listener
			IconViewListItemClickListener click_listener = new IconViewListItemClickListener();
			click_listener.initIconViewListItemClickListener(item_name, is_tag,
					m_event_interface);
			m_ItemButton1.setOnClickListener(click_listener);

			// set long click listener
			IconViewListItemLongClickListener long_click_listener = new IconViewListItemLongClickListener();
			long_click_listener.initIconViewListItemLongClickListener(
					item_name, is_tag, m_event_interface);
			m_ItemButton1.setOnLongClickListener(long_click_listener);
		}
	}

	/**
	 * initializes with the provided view
	 * 
	 * @param convertView
	 */
	public void initializeIconViewListItemUIElements(View convertView,
			Context context, EventDispatcherInterface event_interface) {

		//
		// initialize class
		//
		m_ItemName1 = (TextView) convertView.findViewById(R.id.tag_name_one);
		m_ItemButton1 = (ImageButton) convertView
				.findViewById(R.id.tag_image_one);
		m_context = context;
		m_event_interface = event_interface;

	}
}