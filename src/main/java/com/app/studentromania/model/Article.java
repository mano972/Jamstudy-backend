package com.app.studentromania.model;

import java.util.Date;

import org.springframework.data.couchbase.core.mapping.Document;

import com.app.studentromania.enumtype.DocTypeEnum;

@Document
public class Article extends ParentEntity {

	public Article() {
		super(DocTypeEnum.ARTICLE);
	}

	private String articleId;

	private String authorName;

	private String articleTitle;

	private String articleText;

	private String articlePhoto;

	private Date articleDate;

	private String formattedArticleDate;

	private int viewCount;

	private int helpful;

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public String getArticleText() {
		return articleText;
	}

	public void setArticleText(String articleText) {
		this.articleText = articleText;
	}

	public String getArticlePhoto() {
		return articlePhoto;
	}

	public void setArticlePhoto(String articlePhoto) {
		this.articlePhoto = articlePhoto;
	}

	public Date getArticleDate() {
		return articleDate;
	}

	public void setArticleDate(Date articleDate) {
		this.articleDate = articleDate;
	}

	public String getFormattedArticleDate() {
		return formattedArticleDate;
	}

	public void setFormattedArticleDate(String formattedArticleDate) {
		this.formattedArticleDate = formattedArticleDate;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getHelpful() {
		return helpful;
	}

	public void setHelpful(int helpful) {
		this.helpful = helpful;
	}

}
