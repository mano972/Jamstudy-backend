package com.app.studentromania.model;

import java.util.Date;
import java.util.UUID;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;

public abstract class ParentEntity {

	@Id
	private String id;

	@Field
	private Date creationDate;

	@Field
	private Date updateDate;

	@Field
	private String docType;

	public ParentEntity(DocTypeEnum docType) {
		initParentEntity(docType);
		setId(UUID.randomUUID().toString());
	}

	public void initParentEntity(DocTypeEnum docType) {
		setCreationDate(new Date());
		setUpdateDate(new Date());
		setDocType(docType.toString());
	}

	public void setUpdateDateWithCurrentDate() {
		setUpdateDate(new Date());
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

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

}
