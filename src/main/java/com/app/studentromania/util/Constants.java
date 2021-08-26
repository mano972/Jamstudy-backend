package com.app.studentromania.util;

import java.util.regex.Pattern;

public class Constants {

	public static final String UNIVERSITY_PREFIX_ID = "UNI";
	public static final String FACULTY_PREFIX_ID = "FAC";
	public static final String REVIEW_PREFIX_ID = "REV";
	public static final String QUESTION_PREFIX_ID = "QUE";
	public static final String ANSWER_PREFIX_ID = "ANS";
	public static final String USERPROFILE_PREFIX_ID = "USR";

	public static final String BUCKET = "JamstudyBucket";

	public static final String LOGO_PATH_ROOT = "C:\\logos_jamstudy\\";
	public static final String LOGO_IMAGE_TYPE = ".png";
	public static final String DEFAULT_IMAGE = "default_image";

	public static final String ANALYTICS_EXCEL_PATH = "C:\\UnistartAnalytics\\UnistartAnalytics.xlsx";

	public static final String CONFIG_KEY_FACULTIES_LICENSE_FILEPATH = "faculties_licence_filepath";
	public static final String CONFIG_KEY_FACULTIES_MASTER_FILEPATH = "faculties_master_filepath";
	public static final String CONFIG_KEY_ADMIN_USERNAME = "admin_username";
	public static final String CONFIG_KEY_ADMIN_PASS = "admin_pass";

	public static final String DATE_FORMAT = "dd MMM yyyy";
	public static final String BIRTH_DATE_FORMAT = "yyyy-MM-dd";

	public static final String JWT_TOKEN_REQUEST_HEADER = "Token";

	public static final int MAX_FAVORITE_FACULTIES = 10;
	public static final int MAX_REVIEWS = 5;

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	public static final Pattern VALID_PASSWORD_REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-z][A-Z]).{7,22}$",
			Pattern.CASE_INSENSITIVE);

}
