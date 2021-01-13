package com.app.studentromania.enumtype;

import org.springframework.http.HttpStatus;

public enum ErrorsEnumEng {

	NO_ERROR(HttpStatus.OK, "Success", 0),
	GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error occured.", -1),
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occured.", -2),

	// Config
	CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "Config was not found.", -3),

	// Admin Login
	ADMIN_LOGIN_MISSING_HEADER(HttpStatus.BAD_REQUEST, "Authentication header missing.", -4),
	ADMIN_LOGIN_WRONG_CREDENTIALS(HttpStatus.BAD_REQUEST, "Admin username/password not correct", -5),

	// Faculty
	FACULTY_NOT_FOUND(HttpStatus.NOT_FOUND, "Faculty was not found.", -10),
	FACULTY_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Faculty already exists.", -11),
	FACULTY_ID_MISSING(HttpStatus.BAD_REQUEST, "Faculty id is missing.", -12),
	FACULTY_LOGO_ERROR(HttpStatus.NOT_FOUND, "There was an error when retrieving the faculty logo.", -13),

	// University
	UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "University was not found.", -20),
	UNIVERSITY_EXISTS(HttpStatus.NOT_ACCEPTABLE, "University already exists.", -21),
	UNIVERSITY_ID_MISSING(HttpStatus.BAD_REQUEST, "University id is missing.", -22),

	// Review
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Review was not found.", -30),
	REVIEW_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Review already exists.", -31),
	REVIEW_ID_MISSING(HttpStatus.BAD_REQUEST, "Review id is missing.", -32),
	REVIEW_GENERAL_RATING_ERROR(HttpStatus.BAD_REQUEST, "General Rating value is not correct.", -33),
	REVIEW_DIFFICULTY_ERROR(HttpStatus.BAD_REQUEST, "Difficulty value is not correct.", -34),

	// Question
	QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Question was not found.", -40),

	// Answer
	ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Answer was not found.", -50),

	// Review
	USERPROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "User profile was not found.", -60),
	USERPROFILE_EXISTS(HttpStatus.NOT_ACCEPTABLE, "User already exists.", -61),
	USERPROFILE_LOGIN_WRONG_CREDENTIALS(HttpStatus.BAD_REQUEST, "Email/password not correct.", -62),
	USERPROFILE_EMAIL_PASSWORD_MISSING(HttpStatus.BAD_REQUEST, "Email/password missing.", -63),

	// Statistic
	ANALYTICS_NOT_FOUND(HttpStatus.NOT_FOUND, "Analytics document was not found.", -70),
	ANALYTICS_DOCUMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Analytics document already exists.", -71),

	// Newsletter
	NEWSLETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "Newsletter was not found.", -80),
	NEWSLETTER_EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Email already exists.", -81),
	NEWSLETTER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Newsletter document already exists.", -82);

	private String errorDescription;
	private int errorCode;
	private HttpStatus httpStatus;

	private ErrorsEnumEng(HttpStatus httpStatus, String errorDescription, int errorCode) {
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
