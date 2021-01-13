package com.app.studentromania.dto;

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

}
