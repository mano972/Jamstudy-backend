package com.app.studentromania.model;

import java.util.Date;

import com.app.studentromania.util.HtmlSanitizer;

public class Answer {

	private String answerId;

	private String questionId;

	private String userId;

	private String answerText;

	private int upvotes;

	private int reports;

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = HtmlSanitizer.stripHtml(answerText);
	}

	public int getUpvotes() {
		return upvotes;
	}

	public void setUpvotes(int upvotes) {
		this.upvotes = upvotes;
	}

	public int getReports() {
		return reports;
	}

	public void setReports(int reports) {
		this.reports = reports;
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
