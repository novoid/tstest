package org.me.tagstore.interfaces;

/**
 * this interface is used to signal completion when a database reset has been completed
 *
 */
public interface DatabaseResetCallback {

	/**
	 * this function is called when the database reset task has been completed
	 * @param success if true it has been successfully reset
	 */
	public abstract void onDatabaseResetResult(final boolean success);
}
