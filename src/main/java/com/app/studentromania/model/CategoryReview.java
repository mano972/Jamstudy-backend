package com.app.studentromania.model;

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
		this.reviewText = reviewText;
	}

	@Override
	public String toString() {
		return "CategoryReview [generalRating=" + generalRating + ", reviewText=" + reviewText + "]";
	}
	
}
