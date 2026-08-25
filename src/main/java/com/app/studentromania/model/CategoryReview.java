package com.app.studentromania.model;

import com.app.studentromania.util.HtmlSanitizer;

public class CategoryReview {

	private Integer generalRating;

	private String reviewText;

	public Integer getGeneralRating() {
		return generalRating;
	}

	public void setGeneralRating(Integer generalRating) {
		this.generalRating = generalRating;
	}

	public String getReviewText() {
		return reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = HtmlSanitizer.stripHtml(reviewText);
	}

	@Override
	public String toString() {
		return "CategoryReview [generalRating=" + generalRating + ", reviewText=" + reviewText + "]";
	}
	
}
