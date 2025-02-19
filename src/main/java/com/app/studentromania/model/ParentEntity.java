package com.app.studentromania.model;

import java.util.Date;
import java.util.UUID;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.util.Utilities;
import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;

public abstract class ParentEntity {

	@Id
	private String id;

	@Field
	private Date creationDate;

	@Field
	private String formattedCreationDate;

	@Field
	private Date updateDate;

	@Field
	private String formattedUpdateDate;

	@Field
	private String docType;

	@Field
	private String countryCode;

	public ParentEntity(DocTypeEnum docType) {
		initParentEntity(docType);
		setId(UUID.randomUUID().toString());
	}

	public void initParentEntity(DocTypeEnum docType) {
		Date date = new Date();
		setCreationDate(date);
		setFormattedCreationDate(Utilities.getFormattedDate(date));
		setUpdateDate(date);
		setFormattedUpdateDate(Utilities.getFormattedDate(date));
		setDocType(docType.toString());
	}

	public void setUpdateDateWithCurrentDate() {
		Date date = new Date();
		setUpdateDate(date);
		setFormattedUpdateDate(Utilities.getFormattedDate(date));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getFormattedCreationDate() {
		return formattedCreationDate;
	}

	public void setFormattedCreationDate(String formattedCreationDate) {
		this.formattedCreationDate = formattedCreationDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getFormattedUpdateDate() {
		return formattedUpdateDate;
	}

	public void setFormattedUpdateDate(String formattedUpdateDate) {
		this.formattedUpdateDate = formattedUpdateDate;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
