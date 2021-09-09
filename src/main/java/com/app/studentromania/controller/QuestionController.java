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
import com.app.studentromania.dto.AnswerDTO;
import com.app.studentromania.dto.QuestionDTO;
import com.app.studentromania.service.QuestionService;
import com.app.studentromania.util.QuestionFilter;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/question")
public class QuestionController {

	@Autowired
	private QuestionService questionService;

	@GetMapping("/faculty/{facultyId}")
	@RestCall
	@JWTAuth
	public ResponseEntity<String> getByFacultyId(@PathVariable String facultyId, QuestionFilter questionFilter) {
		return questionService.getFilteredQuestionsByFacultyId(facultyId, questionFilter).createRestResponse();
	}

	@PostMapping("/{facultyId}")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> createQuestion(@PathVariable String facultyId, @RequestBody QuestionDTO questionDTO) {
		questionDTO.setFacultyId(facultyId);
		return questionService.createQuestion(questionDTO).createRestResponse();
	}

	@PostMapping("/{questionId}/answer")
	@RestCall
	@JWTAuth
	@VerifyLoggedIn
	public ResponseEntity<String> createAnswer(@PathVariable String questionId, @RequestBody AnswerDTO answerDTO) {
		answerDTO.setQuestionId(questionId);
		return questionService.createAnswer(answerDTO).createRestResponse();
	}

	@PutMapping("/{questionId}/upvote")
	@RestCall
	@JWTAuth
	public ResponseEntity<String> upvoteQuestion(@PathVariable String questionId,
			@RequestBody QuestionDTO questionDTO) {
		questionDTO.setQuestionId(questionId);
		return questionService.upvoteQuestion(questionDTO).createRestResponse();
	}

	@PutMapping("/{questionId}/upvoteanswer/{answerId}")
	@RestCall
	@JWTAuth
	public ResponseEntity<String> upvoteAnswer(@PathVariable String questionId, @PathVariable String answerId,
			@RequestBody AnswerDTO answerDTO) {
		answerDTO.setQuestionId(questionId);
		answerDTO.setAnswerId(answerId);
		return questionService.upvoteAnswer(answerDTO).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteAllQuestions() {
		return questionService.deleteAllQuestions().createRestResponse();
	}

}
