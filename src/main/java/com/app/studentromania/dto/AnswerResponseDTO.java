package com.app.studentromania.dto;

import java.util.Date;

public class AnswerResponseDTO {

	private String answerId;

	private String questionId;

	private String answerText;

	private int upvotes;

	private Date answerDate;

	private String formattedAnswerDate;

	public String getAnswerId() {
		return answerId;
	}

	public void setAnswerId(String answerId) {
		this.answerId = answerId;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

	public int getUpvotes() {
		return upvotes;
	}

	public void setUpvotes(int upvotes) {
		this.upvotes = upvotes;
	}

	public Date getAnswerDate() {
		return answerDate;
	}

	public void setAnswerDate(Date answerDate) {
		this.answerDate = answerDate;
	}

	public String getFormattedAnswerDate() {
		return formattedAnswerDate;
	}

	public void setFormattedAnswerDate(String formattedAnswerDate) {
		this.formattedAnswerDate = formattedAnswerDate;
	}

}
