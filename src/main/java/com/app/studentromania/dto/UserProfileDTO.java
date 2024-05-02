package com.app.studentromania.dto;

import java.util.Date;
import java.util.List;

public class UserProfileDTO {

	private String userId;

	private String firstName;

	private String lastName;

	private String userName;

	private String email;

	private String password;

	/*
	 * true for add, false for remove
	 */
	private Boolean addFavoriteFaculty;

	private Boolean allowNotification;

	private Boolean acceptTermsAndConditions;

	private Boolean subscribeToNewsletter;

	private String emailConfirmationToken;

	private String passwordResetToken;

	private String location;

	private Date birthDate;

	private Integer isUserWorking;

	private String userStatus;

	private String userYear;

	private List<String> userDomainInterest;

	private List<String> userCityInterest;

	private String registerType;

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

	public String getEmailConfirmationToken() {
		return emailConfirmationToken;
	}

	public void setEmailConfirmationToken(String emailConfirmationToken) {
		this.emailConfirmationToken = emailConfirmationToken;
	}

	public String getPasswordResetToken() {
		return passwordResetToken;
	}

	public void setPasswordResetToken(String passwordResetToken) {
		this.passwordResetToken = passwordResetToken;
	}

	public Boolean getAddFavoriteFaculty() {
		return addFavoriteFaculty;
	}

	public void setAddFavoriteFaculty(Boolean addFavoriteFaculty) {
		this.addFavoriteFaculty = addFavoriteFaculty;
	}

	public Boolean getAllowNotification() {
		return allowNotification;
	}

	public void setAllowNotification(Boolean allowNotification) {
		this.allowNotification = allowNotification;
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

	public Integer getIsUserWorking() {
		return isUserWorking;
	}

	public void setIsUserWorking(Integer isUserWorking) {
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

	public String getRegisterType() {
		return registerType;
	}
	public void setRegisterType(String registerType) {
		this.registerType = registerType;
	}
}
