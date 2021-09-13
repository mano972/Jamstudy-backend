package com.app.studentromania.dto;

import java.util.Date;
import java.util.List;

import com.app.studentromania.model.UserProfileFaculty;
import com.app.studentromania.model.UserProfileReview;

public class UserProfileResponseDTO {

	private String userId;

	private String firstName;

	private String lastName;

	private String userName;

	private String email;

	private String location;

	private Date birthDate;

	private String formattedBirthDate;

	private Boolean isUserWorking;

	private String userStatus;

	private String userYear;

	private List<String> userDomainInterest;

	private List<String> userCityInterest;

	private Boolean subscribeToNewsletter;

	private List<UserProfileFaculty> favoriteFaculties;

	private List<UserProfileFaculty> recentFaculties;

	private List<String> likedReviews;

	private List<UserProfileReview> addedReviews;

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

	public Boolean getSubscribeToNewsletter() {
		return subscribeToNewsletter;
	}

	public void setSubscribeToNewsletter(Boolean subscribeToNewsletter) {
		this.subscribeToNewsletter = subscribeToNewsletter;
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

}
