package org.me.TagStore.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import junit.framework.Assert;


public class EventDispatcher {

	public enum EventId
	{
		BACK_KEY_EVENT, // back key was pressed
		ITEM_CLICK_EVENT, // an item was pressed
		ITEM_LONG_CLICK_EVENT, // an item was pressed for a long time
		ITEM_MENU_EVENT, // an menu item of an dialog was pressed
		DATABASE_RESET_EVENT, // database was reset
		FILE_RENAMED_EVENT,   // a file was renamed
		TAG_RENAMED_EVENT,    // a tag was renamed
		FILE_RETAG_EVENT,     // a file was re-tagged
		ITEM_CONFLICT_EVENT,  // conflict dialog (options/replace) selection was made		
		SYNC_COMPLETE_EVENT,  // sync task completed
		FILE_TAGGED_EVENT,    // a file was tagged event
		TAG_STACK_BUTTON_EVENT, // a tag button was pressed
		TAG_STACK_LONG_BUTTON_EVENT, // a long tag button was pressed
		SYNC_DOWNLOAD_EVENT,         // sync downloads a file
		SYNC_UPLOAD_EVENT,           // sync uploads a file
		SYNC_INFO_EVENT,             // sync information msg event
		SYNC_ERROR_EVENT,             // sync error msg event
        SYNC_FILE_INFO_EVENT,          // sync file info event
        SYNC_CONFLICT_EVENT           // sync conflict event
	}

	/**
	 * this class stores all information to a specific event dispatcher
	 * @author Johannes Anderwald
	 *
	 */
	private class EventDetails
	{
		/**
		 * name of the interface
		 */
		public String m_interface_name;
		
		/**
		 * name of the interface function
		 */
		public String m_interface_function;
		
		/**
		 * object dispatchers
		 */
		public HashSet<Object> m_objects;
		
		/**
		 * constructor of class EventDetails
		 * @param interface_name name of the interface
		 * @param interface_function interface function name
		 */
		public EventDetails(String interface_name, String interface_function) {
			
			//
			// init object
			//
			m_interface_name = interface_name;
			m_interface_function = interface_function;
			m_objects = new HashSet<Object>();
			
		}
	}


	/**
	 * stores instance of object dispatcher 
	 */
	private static EventDispatcher s_instance = null;
	
	/**
	 * holds the map event id -> event details
	 */
	private final HashMap<String, EventDetails> m_map;
	
	/**
	 * constructor of class EventDispatcher
	 */
	public EventDispatcher() {
		
		//
		// construct dispatcher
		//
		m_map = new HashMap<String, EventDetails>();
		
		//
		// initialize events
		//
		initialize();
	}

	/**
	 * acquires an instance of the event dispatcher
	 * @return
	 */
	public static EventDispatcher getInstance() {
		
		if (s_instance == null) {
			
			//
			// build instance
			//
			s_instance = new EventDispatcher();
		}
		
		return s_instance;
	}
	
	
	/**
	 * initializes the events
	 */
	private void initialize() {
		
		//
		// init event dispatcher
		//
		m_map.put(EventId.BACK_KEY_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.BackKeyCallback", "backKeyPressed"));
		m_map.put(EventId.TAG_STACK_BUTTON_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.TagStackUIButtonCallback", "tagButtonClicked"));
		m_map.put(EventId.TAG_STACK_LONG_BUTTON_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.TagStackUIButtonCallback", "tagButtonLongClicked"));
		m_map.put(EventId.ITEM_CLICK_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.ItemViewClickListener", "onListItemClick"));
		m_map.put(EventId.ITEM_LONG_CLICK_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.ItemViewClickListener", "onLongListItemClick"));
		m_map.put(EventId.FILE_TAGGED_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.TagEventNotification", "notifyFileTagged"));
		m_map.put(EventId.DATABASE_RESET_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.DatabaseResetCallback", "onDatabaseResetResult"));
		m_map.put(EventId.FILE_RENAMED_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.RenameDialogCallback", "renamedFile"));
		m_map.put(EventId.TAG_RENAMED_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.RenameDialogCallback", "renamedTag"));
		m_map.put(EventId.FILE_RETAG_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.RetagDialogCallback", "retaggedFile"));
		m_map.put(EventId.ITEM_MENU_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.GeneralDialogCallback", "processMenuFileSelection"));
		m_map.put(EventId.ITEM_CONFLICT_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.OptionsDialogCallback", "processOptionsDialogCommand"));
		m_map.put(EventId.SYNC_COMPLETE_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.SyncTaskCallback", "onSyncTaskCompletion"));
		m_map.put(EventId.SYNC_DOWNLOAD_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.SynchronizationAlgorithmCallback", "onDownloadFile"));
		m_map.put(EventId.SYNC_UPLOAD_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.SynchronizationAlgorithmCallback", "onUploadFile"));
		m_map.put(EventId.SYNC_INFO_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.SynchronizationAlgorithmCallback", "notifyInfo"));
		m_map.put(EventId.SYNC_ERROR_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.SynchronizationAlgorithmCallback", "notifyError"));
		m_map.put(EventId.SYNC_FILE_INFO_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.SynchronizationAlgorithmCallback", "notifyFileSyncInfo"));
		m_map.put(EventId.SYNC_CONFLICT_EVENT.name(), new EventDetails("org.me.TagStore.interfaces.SynchronizationAlgorithmCallback", "onNotifyConflict"));
	}
	
	/**
	 * registers an event observer which receives a callback when the event is signaled
	 * @param id event id 
	 * @param callback callback object
	 * @return true on success
	 */
	public boolean registerEvent(EventId id, Object callback) {
		
		//
		// is the event supported
		//
		if (!m_map.containsKey(id.name()))
		{
			//
			// not supported
			//
			Logger.e("Error: event " + id.name() + " not registered");
			return false;
		}
		
		//
		// get event details
		//
		EventDetails details = (EventDetails)m_map.get(id.name());
		
		//
		// add to object list
		//
		return details.m_objects.add(callback);
	}

	/**
	 * unregisters the provided callback from the notification function
	 * @param id event id
	 * @param callback callback id
	 * @return true on success
	 */
	public boolean unregisterEvent(EventId id, Object callback) {
		
		//
		// is the event supported
		//
		if (!m_map.containsKey(id.name()))
		{
			//
			// not supported
			//
			Logger.e("Error: unregisterEvent event " + id.name() + " not supported");
			return false;
		}
		
		//
		// get event details
		//
		EventDetails details = (EventDetails)m_map.get(id.name());
		
		//
		// remove callback
		//
		return details.m_objects.remove(callback);
	}
	
	/**
	 * signals an event with the given event arguments
	 * @param id event ids
	 * @param event_args arguments to be passed to registered callbacks
	 * @return true on success
	 */
	public boolean signalEvent(EventId id, Object [] event_args) {
		
		//
		// is there an event registered
		//
		if (!m_map.containsKey(id.name()))
		{
			Logger.e("Error: signalEvent no such event " + id.name() + " registered");
			return false;
		}
		
		//
		// get event details
		//
		EventDetails details = m_map.get(id.name());
		
		try
		{
			//
			// get class obj
			//
			@SuppressWarnings("rawtypes")
			Class class_obj = Class.forName(details.m_interface_name);
			
			//
			// sanity check
			//
			Assert.assertTrue(class_obj.isInterface());
			
			//
			// get interface methods
			//
			Method [] interface_methods = class_obj.getMethods(); 
			
			for(Method method : interface_methods)
			{
				if (method.getName().equals(details.m_interface_function))
				{
					//
					// enumerate all objects
					//
					for(Object obj : details.m_objects)
					{
						//
						// invoke registered callbacks
						//
						Logger.i("calling " + details.m_interface_function);
						method.invoke(obj, event_args);
					}
					
					//
					// completed successfully
					//
					return true;
				}
				
			}
			
			//
			// no such interface function found
			//
			Logger.e("Error: signalEvent no such interface function found");
			return false;
		}
		catch (IllegalArgumentException e) 
		{
			Logger.e("Error: signalEvent InvalidArgumentException");
			e.printStackTrace();
			return false;
		}
		catch (IllegalAccessException e) 
		{
			Logger.e("Error: signalEvent IllegalAccessException");
			e.printStackTrace();
			return false;
		}
		catch (InvocationTargetException e) 
		{
			Logger.e("Error: signalEvent InvocationTargetException");
			e.printStackTrace();
			return false;
		}catch (ClassNotFoundException e) 
		{
			Logger.e("Error: signalEvent ClassNotFoundException");
			e.printStackTrace();
			return false;
		}
	}
}
