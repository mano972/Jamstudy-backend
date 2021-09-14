package com.app.studentromania.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.Auth;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.dto.FeedbackEntryDTO;
import com.app.studentromania.service.FeedbackService;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/feedback")
public class FeedbackController {

	@Autowired
	private FeedbackService feedbackService;

	@PostMapping
	@VerifyAdmin
	public ResponseEntity<String> saveFeedback() {
		return feedbackService.saveFeedback().createRestResponse();
	}

	@PutMapping("/reset")
	@VerifyAdmin
	public ResponseEntity<String> resetFeedback() {
		return feedbackService.resetFeedback().createRestResponse();
	}

	@PutMapping("/add")
	@Auth
	public ResponseEntity<String> addFeedbackEntry(@RequestBody FeedbackEntryDTO feedbackEntryDTO) {
		return feedbackService.addFeedbackEntry(feedbackEntryDTO).createRestResponse();
	}

	@GetMapping
	@VerifyAdmin
	public ResponseEntity<String> getFeedback() {
		return feedbackService.getFeedback().createRestResponse();
	}

}
