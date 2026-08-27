package com.app.studentromania.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.document.json.JsonArray;
import org.apache.commons.lang3.StringUtils;

public class ReviewFilter {

	private static final int DEFAULT_LIMIT = 25;
	private static final int ADMIN_LIMIT = 999;
	private static final String DEFAULT_SORT_FIELD = "reviewDate";
	private static final String DEFAULT_SORT_FIELD_JOIN = "r.reviewDate";
	private static final String DEFAULT_SORT_DIRECTION = "desc";
	private static final Set<String> ALLOWED_SORT_FIELDS = new HashSet<>(
			Arrays.asList("reviewDate", "upvotes", "generalRating"));

	private String searchBy;
	private Double ratingFrom;
	private Double ratingTo;
	private String countryCode;
	private List<String> userStatus;
	private String orderBy;
	private Integer limit;
	private Integer pageNumber;
	private Integer offset;
	private boolean join;

	public ReviewFilter(String searchBy, Double ratingFrom, Double ratingTo, String countryCode, List<String> userStatus,
			String orderBy, Integer limit, Integer pageNumber, Integer offset) {
		super();
		this.searchBy = searchBy;
		this.ratingFrom = ratingFrom;
		this.ratingTo = ratingTo;
		this.countryCode = countryCode;
		this.userStatus = userStatus;
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

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public List<String> getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(List<String> userStatus) {
		this.userStatus = userStatus;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Integer getLimit() {
		if (limit == null || limit == 0 || (limit > 25 && limit != ADMIN_LIMIT)) {
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

	public boolean isJoin() {
		return join;
	}

	public void setJoin(boolean join) {
		this.join = join;
	}

	public String getSearchByClause() {
		if (StringUtils.isEmpty(searchBy)) {
			return StringUtils.EMPTY;
		}
		String normalizedSearchBy = N1qlUtils.escapeStringLiteral(normalizeDiacritics(searchBy).toUpperCase().trim());
		if (isJoin()) {
			return " and (UPPER(" + diacriticsInsensitive("r.reviewText") + ") LIKE '%" + normalizedSearchBy + "%'"
					+ " or UPPER(" + diacriticsInsensitive("f.facultyName") + ") LIKE '%" + normalizedSearchBy + "%'"
					+ " or UPPER(" + diacriticsInsensitive("f.universityName") + ") LIKE '%" + normalizedSearchBy + "%'"
					+ " or UPPER(" + diacriticsInsensitive("f.facultyShortname") + ") LIKE '%" + normalizedSearchBy + "%'"
					+ " or UPPER(" + diacriticsInsensitive("f.facultyCity") + ") LIKE '%" + normalizedSearchBy + "%') ";
		}
		return " and (UPPER(" + diacriticsInsensitive("reviewText") + ") LIKE '%" + normalizedSearchBy + "%') ";
	}

	public String getRatingFromClause() {
		if (ratingFrom == null || ratingFrom == 0) {
			return StringUtils.EMPTY;
		}
		return " and " + (isJoin() ? "r." : "") + "generalRating >= " + ratingFrom;
	}

	public String getRatingToClause() {
		if (ratingTo == null || ratingTo == 0) {
			return StringUtils.EMPTY;
		}
		return " and " + (isJoin() ? "r." : "") + "generalRating <= " + ratingTo;
	}

	public String getCountryCodeClause() {
		if (StringUtils.isEmpty(countryCode)) {
			return StringUtils.EMPTY;
		}
		return " and " + (isJoin() ? "r." : "") + "countryCode IN " + JsonArray.from(countryCode);
	}

	public String getUserStatusClause() {
		if (userStatus == null || userStatus.isEmpty()) {
			return StringUtils.EMPTY;
		}
		return " and " + (isJoin() ? "r." : "") + "userStatus IN " + JsonArray.from(userStatus);
	}

	public String getOrderByClause() {
		String defaultClause = " order by " + (isJoin() ? DEFAULT_SORT_FIELD_JOIN : DEFAULT_SORT_FIELD) + " "
				+ DEFAULT_SORT_DIRECTION;
		return N1qlUtils.sanitizeOrderByClause(orderBy, ALLOWED_SORT_FIELDS, isJoin() ? "r." : "", defaultClause);
	}

	// Some faculties/reviews store ș/ț with the legacy cedilla forms (ş/ţ) instead of
	// the correct comma-below forms (ș/ț) - same rendered look, different codepoints -
	// so search needs to be diacritics-insensitive on both the query and the data.
	private static String normalizeDiacritics(String text) {
		return text.replaceAll("[ĂăÂâ]", "A").replaceAll("[ŞşȘș]", "S").replaceAll("[ŢţȚț]", "T")
				.replaceAll("[Îî]", "I");
	}

	private static String diacriticsInsensitive(String field) {
		return "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(" + field
				+ ", '[ĂăÂâ]', 'A'), '[ŞşȘș]', 'S'), '[ŢţȚț]', 'T'), '[Îî]', 'I')";
	}

	@Override
	public String toString() {
		return "ReviewFilter{" +
				"searchBy='" + searchBy + '\'' +
				", ratingFrom=" + ratingFrom +
				", ratingTo=" + ratingTo +
				", countryCode='" + countryCode + '\'' +
				", userStatus=" + userStatus +
				", orderBy='" + orderBy + '\'' +
				", limit=" + limit +
				", pageNumber=" + pageNumber +
				", offset=" + offset +
				'}';
	}
}
