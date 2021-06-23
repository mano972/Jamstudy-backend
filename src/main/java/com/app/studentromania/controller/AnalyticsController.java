package com.app.studentromania.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.service.AnalyticsService;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/analytics")
public class AnalyticsController {

	@Autowired
	private AnalyticsService analyticsService;

	@PostMapping
	@VerifyAdmin
	public ResponseEntity<String> saveAnalytics() {
		return analyticsService.saveAnalytics().createRestResponse();
	}

	@PutMapping("/reset")
	@VerifyAdmin
	public ResponseEntity<String> resetAnalytics() {
		return analyticsService.resetAnalytics().createRestResponse();
	}

	@PutMapping("/reset/excel")
	@VerifyAdmin
	public ResponseEntity<String> resetAnalyticsExcel() {
		return analyticsService.resetAnalyticsExcel().createRestResponse();
	}

	@PutMapping("/homepage")
	public ResponseEntity<String> homePageStatistic(@RequestParam boolean m) {
		return analyticsService.increaseHomePageStatistic(m).createRestResponse();
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
