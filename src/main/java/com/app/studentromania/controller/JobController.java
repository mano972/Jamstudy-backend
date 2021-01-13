package com.app.studentromania.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.service.FacultyService;
import com.app.studentromania.util.FacultyFilter;

@RestController
@RequestMapping("job")
public class JobController {

	@Autowired
	private FacultyService facultyService;

	@GetMapping("/facultyreviewdetails")
	@RestCall
	public ResponseEntity<String> updateFacultyReviewDetails(FacultyFilter facultyFilter) {
		facultyService.updateAllFacultiesReviewDetails();
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS).createRestResponse();
	}

}
