package com.app.studentromania.dto;

import com.app.studentromania.model.CategoryReview;

public class ReviewDTO {

	private String reviewId;

	private String facultyId;

	private String userId;

	private String userName;

	private String userEmail;

	private Integer generalRating;

	private Integer isUserWorking;

	private String reviewText;

	private CategoryReview jobProspectsReview;

	private CategoryReview coursesAndLecturersReview;

	private CategoryReview studentOrganisationsReview;

	private CategoryReview accomodationReview;

	private CategoryReview facultyFacilitiesReview;

	private CategoryReview studentSupportReview;

	private CategoryReview timeBalanceReview;

	private Integer upvotes;

	/*
	 * true for upvote, false for downvote (reversal of upvote)
	 */
	private Boolean upvote;

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

	public void setFacultyId(String facultyId) {
		this.facultyId = facultyId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public Integer getGeneralRating() {
		return generalRating;
	}

	public void setGeneralRating(Integer generalRating) {
		this.generalRating = generalRating;
	}

	public Integer getIsUserWorking() {
		return isUserWorking;
	}

	public void setIsUserWorking(Integer isUserWorking) {
		this.isUserWorking = isUserWorking;
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

	public Boolean getUpvote() {
		return upvote;
	}

	public void setUpvote(Boolean upvote) {
		this.upvote = upvote;
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

	@Override
	public String toString() {
		return "ReviewDTO [reviewId=" + reviewId + ", facultyId=" + facultyId + ", userId=" + userId + ", userName="
				+ userName + ", userEmail=" + userEmail + ", generalRating=" + generalRating + ", isUserWorking="
				+ isUserWorking + ", reviewText=" + reviewText + ", jobProspectsReview=" + jobProspectsReview
				+ ", coursesAndLecturersReview=" + coursesAndLecturersReview + ", studentOrganisationsReview="
				+ studentOrganisationsReview + ", accomodationReview=" + accomodationReview
				+ ", facultyFacilitiesReview=" + facultyFacilitiesReview + ", studentSupportReview="
				+ studentSupportReview + ", timeBalanceReview=" + timeBalanceReview + ", upvotes=" + upvotes
				+ ", upvote=" + upvote + ", userStatus=" + userStatus + ", wouldRecommend=" + wouldRecommend
				+ ", difficulty=" + difficulty + ", userDomainOfLicenseOrMaster=" + userDomainOfLicenseOrMaster
				+ ", userProgramName=" + userProgramName + ", userYear=" + userYear + "]";
	}

}
