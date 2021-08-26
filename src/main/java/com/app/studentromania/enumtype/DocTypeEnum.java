package com.app.studentromania.enumtype;

public enum DocTypeEnum {

	// @formatter:off
	FACULTY("FACULTY"), 
	UNIVERSITY("UNIVERSITY"), 
	REVIEW("REVIEW"), 
	USER_PROFILE("USER_PROFILE"), 
	CONFIG("CONFIG"),
	QUESTION("QUESTION"), 
	ANALYTICS("ANALYTICS"), 
	NEWSLETTER("NEWSLETTER"),
	FEEDBACK("FEEDBACK"),
	ARCHIVED_REVIEW("ARCHIVED_REVIEW");
	// @formatter:on

	private final String docTypeText;

	DocTypeEnum(String docTypeText) {
		this.docTypeText = docTypeText;
	}

	public String getValue() {
		return docTypeText;
	}

	@Override
	public String toString() {
		return docTypeText;
	}
}
