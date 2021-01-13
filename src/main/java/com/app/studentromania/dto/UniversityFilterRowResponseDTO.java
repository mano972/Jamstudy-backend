package com.app.studentromania.dto;

public class UniversityFilterRowResponseDTO {

	private String universityId;

	private String universityName;

	private Integer countFac;

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

	public Integer getCountFac() {
		return countFac;
	}

	public void setCountFac(Integer countFac) {
		this.countFac = countFac;
	}

}
