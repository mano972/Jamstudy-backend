package com.app.studentromania.model;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class Analytics extends ParentEntity {

	public Analytics() {
		super(DocTypeEnum.ANALYTICS);
	}

	@Field
	private long homePage;

	@Field
	private long search;

	@Field
	private long reviewIntention;

	@Field
	private long reviewAdded;

	public long getHomePage() {
		return homePage;
	}

	public void setHomePage(long homePage) {
		this.homePage = homePage;
	}

	public long getSearch() {
		return search;
	}

	public void setSearch(long search) {
		this.search = search;
	}

	public long getReviewIntention() {
		return reviewIntention;
	}

	public void setReviewIntention(long reviewIntention) {
		this.reviewIntention = reviewIntention;
	}

	public long getReviewAdded() {
		return reviewAdded;
	}

	public void setReviewAdded(long reviewAdded) {
		this.reviewAdded = reviewAdded;
	}

}
