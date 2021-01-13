package com.app.studentromania.util;

import org.apache.commons.lang3.StringUtils;

public class QuestionFilter {

	private static final int DEFAULT_LIMIT = 25;
	private static final String DEFAULT_SORT_FIELD = "questionDate";
	private static final String DEFAULT_SORT_DIRECTION = "desc";
	private static final String SORT_SPLIT_CHARACTER = ",";

	private String orderBy;
	private Integer limit;
	private Integer offset;
	private Integer pageNumber;

	public QuestionFilter(String orderBy, Integer limit, Integer offset, Integer pageNumber) {
		super();
		this.orderBy = orderBy;
		this.limit = limit;
		this.offset = offset;
		this.pageNumber = pageNumber;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Integer getLimit() {
		if (limit == null || limit == 0) {
			return DEFAULT_LIMIT;
		}
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getOrderByClause() {
		if (StringUtils.isEmpty(orderBy)) {
			return " order by " + DEFAULT_SORT_FIELD + " " + DEFAULT_SORT_DIRECTION;
		}
		String[] sortInfo = orderBy.split(SORT_SPLIT_CHARACTER);
		return " order by q." + sortInfo[0] + " " + sortInfo[1];
	}

	@Override
	public String toString() {
		return "QuestionFilter [orderBy=" + orderBy + ", limit=" + limit + ", offset=" + offset + ", pageNumber="
				+ pageNumber + "]";
	}
	
	

}
