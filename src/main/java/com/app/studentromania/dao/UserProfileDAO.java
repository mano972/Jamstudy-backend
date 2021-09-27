package com.app.studentromania.dao;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.model.UserProfile;
import com.app.studentromania.repo.UserProfileRepo;
import com.app.studentromania.util.LogUtils;

@Component
public class UserProfileDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileDAO.class);

	@Autowired
	private UserProfileRepo userProfileRepo;

	@Autowired
	private LogUtils logUtils;

	public List<UserProfile> getAllUserProfiles() {
		return userProfileRepo.findAll();
	}

	public Optional<UserProfile> getByUserId(String userId) {
		return userProfileRepo.findByUserId(userId).stream().findFirst();
	}

	public Optional<UserProfile> getByUserName(String userName) {
		return userProfileRepo.findByUserName(userName).stream().findFirst();
	}

	public Optional<UserProfile> getByEmail(String email) {
		return userProfileRepo.findByEmail(email.toLowerCase()).stream().findFirst();
	}

	public Optional<UserProfile> getByEmailConfirmationToken(String token) {
		return userProfileRepo.findByEmailConfirmationToken(token).stream().findFirst();
	}

	public Optional<UserProfile> getByPasswordResetToken(String token) {
		return userProfileRepo.findByPasswordResetToken(token).stream().findFirst();
	}

	public List<UserProfile> getUsersToNotify() {
		return userProfileRepo.findUsersToNotify();
	}

	public void createUserProfile(UserProfile userProfile) {
		userProfileRepo.save(userProfile);
		logUtils.logMessage(LOGGER, "User Profile " + userProfile.getUserId() + " was created");
	}

	public void updateUserProfile(UserProfile userProfile) {
		userProfile.setUpdateDateWithCurrentDate();
		userProfileRepo.save(userProfile);
		logUtils.logMessage(LOGGER, "User Profile " + userProfile.getUserId() + " was updated");

	}

	public void saveUserProfile(UserProfile userProfile) {
		userProfileRepo.save(userProfile);
	}

	public void deleteUserProfile(UserProfile userProfile) {
		userProfileRepo.delete(userProfile);
		logUtils.logMessage(LOGGER, "User Profile " + userProfile.getUserId() + " was deleted!");
	}

	public void deleteAllUserProfiles() {
		userProfileRepo.deleteAll();
	}

}
