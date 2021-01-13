package com.app.studentromania.model;

import java.util.Date;

public class UserProfileFaculty {

	private String facultyId;

	private String facultyName;

	private String universityId;

	private String universityName;

	private boolean allowNotification;

	private Date addedDate;

	public String getFacultyId() {
		return facultyId;
	}

	public void setFacultyId(String facultyId) {
		this.facultyId = facultyId;
	}

	public String getFacultyName() {
		return facultyName;
	}

	public void setFacultyName(String facultyName) {
		this.facultyName = facultyName;
	}

	public String getUniversityId() {
		return universityId;
	}

	public void setUniversityId(String universityId) {
		this.universityId = universityId;
	}

	public String getUniversityName() {
		return universityName;
	}

	public void setUniversityName(String universityName) {
		this.universityName = universityName;
	}

	public Date getAddedDate() {
		return addedDate;
	}

	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}

	public boolean isAllowNotification() {
		return allowNotification;
	}

	public void setAllowNotification(boolean allowNotification) {
		this.allowNotification = allowNotification;
	}

}
