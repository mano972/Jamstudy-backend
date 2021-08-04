package com.app.studentromania.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.stereotype.Repository;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.FacultyFilter;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;

@Repository
public class FacultyRepo {

	@Autowired
	private CouchbaseTemplate template;

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Faculty> findAll() {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.FACULTY.getValue())),
				Faculty.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Faculty> findByFacultyId(String facultyId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND facultyId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.FACULTY.getValue(), facultyId)),
				Faculty.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Faculty> findByUniversityId(String universityId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET
				+ " WHERE docType = $1 AND universityId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.FACULTY.getValue(), universityId)),
				Faculty.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Faculty> getFilteredFaculties(FacultyFilter facultyFilter) {
		facultyFilter.setJoin(false);
		String selectStatement = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";
		String paginationStatement = " offset $2 limit $3 ";
		String query = selectStatement + getFilteredFacultiesStatement(facultyFilter) + facultyFilter.getOrderByClause()
				+ paginationStatement;

		return template.findByN1QL(N1qlQuery.parameterized(query,
				JsonArray.from(DocTypeEnum.FACULTY.getValue(), facultyFilter.getOffset(), facultyFilter.getLimit())),
				Faculty.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public N1qlQueryResult getUniversitiesForFilter(FacultyFilter facultyFilter) {
		facultyFilter.setJoin(false);
		String selectStatement = "SELECT count(*) AS countFac, universityId, universityName FROM " + Constants.BUCKET
				+ " WHERE docType = $1  ";
		String groupByStatementUniv = " GROUP BY universityId, universityName ";
		String limitStatement = " limit $2 ";
		String query = selectStatement + getFilteredFacultiesStatementForUniversitiesForFilter(facultyFilter)
				+ groupByStatementUniv + facultyFilter.getOrderByClause() + limitStatement;

		N1qlQueryResult result = template.queryN1QL(N1qlQuery.parameterized(query,
				JsonArray.from(DocTypeEnum.FACULTY.getValue(), facultyFilter.getLimit())));

		return result;
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public N1qlQueryResult getFacultyCitiesForFilter(FacultyFilter facultyFilter) {
		facultyFilter.setJoin(false);
		String selectStatement = "SELECT count(*) AS countFac, facultyCity FROM " + Constants.BUCKET
				+ " WHERE docType = $1 ";
		String groupByStatementCities = " GROUP BY facultyCity ";
		String limitStatement = " limit $2 ";
		String query = selectStatement + getFilteredFacultiesStatementForCitiesForFilter(facultyFilter)
				+ groupByStatementCities + facultyFilter.getOrderByClause() + limitStatement;

		N1qlQueryResult result = template.queryN1QL(N1qlQuery.parameterized(query,
				JsonArray.from(DocTypeEnum.FACULTY.getValue(), facultyFilter.getLimit())));

		return result;
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public long countFilteredFaculties(FacultyFilter facultyFilter) {
		facultyFilter.setJoin(false);
		String selectStatement = "SELECT count(*) as countResults FROM " + Constants.BUCKET + " WHERE docType = $1 ";
		String query = selectStatement + getFilteredFacultiesStatement(facultyFilter);

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.FACULTY.getValue())));
		long count = result.allRows().get(0).value().getLong("countResults");

		return count;
	}

	public void save(Faculty faculty) {
		template.save(faculty);
	}

	@LogExecutionTime
	public void deleteAll() {
		template.remove(findAll());
	}

	private String getFilteredFacultiesStatement(FacultyFilter facultyFilter) {
		String filteredStatement = facultyFilter.getSearchByClause() + facultyFilter.getFacultyIdsClause()
				+ facultyFilter.getUniversityIdsClause() + facultyFilter.getFacultyCitiesClause()
				+ facultyFilter.getFacultyDomainsClause() + facultyFilter.getAdmissionTypeClause()
				+ facultyFilter.getRatingFromClause() + facultyFilter.getCountRevFromClause();
		return filteredStatement;
	}

	private String getFilteredFacultiesStatementForUniversitiesForFilter(FacultyFilter facultyFilter) {
		String filteredStatement = facultyFilter.getSearchByClause() + facultyFilter.getFacultyCitiesClause()
				+ facultyFilter.getFacultyDomainsClause() + facultyFilter.getAdmissionTypeClause()
				+ facultyFilter.getRatingFromClause() + facultyFilter.getCountRevFromClause();
		return filteredStatement;
	}

	private String getFilteredFacultiesStatementForCitiesForFilter(FacultyFilter facultyFilter) {
		String filteredStatement = facultyFilter.getSearchByClause() + facultyFilter.getUniversityIdsClause()
				+ facultyFilter.getFacultyDomainsClause() + facultyFilter.getAdmissionTypeClause()
				+ facultyFilter.getRatingFromClause() + facultyFilter.getCountRevFromClause();
		return filteredStatement;
	}

	// not used
	@Query
	@LogExecutionTime
	@LogParameters
	public N1qlQueryResult getFilteredFacultiesV2(FacultyFilter facultyFilter) {
		facultyFilter.setJoin(true);
		String selectStatement = "SELECT f.facultyId, f.facultyName, f.facultyCity,"
				+ " f.universityId, f.universityName, f.facultyDomainsLicense, f.admissionType, avg(r.generalRating) as avgRating, "
				+ " count(r.generalRating) as countRev FROM " + Constants.BUCKET + " AS f LEFT JOIN " + Constants.BUCKET
				+ " AS r ON f.facultyId = r.facultyId AND r.docType = $1 WHERE f.docType = $2 ";
		String groupByStatement = " GROUP BY f.facultyId, f.facultyName, f.facultyCity, f.universityName, "
				+ "f.facultyDomains, f.admissionType, f.universityId ";
		String paginationStatement = " offset $3 limit $4 ";
		String query = selectStatement + getFilteredFacultiesStatement(facultyFilter) + groupByStatement
				+ facultyFilter.getOrderByClause() + paginationStatement;

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(),
						DocTypeEnum.FACULTY.getValue(), facultyFilter.getOffset(), facultyFilter.getLimit())));

		return result;
	}

	// not used
	@Query
	@LogExecutionTime
	@LogParameters
	public N1qlQueryResult getUniversitiesForFilterV2(FacultyFilter facultyFilter) {
		facultyFilter.setJoin(true);
		String selectStatement = "SELECT count(*) AS countFac, universityId, universityName FROM ( SELECT f.facultyId, f.facultyName, f.facultyCity,"
				+ " f.universityId, f.universityName, f.facultyDomains, f.admissionType, avg(r.generalRating) as avgRating, "
				+ " count(r.generalRating) as countRev FROM " + Constants.BUCKET + " AS f LEFT JOIN " + Constants.BUCKET
				+ " AS r ON f.facultyId = r.facultyId AND r.docType = $1 WHERE f.docType = $2 ";
		String groupByStatement = " GROUP BY f.facultyId, f.facultyName, f.facultyCity, f.universityName, "
				+ "f.facultyDomains, f.admissionType, f.universityId ) AS x ";
		String groupByStatementUniv = " GROUP BY universityId, universityName ";
		String limitStatement = " limit $3 ";
		String query = selectStatement + getFilteredFacultiesStatement(facultyFilter) + groupByStatement
				+ groupByStatementUniv + facultyFilter.getOrderByClause() + limitStatement;

		N1qlQueryResult result = template.queryN1QL(N1qlQuery.parameterized(query, JsonArray
				.from(DocTypeEnum.REVIEW.getValue(), DocTypeEnum.FACULTY.getValue(), facultyFilter.getLimit())));

		return result;
	}

	// not used
	@Query
	@LogExecutionTime
	@LogParameters
	public N1qlQueryResult getFacultyCitiesForFilterV2(FacultyFilter facultyFilter) {
		facultyFilter.setJoin(true);
		String selectStatement = "SELECT count(*) AS countFac, facultyCity FROM ( SELECT f.facultyId, f.facultyName, f.facultyCity,"
				+ " f.universityId, f.universityName, f.facultyDomains, f.admissionType, avg(r.generalRating) as avgRating, "
				+ " count(r.generalRating) as countRev FROM " + Constants.BUCKET + " AS f LEFT JOIN " + Constants.BUCKET
				+ " AS r ON f.facultyId = r.facultyId AND r.docType = $1 WHERE f.docType = $2 ";
		String groupByStatement = " GROUP BY f.facultyId, f.facultyName, f.facultyCity, f.universityName, "
				+ "f.facultyDomains, f.admissionType, f.universityId ) AS x ";
		String groupByStatementCities = " GROUP BY facultyCity ";
		String limitStatement = " limit $3 ";
		String query = selectStatement + getFilteredFacultiesStatement(facultyFilter) + groupByStatement
				+ groupByStatementCities + facultyFilter.getOrderByClause() + limitStatement;

		N1qlQueryResult result = template.queryN1QL(N1qlQuery.parameterized(query, JsonArray
				.from(DocTypeEnum.REVIEW.getValue(), DocTypeEnum.FACULTY.getValue(), facultyFilter.getLimit())));

		return result;
	}

}
