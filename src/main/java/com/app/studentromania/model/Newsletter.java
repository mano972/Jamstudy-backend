package com.app.studentromania.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class Newsletter extends ParentEntity {

	public Newsletter() {
		super(DocTypeEnum.NEWSLETTER);
	}

	@Field
	private List<String> emails = new ArrayList<String>();

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

}
