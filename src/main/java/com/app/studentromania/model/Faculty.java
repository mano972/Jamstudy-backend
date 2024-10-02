package com.app.studentromania.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class Faculty extends ParentEntity {

	public Faculty() {
		super(DocTypeEnum.FACULTY);
	}

	@Field
	private String facultyId;

	@Field
	private String universityId;

	@Field
	private String universityName;

	@Field
	private Integer availablePlacesLicense;

	@Field
	private Integer availablePlacesMaster;

	@Field
	private String facultyAddress;

	@Field
	private String facultyCity;

	@Field
	private String facultyCover;

	@Field
	private String facultyEmail;

	@Field
	private String facultyLogo;

	@Field
	private String facultyName;

	@Field
	private String facultyPhone;

	@Field
	private String facultyShortname;

	@Field
	private String facultyWebsite;

	@Field
	private String facultyDescription;

	@Field
	private List<FacultyProgram> licensePrograms = new ArrayList<>();

	/** List of all license facultyProgram domains */
	@Field
	private List<String> facultyDomainsLicense = new ArrayList<>();

	@Field
	private List<FacultyProgram> masterPrograms = new ArrayList<>();

	/** List of all master facultyProgram domains */
	@Field
	private List<String> facultyDomainsMaster = new ArrayList<>();

	/*
	 * PUBLIC sau PRIVAT. S-ar putea sa nu fie date bune.
	 */
	@Field
	private String facultyType;

	@Field
	private Integer noOfYears;

	/** List of all license facultyProgram budget places */
	@Field
	private Integer budgetPlacesLicense;

	/** List of all license facultyProgram tax places */
	@Field
	private Integer taxPlacesLicense;

	/** List of all master facultyProgram budget places */
	@Field
	private Integer budgetPlacesMaster;

	/** List of all master facultyProgram tax places */
	@Field
	private Integer taxPlacesMaster;

	/** Number of license students */
	@Field
	private Integer enrolledStudentsLicence;

	/** Number of master students */
	@Field
	private Integer enrolledStudentsMaster;

	@Field
	private Integer noOfProfessors;

	@Field
	private Integer noOfStudentsPerProfessor;

	@Field
	private String accreditation;

	/** Average rating */
//	@Formula("(select ifnull(avg(r.general_rating), 0) from Review r where r.faculty_id = faculty_id)")
//	@Transient
	@Field
	private Double avgRating;

	/** Number of reviews */
//	@Formula("(select count(r.review_id) from Review r where r.faculty_id = faculty_id)")
//	@Transient
	@Field
	private Integer countRev;

	/** Average difficulty */
//	@Formula("(select ifnull(avg(r.difficulty), 0) from Review r where r.faculty_id = faculty_id)")
//	@Transient
	@Field
	private Double avgDifficulty;

	/** Percentage of users who would recommend the faculty */
//	@Formula("(select (count(CASE WHEN r.would_recommend = 1 THEN 1 END) / count(*)) * 100 from Review r where r.faculty_id = faculty_id)")
//	@Transient
	@Field
	private Integer percentageWouldRecommend;

	/** Number of 5 star ratings */
	@Field
	private Integer countRev5Stars;

	/** Number of 4 star ratings */
	@Field
	private Integer countRev4Stars;

	/** Number of 3 star ratings */
	@Field
	private Integer countRev3Stars;

	/** Number of 2 star ratings */
	@Field
	private Integer countRev2Stars;

	/** Number of 1 star ratings */
	@Field
	private Integer countRev1Star;

	/** Average rating for job prospects */
	@Field
	private Double avgRatingJobProspects;

	/** Average rating for courses and lectures */
	@Field
	private Double avgRatingCoursesAndLecturers;

	/** Average rating for student organisations */
	@Field
	private Double avgRatingStudentOrganisations;

	/** Average rating for accommodation (student dorms or other rent places) */
	@Field
	private Double avgRatingAccomodation;

	/** Average rating for faculty facilities */
	@Field
	private Double avgRatingFacultyFacilities;

	/** Average rating for student support (students or professors) */
	@Field
	private Double avgRatingStudentSupport;

	/** Average rating for study/free time balance */
	@Field
	private Double avgRatingTimeBalance;

	@Field
	private List<String> companyIds;

	@Field
	private int viewsCount;

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

	public List<String> getCompanyIds() {
		return companyIds;
	}

	public void setCompanyIds(List<String> companyIds) {
		this.companyIds = companyIds;
	}
	public int getViewsCount() {
		return viewsCount;
	}

	public void setViewsCount(int viewsCount) {
		this.viewsCount = viewsCount;
	}

}