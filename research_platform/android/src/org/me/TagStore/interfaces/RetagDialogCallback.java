package org.me.TagStore.interfaces;

/**
 * This interface is used for call back when the 'tag me' button in the retag dialog is clicked.
 * @author Johannes Anderwald
 *
 */
public interface RetagDialogCallback 
{
	/**
	 * this function is called when a file has been re-tagged.
	 * @param file_name file name which has been re-tagged
	 * @param new_tags new associated tags
	 */
	public abstract void retaggedFile(String file_name, String tag_text);
}