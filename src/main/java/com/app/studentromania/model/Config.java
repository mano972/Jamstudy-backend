package com.app.studentromania.model;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class Config extends ParentEntity {

	public Config() {
		super(DocTypeEnum.CONFIG);
	}

	@Field
	private String configKey;

	@Field
	private String configValue;

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

}
