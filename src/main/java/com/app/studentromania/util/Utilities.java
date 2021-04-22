package com.app.studentromania.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Utilities {

	public static String getFormattedDate(Date date) {
		Locale roLocale = new Locale("ro", "RO");
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		String formattedDate = localDate.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT, roLocale));
		return formattedDate;
	}

}
