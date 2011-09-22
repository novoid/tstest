package org.me.TagStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.SharedPreferences;

public class FileHashsumGenerator {

	
	/**
	 * returns the current default algorithm used for hashing the files
	 * @param context to retrieve default settings
	 * @return name of algorithm
	 */
	public static String getCurrentAlgorithmName(Context context) {
		
		//
		// acquire shared settings
		//
		SharedPreferences settings = context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		
		//
		// get default algorithm
		//
		String algorithm = settings.getString(ConfigurationSettings.CURRENT_HASH_SUM_ALGORITHM, ConfigurationSettings.DEFAULT_HASH_SUM_ALGORITHM);
		
		//
		// return default algorithm
		//
		return algorithm;
	}
	
	public static String generateFileHashsum(Context context, String file_name) {
		
		//
		// construct file object
		//
		File file = new File(file_name);
		
		//
		// check if file exists and is readable
		//
		if (!file.exists() || !file.canRead())
		{
			//
			// file not available
			//
			Logger.e("Error: failed to generate hash sum File " + file_name + " exists: " + file.exists() + " readable: " + file.canRead());
			return null;
		}
			
		//
		// get current algorithm
		//
		String algorithm = getCurrentAlgorithmName(context);
		
		//
		// result buffer
		//
		String result = null;
		FileInputStream in_stream = null;
		
		try {
			//
			// create instance of hash algorithm 
			//
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			
			//
			// open file input stream
			//
			in_stream = new FileInputStream(file);
			
			//
			// read file 
			//
			byte[] buffer = new byte[1024];
			
			do
			{
				int length = -1;
				

					//
					// read into buffer
					//
					length = in_stream.read(buffer);
					

				if (length < 0)
					break;
				
				
				//
				// update message digest
				//
				digest.update(buffer, 0, length);
				
			
			}while(true);
			
			//
			// get the message digest
			//
			byte [] message = digest.digest();
			
			//
			// construct string buffer
			//
			StringBuffer hex_buffer = new StringBuffer();
			
			for(byte hex : message)
			{
				//
				// convert integer to hex
				//
				String hex_string = Integer.toHexString(hex & 0xFF);
				
				if (hex_string.length() < 2)
				{
					//
					// hex string is 8
					// 
					hex_string = "0" + hex_string;
				}
				
				//
				// append hex string
				//
				hex_buffer.append(hex_string);
			}
			
			//
			// get hex buffer
			//
			result = hex_buffer.toString();
			
		} catch (NoSuchAlgorithmException e) {

			//
			// algorithm not available
			//
			Logger.e("Error: algorithm " + algorithm + " not available");
		}
		catch (FileNotFoundException e) {
			
			//
			// file not found exception
			//
			Logger.e("Error: file " + file.getName() + " not found");
		}
		catch(IOException e)
		{
			//
			// io exception
			//
			Logger.e("IOException file: " + file.getName());
		}
		
		try
		{
			if (in_stream != null)
				in_stream.close();
			
		}
		catch(IOException e)
		{
			//
			// IOException when closing the file
			//
			Logger.e("IOException when closing file: " + file.getName());
		}
		
		//
		// done
		//
		return result;
	}
}
