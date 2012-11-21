package org.me.tagstore.core;

import org.me.tagstore.interfaces.StorageProvider;

import android.content.Context;
import android.content.SharedPreferences;

public class StorageProviderFactory {

	/**
	 * application context
	 */
	private Context m_context;

	/**
	 * initializes the storage provider factory
	 * 
	 * @param context
	 */
	public void initializeStorageProviderFactory(Context context) {

		// store context
		m_context = context;
	}

	/**
	 * retrieves the default synchronization provider
	 * 
	 * @return
	 */
	private String getCurrentSyncProvider() {

		// acquire shared settings
		SharedPreferences settings = m_context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		// returns the default synchronization type
		return settings.getString(
				ConfigurationSettings.CURRENT_SYNCHRONIZATION_TYPE,
				ConfigurationSettings.DEFAULT_SYNCHRONIZATION_TYPE);
	}

	/**
	 * acquires a storage provider given by its name
	 * 
	 * @param provider_name
	 *            provider name
	 * @return StorageProvider
	 */
	private StorageProvider getStorageProviderByName(String provider_name) {

		if (provider_name == null)
			return null;

		if (provider_name
				.equals(ConfigurationSettings.SYNCHRONIZATION_DROPBOX_TYPE)) {
			// acquire dropbox storage provider
			return DropboxStorageProvider.getInstance();
		} else if (provider_name
				.equals(ConfigurationSettings.SYNCHRONIZATION_SMB_TYPE)) {

			// acquire smb storage provider
			return SMBStorageProvider.getInstance();
		}

		// not supported
		return null;
	}

	/**
	 * returns the default storage provider
	 * 
	 * @return StorageProvider
	 */
	public StorageProvider getDefaultStorageProvider() {

		// get current sync provider name
		String provider_name = getCurrentSyncProvider();

		// get provider
		return getStorageProviderByName(provider_name);
	}

	/**
	 * retrieves a storage provider with a specific name
	 * 
	 * @param provider_name
	 *            provider name
	 * @return StorageProvider
	 */
	public StorageProvider getStorageProviderWithName(String provider_name) {
		return getStorageProviderByName(provider_name);
	}

}
