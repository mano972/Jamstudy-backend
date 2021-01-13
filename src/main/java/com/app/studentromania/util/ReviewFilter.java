package com.app.studentromania.util;

import org.apache.commons.lang3.StringUtils;

public class ReviewFilter {

	private static final int DEFAULT_LIMIT = 25;
	private static final String DEFAULT_SORT_FIELD = "reviewDate";
	private static final String DEFAULT_SORT_DIRECTION = "desc";
	private static final String SORT_SPLIT_CHARACTER = ",";

	private String searchBy;
	private Double ratingFrom;
	private Double ratingTo;
	private String orderBy;
	private Integer limit;
	private Integer pageNumber;
	private Integer offset;

	public ReviewFilter(String searchBy, Double ratingFrom, Double ratingTo, String orderBy, Integer limit,
			Integer pageNumber, Integer offset) {
		super();
		this.searchBy = searchBy;
		this.ratingFrom = ratingFrom;
		this.ratingTo = ratingTo;
		this.orderBy = orderBy;
		this.limit = limit;
		this.pageNumber = pageNumber;
		this.offset = offset;
	}

	public String getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(String searchBy) {
		this.searchBy = searchBy;
	}

	public Double getRatingFrom() {
		return ratingFrom;
	}

	public void setRatingFrom(Double ratingFrom) {
		this.ratingFrom = ratingFrom;
	}

	public Double getRatingTo() {
		return ratingTo;
	}

	public void setRatingTo(Double ratingTo) {
		this.ratingTo = ratingTo;
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

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Integer getOffset() {
		if (offset == null || offset == 0) {
			return 0;
		}
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public String getSearchByClause() {
		if (StringUtils.isEmpty(searchBy)) {
			return StringUtils.EMPTY;
		}
		return " and (UPPER(reviewText) LIKE '%" + searchBy.toUpperCase() + "%') ";
	}

	public String getRatingFromClause() {
		if (ratingFrom == null || ratingFrom == 0) {
			return StringUtils.EMPTY;
		}
		return " and generalRating >= " + ratingFrom;
	}

	public String getRatingToClause() {
		if (ratingTo == null || ratingTo == 0) {
			return StringUtils.EMPTY;
		}
		return " and generalRating <= " + ratingTo;
	}

	public String getOrderByClause() {
		if (StringUtils.isEmpty(orderBy)) {
			return " order by " + DEFAULT_SORT_FIELD + " " + DEFAULT_SORT_DIRECTION;
		}
		String[] sortInfo = orderBy.split(SORT_SPLIT_CHARACTER);
		return " order by " + sortInfo[0] + " " + sortInfo[1];
	}

	@Override
	public String toString() {
		return "ReviewFilter [searchBy=" + searchBy + ", ratingFrom=" + ratingFrom + ", ratingTo=" + ratingTo
				+ ", orderBy=" + orderBy + ", limit=" + limit + ", pageNumber=" + pageNumber + ", offset=" + offset
				+ "]";
	}
	
	

}
