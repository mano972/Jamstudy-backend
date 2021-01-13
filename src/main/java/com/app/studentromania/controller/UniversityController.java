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

import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.dto.UniversityDTO;
import com.app.studentromania.service.UniversityService;

@CrossOrigin()
@RestController
@RequestMapping("university")
public class UniversityController {

	@Autowired
	private UniversityService universityService;

	@GetMapping
	@RestCall
	public ResponseEntity<String> getAllUniversities() {
		return universityService.getAllUniversities().createRestResponse();
	}

	@GetMapping("/{universityId}")
	@RestCall
	public ResponseEntity<String> getByUniversityId(@PathVariable String universityId) {
		return universityService.getByUniversityId(universityId).createRestResponse();
	}

	@PostMapping()
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> createUniversity(@RequestBody UniversityDTO universityDTO) {
		return universityService.createUniversity(universityDTO).createRestResponse();
	}

	@PutMapping("/{universityId}")
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> updateUniversity(@PathVariable String universityId,
			@RequestBody UniversityDTO universityDTO) {
		universityDTO.setUniversityId(universityId);
		return universityService.updateUniversity(universityDTO).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteAllUniversities() {
		return universityService.deleteAllUniversities().createRestResponse();
	}

}
