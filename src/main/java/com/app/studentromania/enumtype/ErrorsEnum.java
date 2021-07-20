package com.app.studentromania.enumtype;

import org.springframework.http.HttpStatus;

public enum ErrorsEnum {

	NO_ERROR(HttpStatus.OK, "Success", 0), GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Eroare.", -1),
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Eroare baza de date.", -2),

	// Config
	CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "Config was not found.", -3),

	// Admin Login
	ADMIN_LOGIN_MISSING_HEADER(HttpStatus.BAD_REQUEST, "Authentication header missing.", -4),
	ADMIN_LOGIN_WRONG_CREDENTIALS(HttpStatus.BAD_REQUEST, "Admin username/password not correct", -5),

	// Faculty
	FACULTY_NOT_FOUND(HttpStatus.NOT_FOUND, "Facultatea nu a fost gasita.", -10),
	FACULTY_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Facultea exista deja.", -11),
	FACULTY_ID_MISSING(HttpStatus.BAD_REQUEST, "Lipsete id-ul facultatii.", -12),
	FACULTY_LOGO_ERROR(HttpStatus.NOT_FOUND, "A aparut o eroare la incarcarea logo-ului.", -13),

	// University
	UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Universitatea nu a fost gasita.", -20),
	UNIVERSITY_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Universitatea exista deja.", -21),
	UNIVERSITY_ID_MISSING(HttpStatus.BAD_REQUEST, "Lipsete id-ul universitatii.", -22),

	// Review
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Evaluarea nu a fost gasita.", -30),
	REVIEW_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Evaluarea exista deja.", -31),
	REVIEW_ID_MISSING(HttpStatus.BAD_REQUEST, "Lipsete id-ul evaluarii", -32),
	REVIEW_GENERAL_RATING_ERROR(HttpStatus.BAD_REQUEST, "Nota acordata nu este corecta.", -33),
	REVIEW_DIFFICULTY_ERROR(HttpStatus.BAD_REQUEST, "Valoarea dificultatii nu este corecta.", -34),
	REVIEW_ALREADY_UPVOTED(HttpStatus.BAD_REQUEST, "Aceasta evaluare a fost deja votata.", -35),

	// Question
	QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Intrebarea nu a fost gasita.", -40),

	// Answer
	ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Raspunsul nu a fost gasit.", -50),

	// UserProfile
	USERPROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Nu exista user cu acest email.", -60),
	USERPROFILE_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Un user cu acest email exista deja.", -61),
	USERPROFILE_LOGIN_WRONG_CREDENTIALS(HttpStatus.BAD_REQUEST, "Combinatia email-parola este gresita.", -62),
	USERPROFILE_EMAIL_PASSWORD_MISSING(HttpStatus.BAD_REQUEST, "Email/parola nu au fost completate.", -63),
	JWT_GENERATION_ERROR(HttpStatus.BAD_REQUEST, "Eroare la generarea JWT.", -64),
	JWT_MISSING(HttpStatus.BAD_REQUEST, "Token-ul JWT lipseste din request.", -65),
	JWT_VERIFY_ERROR(HttpStatus.BAD_REQUEST, "Token-ul JWT nu este corect.", -66),
	JWT_EXPIRED(HttpStatus.BAD_REQUEST, "Token-ul JWT a expirat.", -67),

	// Statistic
	ANALYTICS_NOT_FOUND(HttpStatus.NOT_FOUND, "Analytics document was not found.", -70),
	ANALYTICS_DOCUMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Analytics document already exists.", -71),
	ANALYTICS_EXCEL_DOCUMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Analytics excel document already exists.", -72),
	ANALYTICS_EXCEL_DOCUMENT_ERROR(HttpStatus.BAD_REQUEST, "Analytics excel error.", -73),
	ANALYTICS_EXCEL_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Analytics excel document was not found.", -74),

	// Newsletter
	NEWSLETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "Newsletter was not found.", -80),
	NEWSLETTER_EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,
			"Deja ești abonat la newsletter cu această adresă de e-mail. Îți mulțumim!", -81),
	NEWSLETTER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Newsletter document already exists.", -82),
	
	//Feedback
	FEEDBACK_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Feedback document was not found.", -90),
	FEEDBACK_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Feedback document already exists.", -91);

	private String errorDescription;
	private int errorCode;
	private HttpStatus httpStatus;

	private ErrorsEnum(HttpStatus httpStatus, String errorDescription, int errorCode) {
		this.errorDescription = errorDescription;
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

}
