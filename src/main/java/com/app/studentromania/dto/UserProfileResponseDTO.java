package com.app.studentromania.dto;

import java.util.List;

import com.app.studentromania.model.UserProfileFaculty;

public class UserProfileResponseDTO {

	private String userId;

	private String firstName;

	private String lastName;

	private String userName;

	private String email;

	private List<UserProfileFaculty> favoriteFaculties;

	private List<UserProfileFaculty> recentFaculties;

	private List<String> likedReviews;

	private List<String> addedReviews;

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

	public List<String> getAddedReviews() {
		return addedReviews;
	}

	public void setAddedReviews(List<String> addedReviews) {
		this.addedReviews = addedReviews;
	}

}
