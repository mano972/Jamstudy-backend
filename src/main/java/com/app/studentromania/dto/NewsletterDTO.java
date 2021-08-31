package com.app.studentromania.dto;

import java.util.Map;

public class NewsletterDTO {

	private String to;

	private String subject;

	private String newsletterBody;

	private Map<String, String> inlineImages;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getNewsletterBody() {
		return newsletterBody;
	}

	public void setNewsletterBody(String newsletterBody) {
		this.newsletterBody = newsletterBody;
	}

	public Map<String, String> getInlineImages() {
		return inlineImages;
	}

	public void setInlineImages(Map<String, String> inlineImages) {
		this.inlineImages = inlineImages;
	}

}
