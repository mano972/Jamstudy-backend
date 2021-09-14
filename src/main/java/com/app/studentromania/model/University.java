package com.app.studentromania.model;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class University extends ParentEntity {

	public University() {
		super(DocTypeEnum.UNIVERSITY);
	}

	@Field
	private String universityId;

	@Field
	private String accreditation;

	/*
	 * PUBLIC sau PRIVAT. S-ar putea sa nu fie date bune.
	 */
	@Field
	private String type;

	@Field
	private String universityAddress;

	@Field
	private String universityCity;

	@Field
	private String universityCover;

	@Field
	private String universityEmail;

	@Field
	private String universityLogo;

	@Field
	private String universityName;

	@Field
	private String universityPhone;

	@Field
	private String universityShortname;

	@Field
	private String universityWebsite;

	@Field
	private String universityDescription;

	@Field
	private Integer studentDormPlaces;

	public String getUniversityId() {
		return universityId;
	}

	public void setUniversityId(String universityId) {
		this.universityId = universityId;
	}

	public String getAccreditation() {
		return accreditation;
	}

	public void setAccreditation(String accreditation) {
		this.accreditation = accreditation;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUniversityAddress() {
		return universityAddress;
	}

	public void setUniversityAddress(String universityAddress) {
		this.universityAddress = universityAddress;
	}

	public String getUniversityCity() {
		return universityCity;
	}

	public void setUniversityCity(String universityCity) {
		this.universityCity = universityCity;
	}

	public String getUniversityCover() {
		return universityCover;
	}

	public void setUniversityCover(String universityCover) {
		this.universityCover = universityCover;
	}

	public String getUniversityEmail() {
		return universityEmail;
	}

	public void setUniversityEmail(String universityEmail) {
		this.universityEmail = universityEmail;
	}

	public String getUniversityLogo() {
		return universityLogo;
	}

	public void setUniversityLogo(String universityLogo) {
		this.universityLogo = universityLogo;
	}

	public String getUniversityName() {
		return universityName;
	}

	public void setUniversityName(String universityName) {
		this.universityName = universityName;
	}

	public String getUniversityPhone() {
		return universityPhone;
	}

	public void setUniversityPhone(String universityPhone) {
		this.universityPhone = universityPhone;
	}

	public String getUniversityShortname() {
		return universityShortname;
	}

	public void setUniversityShortname(String universityShortname) {
		this.universityShortname = universityShortname;
	}

	public String getUniversityWebsite() {
		return universityWebsite;
	}

	public void setUniversityWebsite(String universityWebsite) {
		this.universityWebsite = universityWebsite;
	}

	public String getUniversityDescription() {
		return universityDescription;
	}

	public void setUniversityDescription(String universityDescription) {
		this.universityDescription = universityDescription;
	}

	public Integer getStudentDormPlaces() {
		return studentDormPlaces;
	}

	public void setStudentDormPlaces(Integer studentDormPlaces) {
		this.studentDormPlaces = studentDormPlaces;
	}

}
