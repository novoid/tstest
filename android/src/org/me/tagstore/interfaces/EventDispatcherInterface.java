package org.me.tagstore.interfaces;

import java.util.Iterator;
import java.util.Map.Entry;

import org.me.tagstore.core.EventDispatcher.EventId;
import org.me.tagstore.core.EventDispatcher.EventDetails;

public interface EventDispatcherInterface {

	/**
	 * registers an event observer which receives a callback when the event is signaled
	 * @param id event id 
	 * @param callback callback object
	 * @return true on success
	 */
	public abstract boolean registerEvent(EventId id, Object callback);

	/**
	 * unregisters the provided callback from the notification function
	 * @param id event id
	 * @param callback callback id
	 * @return true on success
	 */
	public abstract boolean unregisterEvent(EventId id, Object callback);

	/**
	 * signals an event with the given event arguments
	 * @param id event ids
	 * @param event_args arguments to be passed to registered callbacks
	 * @return true on success
	 */
	public abstract boolean signalEvent(EventId id, Object[] event_args);

	/**
	 * returns whether an object has registered the specific event id
	 * @param id event id to check
	 * @param callback object implementing the event id
	 * @return true on success
	 */
	public abstract boolean isEventRegistered(EventId id, Object callback);

	/**
	 * returns an iterator on the current event id map
	 * @return
	 */
	public abstract Iterator<Entry<String, EventDetails>> getEventDetailsIterator();
}