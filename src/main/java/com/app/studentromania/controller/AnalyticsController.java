package com.app.studentromania.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.service.AnalyticsService;

@CrossOrigin()
@RestController
@RequestMapping("analytics")
public class AnalyticsController {

	@Autowired
	private AnalyticsService analyticsService;

	@PostMapping
	@VerifyAdmin
	public ResponseEntity<String> saveStatistic() {
		return analyticsService.saveAnalytics().createRestResponse();
	}

	@PutMapping("/reset")
	@VerifyAdmin
	public ResponseEntity<String> resetStatistic() {
		return analyticsService.resetAnalytics().createRestResponse();
	}

	@PutMapping("/homepage")
	public void homePageStatistic() {
		analyticsService.increaseHomePageStatistic();
	}

	@PutMapping("/search")
	public void searchStatistic() {
		analyticsService.increaseSearchStatistic();
	}

	@PutMapping("/revint")
	public void reviewIntentionStatistic() {
		analyticsService.increaseReviewIntentionStatistic();
	}

	@PutMapping("/revadd")
	public void reviewAddedStatistic() {
		analyticsService.increaseReviewAddedStatistic();
	}

	@GetMapping
	@VerifyAdmin
	public ResponseEntity<String> getAnalytics() {
		return analyticsService.getAnalytics().createRestResponse();
	}

}
