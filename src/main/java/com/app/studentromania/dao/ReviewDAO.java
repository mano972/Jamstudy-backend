package com.app.studentromania.dao;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.Review;
import com.app.studentromania.repo.ReviewRepo;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.ReviewFilter;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

@Component
public class ReviewDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReviewDAO.class);

	@Autowired
	private ReviewRepo reviewRepo;

	@Autowired
	private LogUtils logUtils;

	public List<Review> getAllReviews(ReviewFilter reviewFilter) {
		return reviewRepo.findAll(reviewFilter);
	}

	public Optional<Review> getByReviewId(String reviewId) {
		return reviewRepo.findByReviewId(reviewId).stream().findFirst();
	}

	public Optional<Review> getByReviewIdNoLog(String reviewId) {
		return reviewRepo.findByReviewIdNoLog(reviewId).stream().findFirst();
	}

	public List<Review> getFilteredReviewsByFacultyId(String facultyId, ReviewFilter reviewFilter) {
		return reviewRepo.getFilteredReviewsByFacultyId(facultyId, reviewFilter);
	}

	public List<Review> getReviewsByFacultyId(String facultyId) {
		return reviewRepo.getReviewsByFacultyId(facultyId);
	}

	public List<Review> getReviewsByUserId(String userId) {
		return reviewRepo.getReviewsByUserId(userId);
	}

	public List<Review> getReviewsByReviewDate(Date reviewDate) {
		return reviewRepo.getReviewsByReviewDate(reviewDate);
	}

	public long countFilteredReviewsByFacultyId(String facultyId, ReviewFilter reviewFilter) {
		return reviewRepo.countFilteredReviewsByFacultyId(facultyId, reviewFilter);
	}

	public long countFilteredReviews(ReviewFilter reviewFilter) {
		return reviewRepo.countFilteredReviews(reviewFilter);
	}

	public void updateReviewsDetailsForFaculty(Faculty faculty) {

		N1qlQueryResult result = reviewRepo.getReviewsDetailsByFacultyId(faculty.getFacultyId());
		JsonObject jsonObject = result.allRows().get(0).value();

		if (jsonObject.containsKey("countResults") && jsonObject.getLong("countResults") != null) {
			long countRev = jsonObject.getLong("countResults");
			faculty.setCountRev((int) countRev);
		}
		if (jsonObject.containsKey("avgRating")) {
			Double avgRating = jsonObject.getDouble("avgRating");
			faculty.setAvgRating(avgRating);
		}
		if (jsonObject.containsKey("avgDifficulty")) {
			Double avgDifficulty = jsonObject.getDouble("avgDifficulty");
			faculty.setAvgDifficulty(avgDifficulty);
		}
		if (jsonObject.containsKey("avgRatingJobProspects")) {
			Double avgRatingJobProspects = jsonObject.getDouble("avgRatingJobProspects");
			faculty.setAvgRatingJobProspects(avgRatingJobProspects);
		}
		if (jsonObject.containsKey("avgRatingCoursesAndLecturers")) {
			Double avgRatingCoursesAndLecturers = jsonObject.getDouble("avgRatingCoursesAndLecturers");
			faculty.setAvgRatingCoursesAndLecturers(avgRatingCoursesAndLecturers);
		}
		if (jsonObject.containsKey("avgRatingStudentOrganisations")) {
			Double avgRatingStudentOrganisations = jsonObject.getDouble("avgRatingStudentOrganisations");
			faculty.setAvgRatingStudentOrganisations(avgRatingStudentOrganisations);
		}
		if (jsonObject.containsKey("avgRatingAccomodation")) {
			Double avgRatingAccomodation = jsonObject.getDouble("avgRatingAccomodation");
			faculty.setAvgRatingAccomodation(avgRatingAccomodation);
		}
		if (jsonObject.containsKey("avgRatingFacultyFacilities")) {
			Double avgRatingFacultyFacilities = jsonObject.getDouble("avgRatingFacultyFacilities");
			faculty.setAvgRatingFacultyFacilities(avgRatingFacultyFacilities);
		}
		if (jsonObject.containsKey("avgRatingStudentSupport")) {
			Double avgRatingStudentSupport = jsonObject.getDouble("avgRatingStudentSupport");
			faculty.setAvgRatingStudentSupport(avgRatingStudentSupport);
		}
		if (jsonObject.containsKey("avgRatingTimeBalance")) {
			Double avgRatingTimeBalance = jsonObject.getDouble("avgRatingTimeBalance");
			faculty.setAvgRatingTimeBalance(avgRatingTimeBalance);
		}

		Long percentageWouldRecommend = getWouldRecommendPercentage(faculty.getFacultyId());
		Integer percentageWouldRecommendInt = percentageWouldRecommend != null ? percentageWouldRecommend.intValue()
				: null;
		faculty.setPercentageWouldRecommend(percentageWouldRecommendInt);

		faculty.setCountRev1Star(countByGeneralRating(faculty.getFacultyId(), 1).intValue());
		faculty.setCountRev2Stars(countByGeneralRating(faculty.getFacultyId(), 2).intValue());
		faculty.setCountRev3Stars(countByGeneralRating(faculty.getFacultyId(), 3).intValue());
		faculty.setCountRev4Stars(countByGeneralRating(faculty.getFacultyId(), 4).intValue());
		faculty.setCountRev5Stars(countByGeneralRating(faculty.getFacultyId(), 5).intValue());

	}

	private Long getWouldRecommendPercentage(String facultyId) {
		N1qlQueryResult result = reviewRepo.getWouldRecommendPercentage(facultyId);

		JsonObject jsonObject = result.allRows().get(0).value();
		Long percentage = null;
		if (jsonObject.containsKey("percentage") && jsonObject.getLong("percentage") != null) {
			percentage = result.allRows().get(0).value().getLong("percentage");
		}

		return percentage;
	}

	public long countByFacultyId(String facultyId) {
		return reviewRepo.countByFacultyId(facultyId);
	}

	public Long countByGeneralRating(String facultyId, int generalRating) {
		return reviewRepo.countByGeneralRating(facultyId, generalRating);
	}

	public void createReview(Review review) {
		reviewRepo.save(review);
		logUtils.logMessage(LOGGER,
				"Review " + review.getReviewId() + " was created for faculty " + review.getFacultyId());
	}

	public void updateReview(Review review) {
		review.setUpdateDateWithCurrentDate();
		reviewRepo.save(review);
		logUtils.logMessage(LOGGER, "Review " + review.getReviewId() + " was updated!");
	}

	/** Atomically bump a review counter (e.g. upvotes/reports) without rewriting the whole document. */
	public long adjustReviewCounter(String docId, String field, long delta) {
		return reviewRepo.adjustCounter(docId, field, delta);
	}

	public void deleteReview(Review review) {
		reviewRepo.delete(review);
		logUtils.logMessage(LOGGER, "Review " + review.getReviewId() + " was deleted!");
	}

	public void deleteAllReviews() {
		reviewRepo.deleteAll();
	}

	/**
	 * facultyId -> date of its most recent review. Faculties with no reviews are
	 * absent from the map. Used by the sitemap; a query failure yields an empty
	 * map so the sitemap still renders (falling back to the static lastmod).
	 */
	public Map<String, Date> getLatestReviewDateByFaculty() {
		Map<String, Date> out = new HashMap<>();
		try {
			N1qlQueryResult result = reviewRepo.getLatestReviewDateByFaculty();
			for (N1qlQueryRow row : result.allRows()) {
				JsonObject value = row.value();
				String facultyId = value.getString("facultyId");
				if (facultyId == null || value.get("lastReviewDate") == null) {
					continue;
				}
				Date date = toDate(value.get("lastReviewDate"));
				if (date != null) {
					out.put(facultyId, date);
				}
			}
		} catch (RuntimeException e) {
			logUtils.logMessage(LOGGER, "getLatestReviewDateByFaculty failed; sitemap falls back to static lastmod: "
					+ e.getMessage());
		}
		return out;
	}

	/** reviewDate comes back as epoch millis (Spring Data stores Date as a long); tolerate ISO strings too. */
	private static Date toDate(Object raw) {
		if (raw instanceof Number) {
			return new Date(((Number) raw).longValue());
		}
		if (raw instanceof String) {
			String s = ((String) raw).trim();
			try {
				return new Date(Long.parseLong(s));
			} catch (NumberFormatException ignored) {
				// not epoch millis
			}
			try {
				return Date.from(Instant.parse(s));
			} catch (RuntimeException ignored) {
				// not an ISO instant
			}
			try {
				return Date.from(OffsetDateTime.parse(s).toInstant());
			} catch (RuntimeException ignored) {
				// give up
			}
		}
		return null;
	}

}
