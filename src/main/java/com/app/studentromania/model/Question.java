package com.app.studentromania.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
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
	private String questionText;

	@Field
	private int upvotes;

	@Field
	private Date questionDate;

	@Field
	private List<Answer> answers = new ArrayList<>();

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

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public int getUpvotes() {
		return upvotes;
	}

	public void setUpvotes(int upvotes) {
		this.upvotes = upvotes;
	}

	public Date getQuestionDate() {
		return questionDate;
	}

	public void setQuestionDate(Date questionDate) {
		this.questionDate = questionDate;
	}

	public List<Answer> getAnswers() {
		return answers;
	}

	public void setAnswers(List<Answer> answers) {
		this.answers = answers;
	}

}
