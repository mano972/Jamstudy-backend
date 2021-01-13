package com.app.studentromania.enumtype;

public enum ProgramTypeEnum {

	LICENSE("LICENSE"), MASTER("MASTER");

	private final String programTypeText;

	ProgramTypeEnum(String programTypeText) {
		this.programTypeText = programTypeText;
	}

	public String getValue() {
		return programTypeText;
	}
}
