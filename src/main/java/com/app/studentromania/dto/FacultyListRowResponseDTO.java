package com.app.studentromania.dto;

public class FacultyListRowResponseDTO {

	private String facultyId;

	private String universityId;

	private String universityName;

	private String facultyCity;

	private String facultyName;

	private String facultyShortname;

	private Double avgRating;

	private Integer countRev;

	public String getFacultyId() {
		return facultyId;
	}

	public void setFacultyId(String facultyId) {
		this.facultyId = facultyId;
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

	public String getFacultyCity() {
		return facultyCity;
	}

	public void setFacultyCity(String facultyCity) {
		this.facultyCity = facultyCity;
	}

	public String getFacultyName() {
		return facultyName;
	}

	public void setFacultyName(String facultyName) {
		this.facultyName = facultyName;
	}

	public String getFacultyShortname() {
		return facultyShortname;
	}

	public void setFacultyShortname(String facultyShortname) {
		this.facultyShortname = facultyShortname;
	}

	public Double getAvgRating() {
		return avgRating;
	}

	public void setAvgRating(Double avgRating) {
		this.avgRating = avgRating;
	}

	public Integer getCountRev() {
		return countRev;
	}

	public void setCountRev(Integer countRev) {
		this.countRev = countRev;
	}

}
