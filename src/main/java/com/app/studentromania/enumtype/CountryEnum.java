package com.app.studentromania.enumtype;

public enum CountryEnum {

	RO("RO"), LT("LT");

	private final String countryText;

	CountryEnum(String countryText) {
		this.countryText = countryText;
	}

	public String getValue() {
		return countryText;
	}
}
