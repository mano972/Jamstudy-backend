package com.app.studentromania.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.util.HtmlSanitizer;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class Question extends ParentEntity {

	public Question() {
		super(DocTypeEnum.QUESTION);
	}

	@Field
	private String questionId;

	@Field
	private String facultyId;

	@Field
	private String facultyName;

	@Field
	private String userId;
	
	@Field
	private String userEmail;

	@Field
	private String title;

	@Field
	private String questionText;

	@Field
	private int upvotes;

	@Field
	private String category;

	@Field
	private Date questionDate;

	@Field
	private String formattedQuestionDate;

	@Field
	private List<Answer> answers = new ArrayList<>();

	@Field
	private int reports;

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getFacultyId() {
		return facultyId;
	}

	public void setFacultyId(String facultyId) {
		this.facultyId = facultyId;
	}

	public String getFacultyName() {
		return facultyName;
	}

	public void setFacultyName(String facultyName) {
		this.facultyName = facultyName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = HtmlSanitizer.stripHtml(title);
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = HtmlSanitizer.stripHtml(questionText);
	}

	public int getUpvotes() {
		return upvotes;
	}

	public void setUpvotes(int upvotes) {
		this.upvotes = upvotes;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getQuestionDate() {
		return questionDate;
	}

	public void setQuestionDate(Date questionDate) {
		this.questionDate = questionDate;
	}

	public String getFormattedQuestionDate() {
		return formattedQuestionDate;
	}

	public void setFormattedQuestionDate(String formattedQuestionDate) {
		this.formattedQuestionDate = formattedQuestionDate;
	}

	public List<Answer> getAnswers() {
		return answers;
	}

	public void setAnswers(List<Answer> answers) {
		this.answers = answers;
	}

	public int getReports() {
		return reports;
	}

	public void setReports(int reports) {
		this.reports = reports;
	}

}
