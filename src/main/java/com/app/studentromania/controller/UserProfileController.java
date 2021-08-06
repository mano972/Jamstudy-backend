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

	@PutMapping("/{userId}")
	@RestCall
	public ResponseEntity<String> updateUserProfile(@PathVariable String userId,
			@RequestBody UserProfileDTO userProfileDTO) {
		userProfileDTO.setUserId(userId);
		return userProfileService.updateUserProfile(userProfileDTO).createRestResponse();
	}

	@PutMapping("/{userId}/recentfaculty/{facultyId}")
	@RestCall
	public ResponseEntity<String> addRecentFaculty(@PathVariable String userId, @PathVariable String facultyId) {
		return userProfileService.addRecentFaculty(userId, facultyId).createRestResponse();
	}

	@PutMapping("/{userId}/favoritefaculty/{facultyId}")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> addFavoriteFaculty(@PathVariable String userId, @PathVariable String facultyId,
			@RequestBody UserProfileDTO userProfileDTO) {
		userProfileDTO.setUserId(userId);
		return userProfileService.addFavoriteFaculty(facultyId, userProfileDTO).createRestResponse();
	}

	@PutMapping("/{userId}/notification/{facultyId}")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> allowNotificationForFaculty(@PathVariable String userId,
			@PathVariable String facultyId, @RequestBody UserProfileDTO userProfileDTO) {
		userProfileDTO.setUserId(userId);
		return userProfileService.allowNotificationForFaculty(facultyId, userProfileDTO).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteAllUserProfiles() {
		return userProfileService.deleteAllUserProfiles().createRestResponse();
	}

}
