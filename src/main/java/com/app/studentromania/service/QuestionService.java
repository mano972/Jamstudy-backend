package com.app.studentromania.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.app.studentromania.dao.UserProfileDAO;
import com.app.studentromania.dto.AnswerDTO;
import com.app.studentromania.dto.AnswerResponseDTO;
import com.app.studentromania.dto.QuestionDTO;
import com.app.studentromania.dto.QuestionResponseDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Answer;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.Question;
import com.app.studentromania.model.UserProfile;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.QuestionFilter;
import com.app.studentromania.util.Utilities;

@Service
public class QuestionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionService.class);

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private QuestionDAO questionDAO;

	@Autowired
	private FacultyDAO facultyDAO;

	@Autowired
	private UserProfileDAO userProfileDAO;

	@Autowired
	private LogUtils logUtils;

	@Autowired
	private CustomRequestContext customRequestContext;

	public ResponseDTO getFilteredQuestionsByFacultyId(String facultyId, QuestionFilter questionFilter) {
		List<Question> questions = questionDAO.getFilteredQuestionsByFacultyId(facultyId, questionFilter);
		long count = questionDAO.countFilteredQuestions(facultyId, questionFilter);
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
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();

		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(questionDTO.getFacultyId());
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}

		if (questionDTO.getTitle() == null || questionDTO.getTitle().trim().length() < Constants.QUESTION_TITLE_MIN_LENGTH
				|| questionDTO.getTitle().trim().length() > Constants.QUESTION_TITLE_MAX_LENGTH) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_TITLE_LENGTH_ERROR);
		}

		if (questionDTO.getQuestionText() == null
				|| questionDTO.getQuestionText().trim().length() < Constants.QUESTION_MIN_LENGTH
				|| questionDTO.getQuestionText().trim().length() > Constants.QUESTION_MAX_LENGTH) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_LENGTH_ERROR);
		}

		Question question = new Question();
		String generatedId = configDAO.generateDocumentId(Constants.QUESTION_PREFIX_ID);
		question.setQuestionId(generatedId);
		question.setFacultyName(facultyOpt.get().getFacultyName());
		Date currentDate = new Date();
		question.setQuestionDate(currentDate);
		question.setFormattedQuestionDate(Utilities.getFormattedDate(currentDate));
		mapToQuestion(questionDTO, question);
		question.setUserId(userProfile.getUserId());
		question.setUserEmail(userProfile.getEmail());
		questionDAO.createQuestion(question);
		QuestionResponseDTO questionResponseDTO = new QuestionResponseDTO();
		questionResponseDTO.setQuestionId(question.getQuestionId());
		questionResponseDTO.setFacultyId(question.getFacultyId());

		return ResponseDTO.createSuccessResponse(new JSONObject(questionResponseDTO));
	}

	public ResponseDTO createAnswer(AnswerDTO answerDTO) {
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();

		Optional<Question> questionOpt = questionDAO.getByQuestionId(answerDTO.getQuestionId());
		if (!questionOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_NOT_FOUND);
		}

		if (answerDTO.getAnswerText() == null
				|| answerDTO.getAnswerText().trim().length() < Constants.ANSWER_MIN_LENGTH
				|| answerDTO.getAnswerText().trim().length() > Constants.ANSWER_MAX_LENGTH) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANSWER_LENGTH_ERROR);
		}

		Question question = questionOpt.get();
		Answer answer = new Answer();
		String generatedId = configDAO.generateDocumentId(Constants.ANSWER_PREFIX_ID);
		answer.setAnswerId(generatedId);
		Date currentDate = new Date();
		answer.setAnswerDate(currentDate);
		answer.setFormattedAnswerDate(Utilities.getFormattedDate(currentDate));
		mapToAnswer(answerDTO, answer);
		answer.setUserId(userProfile.getUserId());
		List<Answer> answers = question.getAnswers();
		answers.add(answer);
		logUtils.logMessage(LOGGER,
				"Answer " + answer.getAnswerId() + " was created for question " + question.getQuestionId());
		questionDAO.updateQuestion(question);
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO upvoteQuestion(QuestionDTO questionDTO) {
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();

		Optional<Question> questionOpt = questionDAO.getByQuestionId(questionDTO.getQuestionId());
		if (!questionOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_NOT_FOUND);
		}
		Question question = questionOpt.get();
		int upvotes = question.getUpvotes();
		if (questionDTO.getUpvote()) {
			if (userProfile.getLikedQuestions().contains(questionDTO.getQuestionId())) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_ALREADY_UPVOTED);
			}
			question.setUpvotes(++upvotes);
			userProfile.getLikedQuestions().add(questionDTO.getQuestionId());
			logUtils.logMessage(LOGGER, "Question " + question.getQuestionId() + " was upvoted!");
		} else {
			if (!userProfile.getLikedQuestions().contains(questionDTO.getQuestionId())) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_ALREADY_UPVOTED);
			}
			if (upvotes == 0) {
				question.setUpvotes(0);
			} else {
				question.setUpvotes(--upvotes);
			}
			userProfile.getLikedQuestions().remove(questionDTO.getQuestionId());
			logUtils.logMessage(LOGGER, "Question " + question.getQuestionId() + " was downvoted!");
		}
		questionDAO.updateQuestion(question);
		userProfileDAO.updateUserProfile(userProfile);
		QuestionResponseDTO questionResponseDTO = new QuestionResponseDTO();
		questionResponseDTO.setQuestionId(question.getQuestionId());
		questionResponseDTO.setFacultyId(question.getFacultyId());
		questionResponseDTO.setUpvotes(question.getUpvotes());

		return ResponseDTO.createSuccessResponse(new JSONObject(questionResponseDTO));
	}

	public ResponseDTO upvoteAnswer(AnswerDTO answerDTO) {
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();

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
			if (userProfile.getLikedAnswers().contains(answerDTO.getAnswerId())) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.ANSWER_ALREADY_UPVOTED);
			}
			answer.setUpvotes(++upvotes);
			userProfile.getLikedAnswers().add(answerDTO.getAnswerId());
			logUtils.logMessage(LOGGER, "Answer " + answer.getAnswerId() + " was upvoted!");
		} else {
			if (!userProfile.getLikedAnswers().contains(answerDTO.getAnswerId())) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.ANSWER_ALREADY_UPVOTED);
			}
			if (upvotes == 0) {
				answer.setUpvotes(0);
			} else {
				answer.setUpvotes(--upvotes);
			}
			userProfile.getLikedAnswers().remove(answerDTO.getAnswerId());
			logUtils.logMessage(LOGGER, "Answer " + answer.getAnswerId() + " was downvoted!");
		}
		questionDAO.updateQuestion(question);
		userProfileDAO.updateUserProfile(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO reportQuestion(String questionId) {
		Optional<Question> questionOpt = questionDAO.getByQuestionId(questionId);
		if (!questionOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_NOT_FOUND);
		}
		Question question = questionOpt.get();
		int reports = question.getReports();
		question.setReports(++reports);
		logUtils.logMessage(LOGGER, "Question " + question.getQuestionId() + " was reported!");
		questionDAO.updateQuestion(question);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO reportAnswer(String questionId, String answerId) {
		Optional<Question> questionOpt = questionDAO.getByQuestionId(questionId);
		if (!questionOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.QUESTION_NOT_FOUND);
		}
		Question question = questionOpt.get();
		Optional<Answer> answerOpt = question.getAnswers().stream()
				.filter(answer -> answerId.equals(answer.getAnswerId())).findAny();
		if (!answerOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANSWER_NOT_FOUND);
		}
		Answer answer = answerOpt.get();
		int reports = answer.getReports();
		answer.setReports(++reports);
		logUtils.logMessage(LOGGER, "Answer " + answer.getAnswerId() + " was reported!");
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
		QuestionResponseDTO questionResponseDTO = mapper.map(question, QuestionResponseDTO.class);
		questionResponseDTO.setAnswers(mapToAnswerResponseDTOs(question.getAnswers()));
		return questionResponseDTO;
	}

	// Answer is not mapped through ModelMapper here on purpose: it carries userId
	// internally for moderation, which must never reach the client response.
	private List<AnswerResponseDTO> mapToAnswerResponseDTOs(List<Answer> answers) {
		return answers.stream().map(answer -> {
			AnswerResponseDTO answerResponseDTO = new AnswerResponseDTO();
			answerResponseDTO.setAnswerId(answer.getAnswerId());
			answerResponseDTO.setQuestionId(answer.getQuestionId());
			answerResponseDTO.setAnswerText(answer.getAnswerText());
			answerResponseDTO.setUpvotes(answer.getUpvotes());
			answerResponseDTO.setAnswerDate(answer.getAnswerDate());
			answerResponseDTO.setFormattedAnswerDate(answer.getFormattedAnswerDate());
			return answerResponseDTO;
		}).collect(Collectors.toList());
	}

}
