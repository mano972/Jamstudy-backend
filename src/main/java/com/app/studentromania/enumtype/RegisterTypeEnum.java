package com.app.studentromania.enumtype;

public enum RegisterTypeEnum {

	REGULAR("REGULAR"), FACEBOOK("FACEBOOK");

	private final String registerTypeText;

	RegisterTypeEnum(String registerTypeText) {
		this.registerTypeText = registerTypeText;
	}

	public String getValue() {
		return registerTypeText;
	}

}
