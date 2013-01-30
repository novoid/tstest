package org.me.tagstore.core;

//import java.lang.ref.WeakReference;

import android.os.Binder;

public class FileWatchdogServiceBinder<S> extends Binder {

	/**
	 * stores weak reference to service
	 */
	private S mService;

	public void initializeFileWatchdogServiceBinder(S service) {
		mService = service;
	}

	public S getService() {
		return mService;
	}
}
