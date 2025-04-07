package com.app.studentromania.model;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.Date;

@Document
public class Review extends ParentEntity {

    public Review() {
        super(DocTypeEnum.REVIEW);
    }

    @Field
    private String reviewId;

    @Field
    private String facultyId;

    @Field
    private String facultyName;

    @Field
    private String userId;

    @Field
    private String userName;

    @Field
    private String userEmail;

    @Field
    private Integer generalRating;

    @Field
    private Boolean isUserWorking;

    @Field
    private Date reviewDate;

    @Field
    private String formattedReviewDate;

    @Field
    private String reviewText;

    @Field
    private String reviewAnswerText;

    @Field
    private CategoryReview jobProspectsReview;

    @Field
    private CategoryReview coursesAndLecturersReview;

    @Field
    private CategoryReview studentOrganisationsReview;

    @Field
    private CategoryReview accomodationReview;

    @Field
    private CategoryReview facultyFacilitiesReview;

    @Field
    private CategoryReview studentSupportReview;

    @Field
    private CategoryReview timeBalanceReview;

    @Field
    private int upvotes;

    @Field
    private String userStatus;

    @Field
    private Boolean wouldRecommend;

    @Field
    private Integer difficulty;

    @Field
    private int reports;

    @Field
    private String userDomainOfLicenseOrMaster;

    @Field
    private String userProgramName;

    @Field
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

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
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

    public String getReviewAnswerText() {
        return reviewAnswerText;
    }

    public void setReviewAnswerText(String reviewAnswerText) {
        this.reviewAnswerText = reviewAnswerText;
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

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
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

    public int getReports() {
        return reports;
    }

    public void setReports(int reports) {
        this.reports = reports;
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
