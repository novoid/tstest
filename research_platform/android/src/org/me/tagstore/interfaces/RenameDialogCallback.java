package org.me.tagstore.interfaces;

/**
 * This interface is used as a callback which is invoked when rename button is clicked in the rename dialog
 * @author Johannes Anderwald
 *
 */
public interface RenameDialogCallback
{
	/**
	 * This call back is invoked when a file has been renamed
	 * @param old_file_name old name of the file
	 * @param new_file new name of the file
	 */
	public abstract void renamedFile(String old_file_name, String new_file);
	
	/**
	 * This call back is invoked when a tag has been renamed
	 */
	public abstract void renamedTag(String old_tag_name, String new_tag_name);
	
}