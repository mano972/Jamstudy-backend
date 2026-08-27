package com.app.studentromania.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.Auth;
import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.service.StatsService;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/stats")
public class StatsController {

	@Autowired
	private StatsService statsService;

	@GetMapping
	@RestCall
	@Auth
	public ResponseEntity<String> getHomepageStats() {
		return statsService.getHomepageStats().createRestResponse();
	}

}
