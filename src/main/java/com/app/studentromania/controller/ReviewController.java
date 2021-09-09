package com.app.studentromania.controller;

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
import com.app.studentromania.dto.ReviewDTO;
import com.app.studentromania.service.ReviewService;
import com.app.studentromania.util.ReviewFilter;

@CrossOrigin(origins = "https://unistart.ro")
@RestController
@RequestMapping("${base.path}/review")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	@GetMapping
	@RestCall
	public ResponseEntity<String> getAllReviews() {
		return reviewService.getAllReviews().createRestResponse();
	}

	@GetMapping("/{reviewId}")
	@RestCall
	public ResponseEntity<String> getByReviewId(@PathVariable String reviewId) {
		return reviewService.getByReviewId(reviewId).createRestResponse();
	}

	@GetMapping("/faculty/{facultyId}")
	@RestCall
	public ResponseEntity<String> getByFacultyId(@PathVariable String facultyId, ReviewFilter reviewFilter) {
		return reviewService.getFilteredReviewsByFacultyId(facultyId, reviewFilter).createRestResponse();
	}

	@GetMapping("/user")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> getUserReviews() {
		return reviewService.getUserReviews().createRestResponse();
	}

	@PostMapping("/{facultyId}")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> createReview(@PathVariable String facultyId, @RequestBody ReviewDTO reviewDTO) {
		reviewDTO.setFacultyId(facultyId);
		return reviewService.createReview(reviewDTO).createRestResponse();
	}

	@PutMapping("/{reviewId}")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> updateReview(@PathVariable String reviewId, @RequestBody ReviewDTO reviewDTO) {
		reviewDTO.setReviewId(reviewId);
		return reviewService.updateReview(reviewDTO).createRestResponse();
	}

	@PutMapping("/{reviewId}/report")
	@RestCall
	public ResponseEntity<String> reportReview(@PathVariable String reviewId) {
		return reviewService.reportReview(reviewId).createRestResponse();
	}

	@PutMapping("/{reviewId}/upvote")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> upvoteReview(@PathVariable String reviewId, @RequestBody ReviewDTO reviewDTO) {
		reviewDTO.setReviewId(reviewId);
		return reviewService.upvoteReview(reviewDTO).createRestResponse();
	}

	@DeleteMapping("/{reviewId}")
	@VerifyAdmin
	public ResponseEntity<String> deleteByReviewId(@PathVariable String reviewId) {
		return reviewService.deleteByReviewId(reviewId).createRestResponse();
	}

	@DeleteMapping("/faculty/{facultyId}")
	@VerifyAdmin
	public ResponseEntity<String> deleteByFacultyId(@PathVariable String facultyId) {
		return reviewService.deleteByFacultyId(facultyId).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteAllReviews() {
		return reviewService.deleteAllReviews().createRestResponse();
	}

}
