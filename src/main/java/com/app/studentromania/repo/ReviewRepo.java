package com.app.studentromania.repo;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.stereotype.Repository;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.model.Review;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.ReviewFilter;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;

@Repository
public class ReviewRepo {

	@Autowired
	private CouchbaseTemplate template;

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Review> findAll() {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue())),
				Review.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Review> findAll(ReviewFilter reviewFilter) {
		if (StringUtils.isNotEmpty(reviewFilter.getSearchBy())) {
			return findAllWithFacultySearch(reviewFilter);
		}

		reviewFilter.setJoin(false);
		String selectStatement = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";

		String paginationStatement = " offset $2 limit $3 ";
		String query = selectStatement + getFilteredReviewsStatement(reviewFilter) + reviewFilter.getOrderByClause()
				+ paginationStatement;

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(),
						reviewFilter.getOffset(), reviewFilter.getLimit())),
				Review.class);
	}

	// Joins in the faculty documents so a keyword search on the all-faculties review
	// list can also match facultyName/universityName/facultyShortname/facultyCity,
	// not just reviewText. Only worth the join when there's actually a keyword.
	private List<Review> findAllWithFacultySearch(ReviewFilter reviewFilter) {
		reviewFilter.setJoin(true);
		String selectStatement = "SELECT r.*, meta(r).cas AS _CAS, meta(r).id AS _ID FROM " + Constants.BUCKET
				+ " AS r JOIN " + Constants.BUCKET + " AS f ON r.facultyId = f.facultyId AND f.docType = $2"
				+ " WHERE r.docType = $1 ";

		String paginationStatement = " offset $3 limit $4 ";
		String query = selectStatement + getFilteredReviewsStatement(reviewFilter) + reviewFilter.getOrderByClause()
				+ paginationStatement;

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(),
						DocTypeEnum.FACULTY.getValue(), reviewFilter.getOffset(), reviewFilter.getLimit())),
				Review.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Review> findByReviewId(String reviewId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND reviewId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), reviewId)), Review.class);
	}

	@Query
	public List<Review> findByReviewIdNoLog(String reviewId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND reviewId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), reviewId)), Review.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Review> getFilteredReviewsByFacultyId(String facultyId, ReviewFilter reviewFilter) {
		String selectStatement = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND facultyId = $2 ";

		String paginationStatement = " offset $3 limit $4 ";
		String query = selectStatement + getFilteredReviewsStatement(reviewFilter) + reviewFilter.getOrderByClause()
				+ paginationStatement;

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(),
				facultyId, reviewFilter.getOffset(), reviewFilter.getLimit())), Review.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public long countFilteredReviewsByFacultyId(String facultyId, ReviewFilter reviewFilter) {
		String selectStatement = "SELECT count(*) as countResults FROM " + Constants.BUCKET
				+ " WHERE docType = $1 AND facultyId = $2 ";
		String query = selectStatement + getFilteredReviewsStatement(reviewFilter);

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), facultyId)));
		long count = result.allRows().get(0).value().getLong("countResults");

		return count;
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public long countFilteredReviews(ReviewFilter reviewFilter) {
		if (StringUtils.isNotEmpty(reviewFilter.getSearchBy())) {
			return countFilteredReviewsWithFacultySearch(reviewFilter);
		}

		reviewFilter.setJoin(false);
		String selectStatement = "SELECT count(*) as countResults FROM " + Constants.BUCKET
				+ " WHERE docType = $1 ";
		String query = selectStatement + getFilteredReviewsStatement(reviewFilter);

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue())));
		long count = result.allRows().get(0).value().getLong("countResults");

		return count;
	}

	private long countFilteredReviewsWithFacultySearch(ReviewFilter reviewFilter) {
		reviewFilter.setJoin(true);
		String selectStatement = "SELECT count(*) as countResults FROM " + Constants.BUCKET + " AS r JOIN "
				+ Constants.BUCKET + " AS f ON r.facultyId = f.facultyId AND f.docType = $2 WHERE r.docType = $1 ";
		String query = selectStatement + getFilteredReviewsStatement(reviewFilter);

		N1qlQueryResult result = template.queryN1QL(N1qlQuery.parameterized(query,
				JsonArray.from(DocTypeEnum.REVIEW.getValue(), DocTypeEnum.FACULTY.getValue())));
		long count = result.allRows().get(0).value().getLong("countResults");

		return count;
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Review> getReviewsByFacultyId(String facultyId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND facultyId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), facultyId)), Review.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Review> getReviewsByUserId(String userId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND userId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), userId)), Review.class);
	}
	
	@Query
	@LogExecutionTime
	@LogParameters
	public List<Review> getReviewsByReviewDate(Date reviewDate) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND reviewDate >= $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query,
						JsonArray.from(DocTypeEnum.REVIEW.getValue(), reviewDate)), Review.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public N1qlQueryResult getReviewsDetailsByFacultyId(String facultyId) {
		String query = "SELECT count(*) as countResults, avg(generalRating) as avgRating, "
				+ " avg(jobProspectsReview.generalRating) as avgRatingJobProspects, "
				+ " avg(coursesAndLecturersReview.generalRating) as avgRatingCoursesAndLecturers, "
				+ " avg(studentOrganisationsReview.generalRating) as avgRatingStudentOrganisations, "
				+ " avg(accomodationReview.generalRating) as avgRatingAccomodation, "
				+ " avg(facultyFacilitiesReview.generalRating) as avgRatingFacultyFacilities, "
				+ " avg(studentSupportReview.generalRating) as avgRatingStudentSupport, "
				+ " avg(timeBalanceReview.generalRating) as avgRatingTimeBalance, "
				+ " avg(difficulty) as avgDifficulty FROM " + Constants.BUCKET
				+ " WHERE docType = $1 and facultyId = $2";

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), facultyId)));

		return result;
	}

	@LogExecutionTime
	@LogParameters
	public N1qlQueryResult getWouldRecommendPercentage(String facultyId) {
		String query = "SELECT (count(CASE WHEN wouldRecommend = true THEN 1 END) / count(*)) * 100 as percentage "
				+ "FROM " + Constants.BUCKET + " WHERE docType = $1 and facultyId = $2 and wouldRecommend is not null";

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), facultyId)));

		return result;
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public long countByFacultyId(String facultyId) {
		String query = "SELECT count(*) as countResults FROM " + Constants.BUCKET
				+ " WHERE docType = $1 AND facultyId = $2 ";

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.REVIEW.getValue(), facultyId)));
		long count = result.allRows().get(0).value().getLong("countResults");

		return count;
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public long countByGeneralRating(String facultyId, int generalRating) {
		String query = "SELECT count(*) as countResults FROM " + Constants.BUCKET
				+ " WHERE docType = $1 AND facultyId = $2 AND generalRating = $3 ";

		N1qlQueryResult result = template.queryN1QL(N1qlQuery.parameterized(query,
				JsonArray.from(DocTypeEnum.REVIEW.getValue(), facultyId, generalRating)));
		long count = result.allRows().get(0).value().getLong("countResults");

		return count;
	}

	public void save(Review review) {
		template.save(review);
	}

	@LogExecutionTime
	public void delete(Review review) {
		template.remove(review);
	}

	@LogExecutionTime
	public void deleteAll() {
		template.remove(findAll());
	}

	private String getFilteredReviewsStatement(ReviewFilter reviewFilter) {
		String filteredStatement = reviewFilter.getSearchByClause() + reviewFilter.getRatingFromClause()
				+ reviewFilter.getRatingToClause() + reviewFilter.getCountryCodeClause();
		return filteredStatement;
	}

}
