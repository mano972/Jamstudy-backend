package com.app.studentromania.dto;

import java.util.List;

import com.app.studentromania.model.UserProfileFaculty;
import com.app.studentromania.model.UserProfileReview;

public class AuthResponseDTO {

	private String firstName;

	private String lastName;

	private List<UserProfileFaculty> favoriteFaculties;

	private List<UserProfileFaculty> recentFaculties;

	private List<String> likedReviews;

	private List<UserProfileReview> addedReviews;

	private String jwtToken;

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

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

}
