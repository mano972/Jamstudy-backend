package com.app.studentromania.dto;

import java.util.Date;

import com.app.studentromania.model.CategoryReview;

public class ReviewResponseDTO {

	private String reviewId;

	private String facultyId;

	private String userName;

	private Integer generalRating;

	private Boolean isUserWorking;

	private Date reviewDate;

	private String formattedReviewDate;

	private String reviewText;

	private CategoryReview jobProspectsReview;

	private CategoryReview coursesAndLecturersReview;

	private CategoryReview studentOrganisationsReview;

	private CategoryReview accomodationReview;

	private CategoryReview facultyFacilitiesReview;

	private CategoryReview studentSupportReview;

	private CategoryReview timeBalanceReview;

	private Integer upvotes;

	private String userStatus;

	private Boolean wouldRecommend;

	private Integer difficulty;

	private String userDomainOfLicenseOrMaster;

	private String userProgramName;

	private String userYear;

	public String getReviewId() {
		return reviewId;
	}

	public void setReviewId(String reviewId) {
		this.reviewId = reviewId;
	}

	public String getFacultyId() {
		return facultyId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setFacultyId(String facultyId) {
		this.facultyId = facultyId;
	}

	public Integer getGeneralRating() {
		return generalRating;
	}

	public void setGeneralRating(Integer generalRating) {
		this.generalRating = generalRating;
	}

	public Boolean getIsUserWorking() {
		return isUserWorking;
	}

	public void setIsUserWorking(Boolean isUserWorking) {
		this.isUserWorking = isUserWorking;
	}

	public Date getReviewDate() {
		return reviewDate;
	}

	public void setReviewDate(Date reviewDate) {
		this.reviewDate = reviewDate;
	}

	public String getFormattedReviewDate() {
		return formattedReviewDate;
	}

	public void setFormattedReviewDate(String formattedReviewDate) {
		this.formattedReviewDate = formattedReviewDate;
	}

	public String getReviewText() {
		return reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}

	public CategoryReview getJobProspectsReview() {
		return jobProspectsReview;
	}

	public void setJobProspectsReview(CategoryReview jobProspectsReview) {
		this.jobProspectsReview = jobProspectsReview;
	}

	public CategoryReview getCoursesAndLecturersReview() {
		return coursesAndLecturersReview;
	}

	public void setCoursesAndLecturersReview(CategoryReview coursesAndLecturersReview) {
		this.coursesAndLecturersReview = coursesAndLecturersReview;
	}

	public CategoryReview getStudentOrganisationsReview() {
		return studentOrganisationsReview;
	}

	public void setStudentOrganisationsReview(CategoryReview studentOrganisationsReview) {
		this.studentOrganisationsReview = studentOrganisationsReview;
	}

	public CategoryReview getAccomodationReview() {
		return accomodationReview;
	}

	public void setAccomodationReview(CategoryReview accomodationReview) {
		this.accomodationReview = accomodationReview;
	}

	public CategoryReview getFacultyFacilitiesReview() {
		return facultyFacilitiesReview;
	}

	public void setFacultyFacilitiesReview(CategoryReview facultyFacilitiesReview) {
		this.facultyFacilitiesReview = facultyFacilitiesReview;
	}

	public CategoryReview getStudentSupportReview() {
		return studentSupportReview;
	}

	public void setStudentSupportReview(CategoryReview studentSupportReview) {
		this.studentSupportReview = studentSupportReview;
	}

	public CategoryReview getTimeBalanceReview() {
		return timeBalanceReview;
	}

	public void setTimeBalanceReview(CategoryReview timeBalanceReview) {
		this.timeBalanceReview = timeBalanceReview;
	}

	public Integer getUpvotes() {
		return upvotes;
	}

	public void setUpvotes(Integer upvotes) {
		this.upvotes = upvotes;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public Boolean getWouldRecommend() {
		return wouldRecommend;
	}

	public void setWouldRecommend(Boolean wouldRecommend) {
		this.wouldRecommend = wouldRecommend;
	}

	public Integer getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Integer difficulty) {
		this.difficulty = difficulty;
	}

	public String getUserDomainOfLicenseOrMaster() {
		return userDomainOfLicenseOrMaster;
	}

	public void setUserDomainOfLicenseOrMaster(String userDomainOfLicenseOrMaster) {
		this.userDomainOfLicenseOrMaster = userDomainOfLicenseOrMaster;
	}

	public String getUserProgramName() {
		return userProgramName;
	}

	public void setUserProgramName(String userProgramName) {
		this.userProgramName = userProgramName;
	}

	public String getUserYear() {
		return userYear;
	}

	public void setUserYear(String userYear) {
		this.userYear = userYear;
	}

}
