package org.me.TagStore.interfaces;

/**
 * This interface is used as a call-back mechanism which is invoked a button of the options dialog is pressed
 * @author Johannes Anderwald
 *
 */
public interface OptionsDialogCallback
{
	/**
	 * call back which is called when one options button is pressed
	 * @param file_name name of the file
	 * @param ignore if ignore is true, then the file should be ignored, otherwise it should be renamed
	 */
	public abstract void processOptionsDialogCommand(String file_name, boolean ignore);
}