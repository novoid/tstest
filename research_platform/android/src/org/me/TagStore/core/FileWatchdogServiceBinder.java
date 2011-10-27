package org.me.TagStore.core;

//import java.lang.ref.WeakReference;

import android.os.Binder;

public class FileWatchdogServiceBinder<S> extends Binder {

	/**
	 * stores weak reference to service
	 */
	private S mService;

	/**
	 * constructor of class service binder
	 * 
	 * @param service
	 */
	public FileWatchdogServiceBinder(S service) {
		mService = service; // new WeakReference<S>(service);
	}

	public S getService() {
		return mService;
	}
}
