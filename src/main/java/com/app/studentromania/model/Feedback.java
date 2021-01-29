package com.app.studentromania.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class Feedback extends ParentEntity {

	public Feedback() {
		super(DocTypeEnum.FEEDBACK);
	}

	@Field
	private List<FeedbackEntry> feedbackEntries = new ArrayList<FeedbackEntry>();

	public List<FeedbackEntry> getFeedbackEntries() {
		return feedbackEntries;
	}

	public void setFeedbackEntries(List<FeedbackEntry> feedbackEntries) {
		this.feedbackEntries = feedbackEntries;
	}

}
