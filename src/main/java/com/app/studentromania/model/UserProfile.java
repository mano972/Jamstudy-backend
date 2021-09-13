package com.app.studentromania.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class UserProfile extends ParentEntity {

	public UserProfile() {
		super(DocTypeEnum.USER_PROFILE);
	}

	@Field
	private String userId;

	@Field
	private String firstName;

	@Field
	private String lastName;

	@Field
	private String userName;

	@Field
	private String email;

	@Field
	private String password;

	@Field
	private Boolean acceptTermsAndConditions;

	@Field
	private Boolean subscribeToNewsletter;

	@Field
	private Boolean emailConfirmed;

	@Field
	private String emailConfirmationToken;

	@Field
	private Date emailConfirmationTokenExpiration;

	@Field
	private String passwordResetToken;

	@Field
	private Date passwordResetTokenExpiration;

	@Field
	private String location;

	@Field
	private Date birthDate;

	@Field
	private String formattedBirthDate;

	@Field
	private Boolean isUserWorking;

	@Field
	private String userStatus;

	@Field
	private String userYear;

	@Field
	private List<String> userDomainInterest  = new ArrayList<>();

	@Field
	private List<String> userCityInterest  = new ArrayList<>();

	@Field
	private List<UserProfileFaculty> favoriteFaculties = new ArrayList<>();

	@Field
	private List<UserProfileFaculty> recentFaculties = new ArrayList<>();

	@Field
	private List<String> likedReviews = new ArrayList<>();

	@Field
	private List<UserProfileReview> addedReviews = new ArrayList<>();

	@Field
	private String registerType;

	@Field
	private Date lastLogin;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getAcceptTermsAndConditions() {
		return acceptTermsAndConditions;
	}

	public void setAcceptTermsAndConditions(Boolean acceptTermsAndConditions) {
		this.acceptTermsAndConditions = acceptTermsAndConditions;
	}

	public Boolean getSubscribeToNewsletter() {
		return subscribeToNewsletter;
	}

	public void setSubscribeToNewsletter(Boolean subscribeToNewsletter) {
		this.subscribeToNewsletter = subscribeToNewsletter;
	}

	public Boolean getEmailConfirmed() {
		return emailConfirmed;
	}

	public void setEmailConfirmed(Boolean emailConfirmed) {
		this.emailConfirmed = emailConfirmed;
	}

	public String getEmailConfirmationToken() {
		return emailConfirmationToken;
	}

	public void setEmailConfirmationToken(String emailConfirmationToken) {
		this.emailConfirmationToken = emailConfirmationToken;
	}

	public Date getEmailConfirmationTokenExpiration() {
		return emailConfirmationTokenExpiration;
	}

	public void setEmailConfirmationTokenExpiration(Date emailConfirmationTokenExpiration) {
		this.emailConfirmationTokenExpiration = emailConfirmationTokenExpiration;
	}

	public String getPasswordResetToken() {
		return passwordResetToken;
	}

	public void setPasswordResetToken(String passwordResetToken) {
		this.passwordResetToken = passwordResetToken;
	}

	public Date getPasswordResetTokenExpiration() {
		return passwordResetTokenExpiration;
	}

	public void setPasswordResetTokenExpiration(Date passwordResetTokenExpiration) {
		this.passwordResetTokenExpiration = passwordResetTokenExpiration;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getFormattedBirthDate() {
		return formattedBirthDate;
	}

	public void setFormattedBirthDate(String formattedBirthDate) {
		this.formattedBirthDate = formattedBirthDate;
	}

	public Boolean getIsUserWorking() {
		return isUserWorking;
	}

	public void setIsUserWorking(Boolean isUserWorking) {
		this.isUserWorking = isUserWorking;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getUserYear() {
		return userYear;
	}

	public void setUserYear(String userYear) {
		this.userYear = userYear;
	}

	public List<String> getUserDomainInterest() {
		return userDomainInterest;
	}

	public void setUserDomainInterest(List<String> userDomainInterest) {
		this.userDomainInterest = userDomainInterest;
	}

	public List<String> getUserCityInterest() {
		return userCityInterest;
	}

	public void setUserCityInterest(List<String> userCityInterest) {
		this.userCityInterest = userCityInterest;
	}

	public List<UserProfileFaculty> getFavoriteFaculties() {
		return favoriteFaculties;
	}

	public void setFavoriteFaculties(List<UserProfileFaculty> favoriteFaculties) {
		this.favoriteFaculties = favoriteFaculties;
	}

	public List<UserProfileFaculty> getRecentFaculties() {
		return recentFaculties;
	}

	public void setRecentFaculties(List<UserProfileFaculty> recentFaculties) {
		this.recentFaculties = recentFaculties;
	}

	public List<String> getLikedReviews() {
		return likedReviews;
	}

	public void setLikedReviews(List<String> likedReviews) {
		this.likedReviews = likedReviews;
	}

	public List<UserProfileReview> getAddedReviews() {
		return addedReviews;
	}

	public void setAddedReviews(List<UserProfileReview> addedReviews) {
		this.addedReviews = addedReviews;
	}

	public String getRegisterType() {
		return registerType;
	}

	public void setRegisterType(String registerType) {
		this.registerType = registerType;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

}
