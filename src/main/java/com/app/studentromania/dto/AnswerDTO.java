package com.app.studentromania.dto;

public class AnswerDTO {

	private String answerId;

	private String questionId;

	private String answerText;

	/*
	 * true for upvote, false for not upvote (reversal of upvote)
	 */
	private Boolean upvote;

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

	public Boolean getUpvote() {
		return upvote;
	}

	public void setUpvote(Boolean upvote) {
		this.upvote = upvote;
	}

}
