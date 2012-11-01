package org.me.tagstore.interfaces;

import org.me.tagstore.ui.FileDialogBuilder.MENU_ITEM_ENUM;

/**
 * This interface is used as a call-back mechanism which is invoked when a dialog menu is pressed
 * @author Johannes Anderwald
 *
 */
public interface GeneralDialogCallback
{
	public abstract void processMenuFileSelection(MENU_ITEM_ENUM selection);
}