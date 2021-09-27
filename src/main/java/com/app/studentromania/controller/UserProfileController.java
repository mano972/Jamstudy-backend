package com.app.studentromania.controller;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.Auth;
import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.annotation.VerifyLoggedIn;
import com.app.studentromania.dto.UserProfileDTO;
import com.app.studentromania.service.UserProfileService;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/userprofile")
public class UserProfileController {

	@Autowired
	private UserProfileService userProfileService;

	@GetMapping("/profiles")
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> getAllUserProfiles() {
		return userProfileService.getAllUserProfiles().createRestResponse();
	}

	@GetMapping("/{userId}")
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> getByUserId(@PathVariable String userId) {
		return userProfileService.getByUserId(userId).createRestResponse();
	}

	@GetMapping
	@RestCall
	@Auth
	@VerifyLoggedIn
	public ResponseEntity<String> getUserProfile() {
		return userProfileService.getUserProfile().createRestResponse();
	}

	@GetMapping("/usersnotify")
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> getUsersToNotify() {
		return userProfileService.getUsersToNotify().createRestResponse();
	}

	@GetMapping("/notify")
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> notifyUsers() {
		return userProfileService.notifyUsers().createRestResponse();
	}

	@PostMapping("/register")
	@RestCall
	public ResponseEntity<String> register(@RequestBody UserProfileDTO userProfileDTO)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		return userProfileService.register(userProfileDTO).createRestResponse();
	}

	@PostMapping("/login")
	@RestCall
	public ResponseEntity<String> login(@RequestBody UserProfileDTO userProfileDTO)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		return userProfileService.login(userProfileDTO).createRestResponse();
	}

	@PostMapping("/loginfb")
	@RestCall
	public ResponseEntity<String> loginWithFacebook(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.loginWithFacebook(userProfileDTO).createRestResponse();
	}

	@PutMapping("/resendconfirmation")
	@RestCall
	public ResponseEntity<String> resendConfirmation(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.resendConfirmation(userProfileDTO).createRestResponse();
	}

	@PutMapping("/verifyregister")
	@RestCall
	public ResponseEntity<String> verifyRegistration(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.verifyRegistration(userProfileDTO).createRestResponse();
	}

	@PutMapping("/change")
	@RestCall
	@Auth
	public ResponseEntity<String> changePassword(@RequestBody UserProfileDTO userProfileDTO)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		return userProfileService.changePassword(userProfileDTO).createRestResponse();
	}

	@PutMapping("/reset")
	@RestCall
	@Auth
	public ResponseEntity<String> resetPassword(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.resetPassword(userProfileDTO).createRestResponse();
	}

	@PutMapping("/verifyreset")
	@RestCall
	public ResponseEntity<String> verifyPasswordReset(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.verifyPasswordReset(userProfileDTO).createRestResponse();
	}

	@PutMapping
	@RestCall
	@Auth
	@VerifyLoggedIn
	public ResponseEntity<String> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.updateUserProfile(userProfileDTO).createRestResponse();
	}

	@PutMapping("/recent/{facultyId}")
	@RestCall
	@Auth
	public ResponseEntity<String> addRecentFaculty(@PathVariable String facultyId) {
		return userProfileService.addRecentFaculty(facultyId).createRestResponse();
	}

	@PutMapping("/favorite/{facultyId}")
	@RestCall
	@Auth
	@VerifyLoggedIn
	public ResponseEntity<String> addFavoriteFaculty(@PathVariable String facultyId,
			@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.addFavoriteFaculty(facultyId, userProfileDTO).createRestResponse();
	}

	@PutMapping("/notification/{facultyId}")
	@RestCall
	@Auth
	@VerifyLoggedIn
	public ResponseEntity<String> allowNotificationForFaculty(@PathVariable String facultyId,
			@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.allowNotificationForFaculty(facultyId, userProfileDTO).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteByUserId(@PathVariable String userId) {
		return userProfileService.deleteByUserId(userId).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteAllUserProfiles() {
		return userProfileService.deleteAllUserProfiles().createRestResponse();
	}

}
