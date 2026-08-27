package com.app.studentromania.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class QuestionFilter {

	private static final int DEFAULT_LIMIT = 25;
	private static final String DEFAULT_SORT_FIELD = "questionDate";
	private static final String DEFAULT_SORT_DIRECTION = "desc";
	private static final Set<String> ALLOWED_SORT_FIELDS = new HashSet<>(Arrays.asList("questionDate", "upvotes"));

	private String searchBy;
	private String orderBy;
	private Integer limit;
	private Integer offset;
	private Integer pageNumber;

	public QuestionFilter(String searchBy, String orderBy, Integer limit, Integer offset, Integer pageNumber) {
		super();
		this.searchBy = searchBy;
		this.orderBy = orderBy;
		this.limit = limit;
		this.offset = offset;
		this.pageNumber = pageNumber;
	}

	public String getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(String searchBy) {
		this.searchBy = searchBy;
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
		if (offset == null || offset == 0) {
			return 0;
		}
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

	public String getSearchByClause() {
		if (StringUtils.isEmpty(searchBy)) {
			return StringUtils.EMPTY;
		}
		String normalizedSearchBy = N1qlUtils.escapeStringLiteral(searchBy.toUpperCase().trim());
		return " and (UPPER(title) LIKE '%" + normalizedSearchBy + "%' or UPPER(questionText) LIKE '%" + normalizedSearchBy + "%') ";
	}

	public String getOrderByClause() {
		return N1qlUtils.sanitizeOrderByClause(orderBy, ALLOWED_SORT_FIELDS, "",
				" order by " + DEFAULT_SORT_FIELD + " " + DEFAULT_SORT_DIRECTION);
	}

	@Override
	public String toString() {
		return "QuestionFilter{" +
				"searchBy='" + searchBy + '\'' +
				", orderBy='" + orderBy + '\'' +
				", limit=" + limit +
				", offset=" + offset +
				", pageNumber=" + pageNumber +
				'}';
	}
}
