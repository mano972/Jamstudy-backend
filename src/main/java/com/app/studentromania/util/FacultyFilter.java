package com.app.studentromania.util;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.couchbase.client.java.document.json.JsonArray;

public class FacultyFilter {

	private static final int DEFAULT_LIMIT = 999999;
	private static final String DEFAULT_SORT_FIELD = "facultyName";
	private static final String DEFAULT_SORT_FIELD_JOIN = "f.facultyName";
	private static final String DEFAULT_SORT_DIRECTION = "asc";
	private static final String SORT_SPLIT_CHARACTER = ",";

	private String searchBy;
	private List<String> universityIds;
	private List<String> facultyCities;
	private List<String> facultyDomains;
	private List<String> admissionType;
	private Double ratingFrom;
	private String orderBy;
	private Integer limit;
	private Integer pageNumber;
	private Integer offset;
	private boolean join;

	public FacultyFilter(String searchBy, List<String> universityIds, List<String> facultyCities,
			List<String> facultyDomains, List<String> admissionType, Double ratingFrom, String orderBy, Integer limit,
			Integer pageNumber, Integer offset) {
		super();
		this.searchBy = searchBy;
		this.universityIds = universityIds;
		this.facultyCities = facultyCities;
		this.facultyDomains = facultyDomains;
		this.admissionType = admissionType;
		this.ratingFrom = ratingFrom;
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

	public List<String> getUniversityIds() {
		return universityIds;
	}

	public void setUniversityIds(List<String> universityIds) {
		this.universityIds = universityIds;
	}

	public List<String> getFacultyCites() {
		return facultyCities;
	}

	public void setFacultyCity(List<String> facultyCities) {
		this.facultyCities = facultyCities;
	}

	public List<String> getFacultyDomains() {
		return facultyDomains;
	}

	public void setFacultyDomains(List<String> facultyDomains) {
		this.facultyDomains = facultyDomains;
	}

	public List<String> getAdmissionType() {
		return admissionType;
	}

	public void setAdmissionType(List<String> admissionType) {
		this.admissionType = admissionType;
	}

	public Double getRatingFrom() {
		return ratingFrom;
	}

	public void setRatingFrom(Double ratingFrom) {
		this.ratingFrom = ratingFrom;
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

	public boolean isJoin() {
		return join;
	}

	public void setJoin(boolean join) {
		this.join = join;
	}

	public Integer getFirstResult() {
		if (this.pageNumber > 0) {
			return ((this.pageNumber - 1) * this.limit);
		}
		return 0;
	}

//	public String getSearchByClause() {
//		if (StringUtils.isEmpty(searchBy)) {
//			return StringUtils.EMPTY;
//		}
//		if (isJoin()) {
//			return " and (UPPER(f.facultyName) LIKE '%" + searchBy.toUpperCase()
//					+ "%' or UPPER(f.facultyShortname) LIKE '%" + searchBy.toUpperCase()
//					+ "%' or UPPER(f.universityName) LIKE '%" + searchBy.toUpperCase()
//					+ "%' or UPPER(f.facultyCity) LIKE '%" + searchBy.toUpperCase() + "%')";
//		}
//		return " and (UPPER(facultyName) LIKE '%" + searchBy.toUpperCase() + "%' or UPPER(facultyShortname) LIKE '%"
//				+ searchBy.toUpperCase() + "%' or UPPER(universityName) LIKE '%" + searchBy.toUpperCase() + "%' "
//				+ " or UPPER(facultyCity) LIKE '%" + searchBy.toUpperCase() + "%') ";
//	}

	public String getSearchByClause() {
		if (StringUtils.isEmpty(searchBy)) {
			return StringUtils.EMPTY;
		}
		searchBy = searchBy.replaceAll("[ĂăÂâ]", "A").replaceAll("[ŞşȘș]", "S").replaceAll("[ŢţȚț]", "T");
		if (isJoin()) {
			return " and (UPPER(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(f.facultyName, '[ĂăÂâ]', 'A'), '[ŞşȘș]', 'S'), '[ŢţȚț]', 'T')) LIKE '%"
					+ searchBy.toUpperCase()
					+ "%' or UPPER(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(f.universityName, '[ĂăÂâ]', 'A'), '[ŞşȘș]', 'S'), '[ŢţȚț]', 'T')) LIKE '%"
					+ searchBy.toUpperCase() + "%' "
					+ " or UPPER(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(f.facultyCity, '[ĂăÂâ]', 'A'), '[ŞşȘș]', 'S'), '[ŢţȚț]', 'T')) LIKE '%"
					+ searchBy.toUpperCase() + "%') ";
		}
		return " and (UPPER(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(facultyName, '[ĂăÂâ]', 'A'), '[ŞşȘș]', 'S'), '[ŢţȚț]', 'T')) LIKE '%"
				+ searchBy.toUpperCase()
				+ "%' or UPPER(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(universityName, '[ĂăÂâ]', 'A'), '[ŞşȘș]', 'S'), '[ŢţȚț]', 'T')) LIKE '%"
				+ searchBy.toUpperCase() + "%' "
				+ " or UPPER(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(facultyCity, '[ĂăÂâ]', 'A'), '[ŞşȘș]', 'S'), '[ŢţȚț]', 'T')) LIKE '%"
				+ searchBy.toUpperCase() + "%') ";
	}

	public String getUniversityIdsClause() {
		if (CollectionUtils.isEmpty(universityIds)) {
			return StringUtils.EMPTY;
		}
		if (isJoin()) {
			return " and f.universityId IN " + JsonArray.from(universityIds);
		}
		return " and universityId IN " + JsonArray.from(universityIds);
	}

	public String getFacultyCitiesClause() {
		if (CollectionUtils.isEmpty(facultyCities)) {
			return StringUtils.EMPTY;
		}
		if (isJoin()) {
			return " and f.facultyCity IN " + JsonArray.from(facultyCities);
		}
		return " and facultyCity IN " + JsonArray.from(facultyCities);
	}

	public String getFacultyDomainsClause() {
		if (CollectionUtils.isEmpty(facultyDomains)) {
			return StringUtils.EMPTY;
		}
		if (isJoin()) {
			return " and ARRAY_INTERSECT(f.facultyDomainsLicense, " + JsonArray.from(facultyDomains)
					+ ") OR ARRAY_INTERSECT(f.facultyDomainsMaster, " + JsonArray.from(facultyDomains) + ")) ";
		}
		return " and (ARRAY_INTERSECT(facultyDomainsLicense, " + JsonArray.from(facultyDomains)
				+ ") OR ARRAY_INTERSECT(facultyDomainsMaster, " + JsonArray.from(facultyDomains) + ")) ";
	}

	public String getAdmissionTypeClause() {
		if (CollectionUtils.isEmpty(admissionType)) {
			return StringUtils.EMPTY;
		}
		if (isJoin()) {
			return " and f.admissionType IN " + JsonArray.from(admissionType);
		}
		return " and admissionType IN " + JsonArray.from(admissionType);
	}

	public String getRatingFromClause() {
		if (ratingFrom == null || ratingFrom == 0) {
			return StringUtils.EMPTY;
		}
		if (isJoin()) {
			return " and f.avgRating >= " + ratingFrom;
		}
		return " and avgRating >= " + ratingFrom;
	}

	public String getOrderByClause() {
		if (StringUtils.isEmpty(orderBy)) {
			if (isJoin()) {
				return " order by " + DEFAULT_SORT_FIELD_JOIN + " " + DEFAULT_SORT_DIRECTION;
			}
			return " order by " + DEFAULT_SORT_FIELD + " " + DEFAULT_SORT_DIRECTION;
		}
		String[] sortInfo = orderBy.split(SORT_SPLIT_CHARACTER);
		return " order by " + sortInfo[0] + " " + sortInfo[1];
	}

	@Override
	public String toString() {
		return "FacultyFilter [searchBy=" + searchBy + ", universityIds=" + universityIds + ", facultyCities="
				+ facultyCities + ", facultyDomains=" + facultyDomains + ", admissionType=" + admissionType
				+ ", ratingFrom=" + ratingFrom + ", orderBy=" + orderBy + ", limit=" + limit + ", pageNumber="
				+ pageNumber + ", offset=" + offset + ", join=" + join + "]";
	}

}
