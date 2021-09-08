package com.app.studentromania.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dao.QuestionDAO;
import com.app.studentromania.dto.AnswerDTO;
import com.app.studentromania.dto.QuestionDTO;
import com.app.studentromania.dto.QuestionResponseDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Answer;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.Question;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.QuestionFilter;

@Service
public class QuestionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionService.class);

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private QuestionDAO questionDAO;

	@Autowired
	private FacultyDAO facultyDAO;
	
	private LogUtils logUtils;

	@Autowired
	public QuestionService(LogUtils logUtils) {
		this.logUtils = logUtils;
		LOGGER.info("QuestionService initialized");
	}

	public ResponseDTO getFilteredQuestionsByFacultyId(String facultyId, QuestionFilter questionFilter) {
		List<Question> questions = questionDAO.getFilteredQuestionsByFacultyId(facultyId, questionFilter);
		long count = questionDAO.countByFacultyId(facultyId);
		JSONArray questionsArray = new JSONArray();
		questions.forEach(question -> {
			QuestionResponseDTO questionResponseDTO = mapToQuestionResponseDTO(question);
			questionsArray.put(new JSONObject(questionResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("questions", questionsArray);
		response.put("count", count);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO createQuestion(QuestionDTO questionDTO) {
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(questionDTO.getFacultyId());
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Question question = new Question();
		String generatedId = configDAO.generateDocumentId(Constants.QUESTION_PREFIX_ID);
		question.setQuestionId(generatedId);
		question.setQuestionDate(new Date());
		mapToQuestion(questionDTO, question);
		questionDAO.createQuestion(question);
		QuestionResponseDTO questionResponseDTO = new QuestionResponseDTO();
		questionResponseDTO.setQuestionId(question.getQuestionId());
		questionResponseDTO.setFacultyId(question.getFacultyId());

		return ResponseDTO.createSuccessResponse(new JSONObject(questionResponseDTO));
	}

	public ResponseDTO createAnswer(AnswerDTO answerDTO) {
		Optional<Question> questionOpt = questionDAO.getByQuestionId(answerDTO.getQuestionId());
		if (!questionOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_NOT_FOUND);
		}
		Question question = questionOpt.get();
		Answer answer = new Answer();
		String generatedId = configDAO.generateDocumentId(Constants.ANSWER_PREFIX_ID);
		answer.setAnswerId(generatedId);
		answer.setAnswerDate(new Date());
		mapToAnswer(answerDTO, answer);
		List<Answer> answers = question.getAnswers();
		answers.add(answer);
		logUtils.logMessage(LOGGER,
				"Answer " + answer.getAnswerId() + " was created for question " + question.getQuestionId());
		questionDAO.updateQuestion(question);
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO upvoteQuestion(QuestionDTO questionDTO) {
		Optional<Question> questionOpt = questionDAO.getByQuestionId(questionDTO.getQuestionId());
		if (!questionOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_NOT_FOUND);
		}
		Question question = questionOpt.get();
		int upvotes = question.getUpvotes();
		if (questionDTO.getUpvote()) {
			question.setUpvotes(++upvotes);
			logUtils.logMessage(LOGGER, "Question " + question.getQuestionId() + " was upvoted!");
		} else {
			if (upvotes == 0) {
				question.setUpvotes(0);
			} else {
				question.setUpvotes(--upvotes);
			}
			logUtils.logMessage(LOGGER, "Question " + question.getQuestionId() + " was downvoted!");
		}
		questionDAO.updateQuestion(question);
		QuestionResponseDTO questionResponseDTO = new QuestionResponseDTO();
		questionResponseDTO.setQuestionId(question.getQuestionId());
		questionResponseDTO.setFacultyId(question.getFacultyId());
		questionResponseDTO.setUpvotes(question.getUpvotes());

		return ResponseDTO.createSuccessResponse(new JSONObject(questionResponseDTO));
	}

	public ResponseDTO upvoteAnswer(AnswerDTO answerDTO) {
		Optional<Question> questionOpt = questionDAO.getByQuestionId(answerDTO.getQuestionId());
		if (!questionOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_NOT_FOUND);
		}
		Question question = questionOpt.get();
		Optional<Answer> answerOpt = question.getAnswers().stream()
				.filter(answer -> answerDTO.getAnswerId().equals(answer.getAnswerId())).findAny();
		if (!answerOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANSWER_NOT_FOUND);
		}
		Answer answer = answerOpt.get();
		int upvotes = answer.getUpvotes();
		if (answerDTO.getUpvote()) {
			answer.setUpvotes(++upvotes);
			logUtils.logMessage(LOGGER, "Answer " + answer.getAnswerId() + " was upvoted!");
		} else {
			if (upvotes == 0) {
				answer.setUpvotes(0);
			} else {
				answer.setUpvotes(--upvotes);
			}
			logUtils.logMessage(LOGGER, "Answer " + answer.getAnswerId() + " was downvoted!");
		}
		questionDAO.updateQuestion(question);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO deleteAllQuestions() {
		questionDAO.deleteAllQuestions();
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	private void mapToAnswer(AnswerDTO answerDTO, Answer answer) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		mapper.map(answerDTO, answer);
	}

	private void mapToQuestion(QuestionDTO questionDTO, Question question) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		mapper.map(questionDTO, question);
	}

	private QuestionResponseDTO mapToQuestionResponseDTO(Question question) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper.map(question, QuestionResponseDTO.class);
	}

}
