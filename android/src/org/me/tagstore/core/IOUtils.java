package org.me.tagstore.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.Context;

public class IOUtils {

	/**
	 * reads a file into the specified result vector
	 * 
	 * @param reader
	 *            buffered reader
	 * @param lines
	 *            lines to read
	 * @return
	 */
	public boolean readFile(BufferedReader reader, ArrayList<String> lines) {

		try {
			do {
				//
				// read line
				//
				String line = reader.readLine();
				if (line == null)
					break;

				//
				// append line
				//
				lines.add(line);

			} while (true);

			//
			// successfully read file
			//
			return true;

		} catch (IOException exc) {
			Logger.e("IOException while reading");
			return false;
		}
	}

	/**
	 * reads the content of a file into a result line vector
	 * 
	 * @param file_name
	 *            name of the file
	 */
	public ArrayList<String> readFile(String file_name) {
		BufferedReader reader = null;
		ArrayList<String> lines = new ArrayList<String>();

		try {
			//
			// open input stream
			//
			FileInputStream raw_stream = new FileInputStream(file_name);

			//
			// create reader
			//
			reader = new BufferedReader(new InputStreamReader(raw_stream,
					"UTF8"));

			//
			// read file
			//
			boolean result = readFile(reader, lines);
			if (!result)
				return null;
		} catch (IOException exc) {
			Logger.e("Exception while reading file: " + file_name);
			return null;
		} finally {
			//
			// close file objects
			//
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//
		// return result
		//
		return lines;
	}

	/**
	 * copys the contents of the file
	 * 
	 * @param reader
	 *            reader of resource file
	 * @param writer
	 *            writer of the target file
	 */
	public boolean copyFile(BufferedReader reader, BufferedWriter writer) {

		try {
			boolean first = true;
			do {
				//
				// read line
				//
				String line = reader.readLine();

				if (line == null)
					break;

				if (first)
					first = false;
				else
					writer.newLine();

				//
				// write line
				//
				writer.write(line);
			} while (true);

			//
			// done
			//
			writer.flush();
			return true;
		} catch (IOException exc) {
			//
			// exception while writing
			//
			exc.printStackTrace();
			return false;
		}
	}

	/**
	 * copies the resource
	 * 
	 * @param context
	 *            application context
	 * @param resource_name
	 *            name of the resource
	 * @param target_name
	 *            target file name
	 */
	public boolean copyResource(Context context, String resource_name,
			File target_name) {

		BufferedReader reader = null;
		BufferedWriter writer = null;
		boolean result = false;

		try {
			//
			// open input stream
			//
			InputStream raw_stream = context.getAssets().open(resource_name);

			//
			// create reader
			//
			reader = new BufferedReader(new InputStreamReader(raw_stream,
					"UTF8"));

			//
			// create writer
			//
			writer = new BufferedWriter(new FileWriter(target_name, false));

			//
			// copy file
			//
			result = copyFile(reader, writer);
		} catch (IOException exc) {
			Logger.e("Exception while copying resource: " + resource_name);
		} finally {
			//
			// close file objects
			//
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//
		// return result
		//
		return result;
	}

	public boolean copyFile(File sourceFile, File destFile) {
		if (!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Logger.e("file: " + destFile);
				return false;
			}
		}

		FileChannel source = null;
		FileChannel destination = null;
		boolean ret = true;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (IOException e) {
			ret = false;
		} finally {
			try {
				if (source != null) {
					source.close();
				}
			} catch (IOException e) {
			}
			try {
				if (destination != null) {
					destination.close();
				}
			} catch (IOException e) {
			}
		}
		return ret;
	}

}
