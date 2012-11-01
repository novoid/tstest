package org.me.tagstore.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeFormatUtility {

	/**
	 * time zone utility
	 */
	public static final String TIME_ZONE = "UTC";
	
	/**
	 * time format
	 */
	public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * returns the current time in UTC
	 * @return
	 */
	public static String getCurrentTimeInUTC() {
		
		//
		// construct date format
		//
		SimpleDateFormat date_format = new SimpleDateFormat(
				TIME_FORMAT);

		//
		// set utc format
		//
		date_format.setTimeZone(TimeZone.getTimeZone(TimeFormatUtility.TIME_ZONE));
		
		//
		// format date
		//
		String date = date_format.format(new Date());
		
		//
		// done
		//
		return date;
	}

	/**
	 * constructs an date object from a string which holds a date in utc time and which was built according to above format
	 * @param date input date
	 * @return Date object
	 */
	public static Date parseDate(String date, TimeZone timezone) {
		
		//
		// construct date format
		//
		SimpleDateFormat date_format = new SimpleDateFormat(
				TimeFormatUtility.TIME_FORMAT);

		//
		// set utc format
		//
		date_format.setTimeZone(timezone);
		
		
		//
		// construct date object
		//
		try {

			//
			// parse date object
			//
			Date date_object = date_format.parse(date);
			
			//
			// done
			//
			return date_object;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * converts an date object to local time
	 * @param date_object
	 * @return string representation in local time
	 */
	public static String convertDateToLocalTime(Date date_object) {
		
		//
		// construct date format
		//
		SimpleDateFormat date_format = new SimpleDateFormat(
				TimeFormatUtility.TIME_FORMAT);

		//
		// set local time zone
		//
		date_format.setTimeZone(TimeZone.getDefault());
		
		
		//
		// format date
		//
		String date = date_format.format(date_object);
		
		
		//
		// done
		//
		return date;
	}

	/**
	 * converts a date object in local time to string representation in utc time
	 * @param date_object
	 * @return
	 */
	public static String convertDateToUTC(Date date_object) {
		
		//
		// construct date format
		//
		SimpleDateFormat date_format = new SimpleDateFormat(
				TimeFormatUtility.TIME_FORMAT);

		//
		// set local time zone
		//
		date_format.setTimeZone(TimeZone.getTimeZone(TimeFormatUtility.TIME_ZONE));
		
		
		//
		// format date
		//
		return date_format.format(date_object);
	}
	
	/**
	 * converts a string representing utc time to a string in local time
	 * @param date_string date string
	 * @return date string in local time
	 */
	public static String convertStringToLocalTime(String date_string) {
		
		//
		// parse the date
		//
		Date date_object = parseDate(date_string, TimeZone.getTimeZone(TimeFormatUtility.TIME_ZONE));
		
		//
		// now convert it
		//
		return convertDateToLocalTime(date_object);
	}
	
	/**
	 * converts a string representing utc time to a string in local time
	 * @param date_string date string
	 * @return date string in local time
	 */
	public static String convertStringToUTCTime(String date_string) {
		
		//
		// parse the date
		//
		Date date_object = parseDate(date_string, TimeZone.getDefault());
		
		//
		// now convert it
		//
		return convertDateToUTC(date_object);
	}
	
}
