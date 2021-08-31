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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.JWTAuth;
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
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> getUserProfile() {
		return userProfileService.getUserProfile().createRestResponse();
	}

	@GetMapping("/notify")
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> getUsersToNotify() {
		return userProfileService.getUsersToNotify().createRestResponse();
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
	public ResponseEntity<String> resendConfirmation(@RequestParam("token") String existingToken) {
		return userProfileService.resendConfirmation(existingToken).createRestResponse();
	}
	
	@PutMapping("/verifyregister")
	@RestCall
	public ResponseEntity<String> verifyRegistration(@RequestParam("token") String token) {
		return userProfileService.verifyRegistration(token).createRestResponse();
	}

	@PutMapping("/change")
	@RestCall
	public ResponseEntity<String> changePassword(@RequestBody UserProfileDTO userProfileDTO)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		return userProfileService.changePassword(userProfileDTO).createRestResponse();
	}

	@PutMapping("/reset")
	@RestCall
	public ResponseEntity<String> resetPassword(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.resetPassword(userProfileDTO).createRestResponse();
	}
	
	@PutMapping("/verifyreset")
	@RestCall
	public ResponseEntity<String> verifyPasswordReset(@RequestParam("token") String token) {
		return userProfileService.verifyPasswordReset(token).createRestResponse();
	}

	@PutMapping
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.updateUserProfile(userProfileDTO).createRestResponse();
	}

	@PutMapping("/recent/{facultyId}")
	@RestCall
	@JWTAuth
	public ResponseEntity<String> addRecentFaculty(@PathVariable String facultyId) {
		return userProfileService.addRecentFaculty(facultyId).createRestResponse();
	}

	@PutMapping("/favorite/{facultyId}")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> addFavoriteFaculty(@PathVariable String facultyId,
			@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.addFavoriteFaculty(facultyId, userProfileDTO).createRestResponse();
	}

	@PutMapping("/notification/{facultyId}")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> allowNotificationForFaculty(@PathVariable String facultyId,
			@RequestBody UserProfileDTO userProfileDTO) {
		return userProfileService.allowNotificationForFaculty(facultyId, userProfileDTO).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteAllUserProfiles() {
		return userProfileService.deleteAllUserProfiles().createRestResponse();
	}

}
