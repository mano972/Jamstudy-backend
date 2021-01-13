package com.app.studentromania.dao;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.model.Question;
import com.app.studentromania.repo.QuestionRepo;
import com.app.studentromania.util.QuestionFilter;

@Component
public class QuestionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionDAO.class);

	@Autowired
	private QuestionRepo questionRepo;

	public List<Question> getFilteredQuestionsByFacultyId(String facultyId, QuestionFilter questionFilter) {
		return questionRepo.getFilteredQuestionsByFacultyId(facultyId, questionFilter);
	}

	public Optional<Question> getByQuestionId(String questionId) {
		return questionRepo.findByQuestionId(questionId).stream().findFirst();
	}

	public long countByFacultyId(String facultyId) {
		return questionRepo.countByFacultyId(facultyId);
	}

	public void createQuestion(Question question) {
		questionRepo.save(question);
		LOGGER.info("Question " + question.getQuestionId() + " was created for faculty " + question.getFacultyId());
	}

	public void updateQuestion(Question question) {
		question.setUpdateDateWithCurrentDate();
		questionRepo.save(question);
		LOGGER.info("Question " + question.getQuestionId() + " was updated!");
	}

	public void deleteAllQuestions() {
		questionRepo.deleteAll();
	}

//	private List<Question> mapResultToQuestion(N1qlQueryResult result) {
//		List<Question> questions = new ArrayList<>();
//
//		for (N1qlQueryRow row : result) {
//			Question question = new Question();
//			JsonObject jsonObject = row.value();
//			if (jsonObject.containsKey("questionId") && jsonObject.getString("questionId") != null) {
//				String questionId = jsonObject.getString("questionId");
//				question.setQuestionId(questionId);
//			}
//			if (jsonObject.containsKey("facultyId") && jsonObject.getString("facultyId") != null) {
//				String facultyId = jsonObject.getString("facultyId");
//				question.setFacultyId(facultyId);
//			}
//			if (jsonObject.containsKey("questionText") && jsonObject.getString("questionText") != null) {
//				String questionText = jsonObject.getString("questionText");
//				question.setQuestionText(questionText);
//			}
//			if (jsonObject.containsKey("upvotes") && jsonObject.getInt("upvotes") != null) {
//				int upvotes = jsonObject.getInt("upvotes");
//				question.setUpvotes(upvotes);
//			}
//			if (jsonObject.containsKey("questionDate") && jsonObject.getLong("questionDate") != null) {
//				long questionDateLong = jsonObject.getLong("questionDate");
//				Date questionDate = new Date(questionDateLong);
//				question.setQuestionDate(questionDate);
//			}
//			List<Answer> answers = new ArrayList<>();
//			if (jsonObject.containsKey("answers") && jsonObject.get("answers") != null) {
//				JsonArray jsonArray = (JsonArray) jsonObject.get("answers");
//				for (int i = 0; i < jsonArray.size(); i++) {
//					JsonObject jsonObjectAnswer = (JsonObject) jsonArray.get(i);
//					Answer answer = mapJsonToAnswer(jsonObjectAnswer);
//					answers.add(answer);
//				}
//				List<Answer> sortedAnswersList = answers.stream()
//						.sorted(Comparator.comparing(Answer::getAnswerDate).reversed()).collect(Collectors.toList());
//				question.setAnswers(sortedAnswersList);
//			}
//			questions.add(question);
//		}
//
//		return questions;
//	}
//
//	private Answer mapJsonToAnswer(JsonObject jsonObjectAnswer) {
//		Answer answer = new Answer();
//
//		if (jsonObjectAnswer.containsKey("answerId") && jsonObjectAnswer.getString("answerId") != null) {
//			String answerId = jsonObjectAnswer.getString("answerId");
//			answer.setAnswerId(answerId);
//		}
//		if (jsonObjectAnswer.containsKey("questionId") && jsonObjectAnswer.getString("questionId") != null) {
//			String questionId = jsonObjectAnswer.getString("questionId");
//			answer.setQuestionId(questionId);
//		}
//		if (jsonObjectAnswer.containsKey("answerText") && jsonObjectAnswer.getString("answerText") != null) {
//			String answerText = jsonObjectAnswer.getString("answerText");
//			answer.setAnswerText(answerText);
//		}
//		if (jsonObjectAnswer.containsKey("upvotes") && jsonObjectAnswer.getInt("upvotes") != null) {
//			int upvotes = jsonObjectAnswer.getInt("upvotes");
//			answer.setUpvotes(upvotes);
//		}
//		if (jsonObjectAnswer.containsKey("answerDate") && jsonObjectAnswer.getLong("answerDate") != null) {
//			long answerDateLong = jsonObjectAnswer.getLong("answerDate");
//			Date answerDate = new Date(answerDateLong);
//			answer.setAnswerDate(answerDate);
//		}
//
//		return answer;
//	}

}
