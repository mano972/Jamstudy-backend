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
	public static final String CONFIG_KEY_REGISTER_CONFIRMATION_EMAIL_SUBJECT = "register_confirmation_email_subject";
	public static final String CONFIG_KEY_REGISTER_CONFIRMATION_EMAIL_TEXT = "register_confirmation_email_text";
	public static final String CONFIG_KEY_REGISTER_CONFIRMATION_EMAIL_URL = "register_confirmation_email_url";
	public static final String CONFIG_KEY_RESET_PASS_EMAIL_SUBJECT = "reset_pass_email_subject";
	public static final String CONFIG_KEY_RESET_PASS_EMAIL_TEXT = "reset_pass_email_text";
	public static final String CONFIG_KEY_RESET_PASS_EMAIL_URL = "reset_pass_email_url";

	public static final String DATE_FORMAT = "dd MMM yyyy";
	public static final String BIRTH_DATE_FORMAT = "yyyy-MM-dd";

	public static final String JWT_TOKEN_REQUEST_HEADER = "Token";

	public static final int MAX_FAVORITE_FACULTIES = 10;
	public static final int MAX_REVIEWS = 5;
	public static final int SAME_FACULTY_REVIEW_DAYS = 150;
	public static final int REVIEW_MIN_LENGTH = 30;
	public static final int REVIEW_MAX_LENGTH = 1400;
	

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	public static final Pattern VALID_PASSWORD_REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-z][A-Z]).{7,22}$",
			Pattern.CASE_INSENSITIVE);
	
	public static final String DEFAULT_REGISTER_CONFIRMATION_EMAIL_SUBJECT = "Confirma crearea unui cont nou pe platforma Unistart";
	public static final String DEFAULT_REGISTER_CONFIRMATION_EMAIL_TEXT = "Bun venit in comunitatea Unistart! \n \nAcceseaza link-ul pentru a confirma crearea unui cont nou. \n \n %s \n \nFoloseste platforma Unistart pentru a gasi facultatea potrivita pentru tine. \nIn cazul in care esti student sau absolvent, lasa o evaluare facultatii tale si ajuta un elev sa ia decizia potrivita. \n \nCu drag, \nEchipa Unistart";
	public static final String DEFAULT_REGISTER_CONFIRMATION_EMAIL_URL = "https://unistart.ro/login.html?token=";

	public static final String DEFAULT_RESET_PASS_EMAIL_SUBJECT = "Reseteaza parola contului tau Unistart";
	public static final String DEFAULT_RESET_PASS_EMAIL_TEXT = "Salut%s! \n \nAi cerut resetarea parolei pentru contul tau Unistart. Te rugam acceseaza link-ul de mai jos. \n \n %s \n \nIn cazul in care nu ai solicitat schimbarea parolei, este in regula sa nu intreprinzi o actiune. Daca esti îngrijorat de securitatea contului tau, iti recomandam sa ne scrii la office@unistart.ro \n \n Cu drag, \nEchipa Unistart";
	public static final String DEFAULT_RESET_PASS_EMAIL_URL = "https://unistart.ro/change.html?token=";

}
