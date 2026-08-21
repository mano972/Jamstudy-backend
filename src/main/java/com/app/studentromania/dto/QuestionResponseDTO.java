package com.app.studentromania.dto;

import java.util.Date;
import java.util.List;

public class QuestionResponseDTO {

	private String questionId;

	private String facultyId;

	private String title;

	private String questionText;

	private String category;

	private int upvotes;

	private Date questionDate;

	private String formattedQuestionDate;

	private List<AnswerResponseDTO> answers;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
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

	public String getFormattedQuestionDate() {
		return formattedQuestionDate;
	}

	public void setFormattedQuestionDate(String formattedQuestionDate) {
		this.formattedQuestionDate = formattedQuestionDate;
	}

	public List<AnswerResponseDTO> getAnswers() {
		return answers;
	}

	public void setAnswers(List<AnswerResponseDTO> answers) {
		this.answers = answers;
	}

}
