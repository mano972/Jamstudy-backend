package com.app.studentromania.dao;

import java.util.List;
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

@Component
public class ReviewDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReviewDAO.class);

	@Autowired
	private ReviewRepo reviewRepo;

	public List<Review> getAllReviews() {
		return reviewRepo.findAll();
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
		LogUtils.logMessage(LOGGER,
				"Review " + review.getReviewId() + " was created for faculty " + review.getFacultyId());
	}

	public void updateReview(Review review) {
		review.setUpdateDateWithCurrentDate();
		reviewRepo.save(review);
		LogUtils.logMessage(LOGGER, "Review " + review.getReviewId() + " was updated!");
	}

	public void deleteAllReviews() {
		reviewRepo.deleteAll();
	}

}
