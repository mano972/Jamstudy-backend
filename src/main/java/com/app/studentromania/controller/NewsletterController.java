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

import com.app.studentromania.annotation.JWTAuth;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.dto.NewsletterDTO;
import com.app.studentromania.dto.SubscribeDTO;
import com.app.studentromania.service.NewsletterService;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/newsletter")
public class NewsletterController {

	@Autowired
	private NewsletterService newsletterService;

	@PostMapping("/save")
	@VerifyAdmin
	public ResponseEntity<String> saveNewsletter() {
		return newsletterService.saveNewsletter().createRestResponse();
	}

	@PutMapping("/reset")
	@VerifyAdmin
	public ResponseEntity<String> resetNewsletter() {
		return newsletterService.resetNewsletter().createRestResponse();
	}

	@PutMapping("/subscribe")
	@JWTAuth
	public ResponseEntity<String> addEmail(@RequestBody SubscribeDTO subscribeDTO) {
		return newsletterService.addEmail(subscribeDTO.getEmail()).createRestResponse();
	}

	@PutMapping("/unsubscribe")
	public ResponseEntity<String> removeEmail(@RequestBody SubscribeDTO subscribeDTO) {
		return newsletterService.removeEmail(subscribeDTO.getEmail()).createRestResponse();
	}

	@GetMapping
	@VerifyAdmin
	public ResponseEntity<String> getNewsletter() {
		return newsletterService.getNewsletter().createRestResponse();
	}

	@PostMapping("/send")
	@VerifyAdmin
	public ResponseEntity<String> sendNewsletter(@RequestBody NewsletterDTO newsletterDTO) {
		return newsletterService.sendNewsletter(newsletterDTO).createRestResponse();
	}

}
