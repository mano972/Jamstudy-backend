package com.app.studentromania.dto;

import java.util.List;

import com.app.studentromania.model.FacultyProgram;

public class FacultyResponseDTO {

	private String facultyId;

	private String universityId;

	private String universityName;

	private Integer availablePlacesLicense;

	private Integer availablePlacesMaster;

	private String facultyAddress;

	private String facultyCity;

	private String facultyCover;

	private String facultyEmail;

	private String facultyLogo;

	private String facultyName;

	private String facultyPhone;

	private String facultyShortname;

	private String facultyWebsite;

	private String facultyDescription;

	private List<FacultyProgram> licensePrograms;

	private List<String> facultyDomainsLicense;

	private List<FacultyProgram> masterPrograms;

	private List<String> facultyDomainsMaster;

	private String facultyType;

	private Integer noOfYears;

	private Integer budgetPlacesLicense;

	private Integer taxPlacesLicense;

	private Integer budgetPlacesMaster;

	private Integer taxPlacesMaster;

	private Integer enrolledStudentsLicence;

	private Integer enrolledStudentsMaster;

	private Integer noOfProfessors;

	private Integer noOfStudentsPerProfessor;

	private String accreditation;

	private Integer annualTax;

	private Double avgRating;

	private Integer countRev;

	private Double avgDifficulty;

	private Integer percentageWouldRecommend;

	private Integer countRev5Stars;

	private Integer countRev4Stars;

	private Integer countRev3Stars;

	private Integer countRev2Stars;

	private Integer countRev1Star;

	private Double avgRatingJobProspects;

	private Double avgRatingCoursesAndLecturers;

	private Double avgRatingStudentOrganisations;

	private Double avgRatingAccomodation;

	private Double avgRatingFacultyFacilities;

	private Double avgRatingStudentSupport;
	
	private Double avgRatingTimeBalance;

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

	public Integer getAvailablePlacesLicense() {
		return availablePlacesLicense;
	}

	public void setAvailablePlacesLicense(Integer availablePlacesLicense) {
		this.availablePlacesLicense = availablePlacesLicense;
	}

	public Integer getAvailablePlacesMaster() {
		return availablePlacesMaster;
	}

	public void setAvailablePlacesMaster(Integer availablePlacesMaster) {
		this.availablePlacesMaster = availablePlacesMaster;
	}

	public String getFacultyAddress() {
		return facultyAddress;
	}

	public void setFacultyAddress(String facultyAddress) {
		this.facultyAddress = facultyAddress;
	}

	public String getFacultyCity() {
		return facultyCity;
	}

	public void setFacultyCity(String facultyCity) {
		this.facultyCity = facultyCity;
	}

	public String getFacultyCover() {
		return facultyCover;
	}

	public void setFacultyCover(String facultyCover) {
		this.facultyCover = facultyCover;
	}

	public String getFacultyEmail() {
		return facultyEmail;
	}

	public void setFacultyEmail(String facultyEmail) {
		this.facultyEmail = facultyEmail;
	}

	public String getFacultyLogo() {
		return facultyLogo;
	}

	public void setFacultyLogo(String facultyLogo) {
		this.facultyLogo = facultyLogo;
	}

	public String getFacultyName() {
		return facultyName;
	}

	public void setFacultyName(String facultyName) {
		this.facultyName = facultyName;
	}

	public String getFacultyPhone() {
		return facultyPhone;
	}

	public void setFacultyPhone(String facultyPhone) {
		this.facultyPhone = facultyPhone;
	}

	public String getFacultyShortname() {
		return facultyShortname;
	}

	public void setFacultyShortname(String facultyShortname) {
		this.facultyShortname = facultyShortname;
	}

	public String getFacultyWebsite() {
		return facultyWebsite;
	}

	public void setFacultyWebsite(String facultyWebsite) {
		this.facultyWebsite = facultyWebsite;
	}

	public String getFacultyDescription() {
		return facultyDescription;
	}

	public void setFacultyDescription(String facultyDescription) {
		this.facultyDescription = facultyDescription;
	}

	public List<FacultyProgram> getLicensePrograms() {
		return licensePrograms;
	}

	public void setLicensePrograms(List<FacultyProgram> licensePrograms) {
		this.licensePrograms = licensePrograms;
	}

	public List<String> getFacultyDomainsLicense() {
		return facultyDomainsLicense;
	}

	public void setFacultyDomainsLicense(List<String> facultyDomainsLicense) {
		this.facultyDomainsLicense = facultyDomainsLicense;
	}

	public List<FacultyProgram> getMasterPrograms() {
		return masterPrograms;
	}

	public void setMasterPrograms(List<FacultyProgram> masterPrograms) {
		this.masterPrograms = masterPrograms;
	}

	public List<String> getFacultyDomainsMaster() {
		return facultyDomainsMaster;
	}

	public void setFacultyDomainsMaster(List<String> facultyDomainsMaster) {
		this.facultyDomainsMaster = facultyDomainsMaster;
	}

	public String getFacultyType() {
		return facultyType;
	}

	public void setFacultyType(String facultyType) {
		this.facultyType = facultyType;
	}

	public Integer getNoOfYears() {
		return noOfYears;
	}

	public void setNoOfYears(Integer noOfYears) {
		this.noOfYears = noOfYears;
	}

	public Integer getBudgetPlacesLicense() {
		return budgetPlacesLicense;
	}

	public void setBudgetPlacesLicense(Integer budgetPlacesLicense) {
		this.budgetPlacesLicense = budgetPlacesLicense;
	}

	public Integer getTaxPlacesLicense() {
		return taxPlacesLicense;
	}

	public void setTaxPlacesLicense(Integer taxPlacesLicense) {
		this.taxPlacesLicense = taxPlacesLicense;
	}

	public Integer getBudgetPlacesMaster() {
		return budgetPlacesMaster;
	}

	public void setBudgetPlacesMaster(Integer budgetPlacesMaster) {
		this.budgetPlacesMaster = budgetPlacesMaster;
	}

	public Integer getTaxPlacesMaster() {
		return taxPlacesMaster;
	}

	public void setTaxPlacesMaster(Integer taxPlacesMaster) {
		this.taxPlacesMaster = taxPlacesMaster;
	}

	public Integer getEnrolledStudentsLicence() {
		return enrolledStudentsLicence;
	}

	public void setEnrolledStudentsLicence(Integer enrolledStudentsLicence) {
		this.enrolledStudentsLicence = enrolledStudentsLicence;
	}

	public Integer getEnrolledStudentsMaster() {
		return enrolledStudentsMaster;
	}

	public void setEnrolledStudentsMaster(Integer enrolledStudentsMaster) {
		this.enrolledStudentsMaster = enrolledStudentsMaster;
	}

	public Integer getNoOfProfessors() {
		return noOfProfessors;
	}

	public void setNoOfProfessors(Integer noOfProfessors) {
		this.noOfProfessors = noOfProfessors;
	}

	public Integer getNoOfStudentsPerProfessor() {
		return noOfStudentsPerProfessor;
	}

	public void setNoOfStudentsPerProfessor(Integer noOfStudentsPerProfessor) {
		this.noOfStudentsPerProfessor = noOfStudentsPerProfessor;
	}

	public String getAccreditation() {
		return accreditation;
	}

	public void setAccreditation(String accreditation) {
		this.accreditation = accreditation;
	}

	public Integer getAnnualTax() {
		return annualTax;
	}

	public void setAnnualTax(Integer annualTax) {
		this.annualTax = annualTax;
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

	public Double getAvgDifficulty() {
		return avgDifficulty;
	}

	public void setAvgDifficulty(Double avgDifficulty) {
		this.avgDifficulty = avgDifficulty;
	}

	public Integer getPercentageWouldRecommend() {
		return percentageWouldRecommend;
	}

	public void setPercentageWouldRecommend(Integer percentageWouldRecommend) {
		this.percentageWouldRecommend = percentageWouldRecommend;
	}

	public Integer getCountRev5Stars() {
		return countRev5Stars;
	}

	public void setCountRev5Stars(Integer countRev5Stars) {
		this.countRev5Stars = countRev5Stars;
	}

	public Integer getCountRev4Stars() {
		return countRev4Stars;
	}

	public void setCountRev4Stars(Integer countRev4Stars) {
		this.countRev4Stars = countRev4Stars;
	}

	public Integer getCountRev3Stars() {
		return countRev3Stars;
	}

	public void setCountRev3Stars(Integer countRev3Stars) {
		this.countRev3Stars = countRev3Stars;
	}

	public Integer getCountRev2Stars() {
		return countRev2Stars;
	}

	public void setCountRev2Stars(Integer countRev2Stars) {
		this.countRev2Stars = countRev2Stars;
	}

	public Integer getCountRev1Star() {
		return countRev1Star;
	}

	public void setCountRev1Star(Integer countRev1Star) {
		this.countRev1Star = countRev1Star;
	}

	public Double getAvgRatingJobProspects() {
		return avgRatingJobProspects;
	}

	public void setAvgRatingJobProspects(Double avgRatingJobProspects) {
		this.avgRatingJobProspects = avgRatingJobProspects;
	}

	public Double getAvgRatingCoursesAndLecturers() {
		return avgRatingCoursesAndLecturers;
	}

	public void setAvgRatingCoursesAndLecturers(Double avgRatingCoursesAndLecturers) {
		this.avgRatingCoursesAndLecturers = avgRatingCoursesAndLecturers;
	}

	public Double getAvgRatingStudentOrganisations() {
		return avgRatingStudentOrganisations;
	}

	public void setAvgRatingStudentOrganisations(Double avgRatingStudentOrganisations) {
		this.avgRatingStudentOrganisations = avgRatingStudentOrganisations;
	}

	public Double getAvgRatingAccomodation() {
		return avgRatingAccomodation;
	}

	public void setAvgRatingAccomodation(Double avgRatingAccomodation) {
		this.avgRatingAccomodation = avgRatingAccomodation;
	}

	public Double getAvgRatingFacultyFacilities() {
		return avgRatingFacultyFacilities;
	}

	public void setAvgRatingFacultyFacilities(Double avgRatingFacultyFacilities) {
		this.avgRatingFacultyFacilities = avgRatingFacultyFacilities;
	}

	public Double getAvgRatingStudentSupport() {
		return avgRatingStudentSupport;
	}

	public void setAvgRatingStudentSupport(Double avgRatingStudentSupport) {
		this.avgRatingStudentSupport = avgRatingStudentSupport;
	}

	public Double getAvgRatingTimeBalance() {
		return avgRatingTimeBalance;
	}

	public void setAvgRatingTimeBalance(Double avgRatingTimeBalance) {
		this.avgRatingTimeBalance = avgRatingTimeBalance;
	}

}
