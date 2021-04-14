package com.app.studentromania.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dao.ReviewDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.dto.ReviewDTO;
import com.app.studentromania.dto.ReviewResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.Review;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.ReviewFilter;
import com.app.studentromania.util.Utilities;

@Service
public class ReviewService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReviewService.class);

	@Autowired
	private ReviewDAO reviewDAO;

	@Autowired
	private FacultyDAO facultyDAO;

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	public ReviewService() {
		LogUtils.logMessage(LOGGER, "ReviewService initialized");
	}

	public ResponseDTO getAllReviews() {
		List<Review> reviews = reviewDAO.getAllReviews();
		JSONArray reviewsArray = new JSONArray();
		reviews.forEach(review -> {
			ReviewResponseDTO reviewResponseDTO = mapToReviewResponseDTO(review);
			reviewsArray.put(new JSONObject(reviewResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("reviews", reviewsArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getByReviewId(String reviewId) {
		Optional<Review> reviewOpt = reviewDAO.getByReviewId(reviewId);
		if (!reviewOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.REVIEW_NOT_FOUND);
		}
		Review review = reviewOpt.get();
		ReviewResponseDTO reviewResponseDTO = mapToReviewResponseDTO(review);
		JSONObject response = new JSONObject(reviewResponseDTO);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getFilteredReviewsByFacultyId(String facultydId, ReviewFilter reviewFilters) {
		List<Review> reviews = reviewDAO.getFilteredReviewsByFacultyId(facultydId, reviewFilters);
		long count = reviewDAO.countFilteredReviews(facultydId, reviewFilters);
		JSONArray reviewsArray = new JSONArray();
		reviews.forEach(review -> {
			ReviewResponseDTO reviewResponseDTO = mapToReviewResponseDTO(review);
			reviewsArray.put(new JSONObject(reviewResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("reviews", reviewsArray);
		response.put("count", count);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO createReview(ReviewDTO reviewDTO) {
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(reviewDTO.getFacultyId());
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = facultyOpt.get();
		ErrorsEnum error = validateReviewCreate(reviewDTO);
		if (ErrorsEnum.NO_ERROR != error) {
			return ResponseDTO.createErrorResponse(error);
		}
		String generatedId = configDAO.generateDocumentId(Constants.REVIEW_PREFIX_ID);
		Review review = new Review();
		review.setReviewId(generatedId);
		Date currentDate = new Date();
		review.setReviewDate(currentDate);
		review.setFormattedReviewDate(Utilities.getFormatedDate(currentDate));
		mapToReview(reviewDTO, review);
		reviewDAO.createReview(review);
		ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO();
		reviewResponseDTO.setReviewId(review.getReviewId());
		reviewResponseDTO.setFacultyId(review.getFacultyId());

		// review document arrives late in database
		while (!reviewDAO.getByReviewIdNoLog(review.getReviewId()).isPresent()) {

		}

		// update faculty with new ratings
		updateFacultyReviewDetails(faculty);

		return ResponseDTO.createSuccessResponse(new JSONObject(reviewResponseDTO));
	}

	public ResponseDTO updateReview(ReviewDTO reviewDTO) {
		Optional<Review> reviewOpt = reviewDAO.getByReviewId(reviewDTO.getReviewId());
		if (!reviewOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.REVIEW_NOT_FOUND);
		}
		ErrorsEnum error = validateReviewUpdate(reviewDTO);
		if (ErrorsEnum.NO_ERROR != error) {
			return ResponseDTO.createErrorResponse(error);
		}
		Review review = reviewOpt.get();
		mapToReview(reviewDTO, review);
		reviewDAO.updateReview(review);
		ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO();
		reviewResponseDTO.setReviewId(review.getReviewId());
		reviewResponseDTO.setFacultyId(review.getFacultyId());

		// update faculty with new ratings
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(reviewDTO.getFacultyId());
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = facultyOpt.get();
		updateFacultyReviewDetails(faculty);

		return ResponseDTO.createSuccessResponse(new JSONObject(reviewResponseDTO));
	}

	public void updateFacultyReviewDetails(Faculty faculty) {
		LogUtils.logMessage(LOGGER,
				"Faculty " + faculty.getFacultyId() + " currently has the following review details. avgRating: "
						+ faculty.getAvgRating() + " countRev: " + faculty.getCountRev() + " avgDifficulty: "
						+ faculty.getAvgDifficulty() + " percentageWouldRecommend: "
						+ faculty.getPercentageWouldRecommend());

		reviewDAO.updateReviewsDetailsForFaculty(faculty);
		facultyDAO.updateFaculty(faculty);

		LogUtils.logMessage(LOGGER,
				"Faculty " + faculty.getFacultyId() + " was updated with the following review details. avgRating: "
						+ faculty.getAvgRating() + " countRev: " + faculty.getCountRev() + " avgDifficulty: "
						+ faculty.getAvgDifficulty() + " percentageWouldRecommend: "
						+ faculty.getPercentageWouldRecommend());
	}

	public ResponseDTO reportReview(String reviewId) {
		Optional<Review> reviewOpt = reviewDAO.getByReviewId(reviewId);
		if (!reviewOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.REVIEW_NOT_FOUND);
		}
		Review review = reviewOpt.get();
		int reports = review.getReports();
		review.setReports(++reports);
		LogUtils.logMessage(LOGGER, "Review " + review.getReviewId() + " was reported!");
		reviewDAO.updateReview(review);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO upvoteReview(ReviewDTO reviewDTO) {
		Optional<Review> reviewOpt = reviewDAO.getByReviewId(reviewDTO.getReviewId());
		if (!reviewOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.REVIEW_NOT_FOUND);
		}
		Review review = reviewOpt.get();
		int upvotes = review.getUpvotes();
		if (reviewDTO.getUpvote()) {
			review.setUpvotes(++upvotes);
			LogUtils.logMessage(LOGGER, "Review " + review.getReviewId() + " was upvoted!");
		} else {
			if (upvotes == 0) {
				review.setUpvotes(0);
			} else {
				review.setUpvotes(--upvotes);
			}
			LogUtils.logMessage(LOGGER, "Review " + review.getReviewId() + " was downvoted!");
		}
		reviewDAO.updateReview(review);
		ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO();
		reviewResponseDTO.setReviewId(review.getReviewId());
		reviewResponseDTO.setFacultyId(review.getFacultyId());
		reviewResponseDTO.setUpvotes(review.getUpvotes());

		return ResponseDTO.createSuccessResponse(new JSONObject(reviewResponseDTO));
	}

	public ResponseDTO deleteByReviewId(String reviewId) {
		Optional<Review> reviewOpt = reviewDAO.getByReviewId(reviewId);
		if (!reviewOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.REVIEW_NOT_FOUND);
		}
		Review review = reviewOpt.get();
		reviewDAO.deleteReview(review);

		// update faculty with new ratings
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(review.getFacultyId());
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = facultyOpt.get();
		updateFacultyReviewDetails(faculty);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO deleteByFacultyId(String facultyId) {
		List<Review> reviews = reviewDAO.getReviewsByFacultyId(facultyId);
		for (Review review : reviews) {
			reviewDAO.deleteReview(review);
		}

		// update faculty with new ratings
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = facultyOpt.get();
		updateFacultyReviewDetails(faculty);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO deleteAllReviews() {
		reviewDAO.deleteAllReviews();
		List<Faculty> faculties = facultyDAO.getAllFaculties();
		// update all faculties with new ratings
		for (Faculty faculty : faculties) {
			updateFacultyReviewDetails(faculty);
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	private ErrorsEnum validateReviewCreate(ReviewDTO reviewDTO) {
		if (reviewDTO.getGeneralRating() == null || reviewDTO.getGeneralRating() < 1
				|| reviewDTO.getGeneralRating() > 5) {
			return ErrorsEnum.REVIEW_GENERAL_RATING_ERROR;
		}
		if (reviewDTO.getDifficulty() != null) {
			if (reviewDTO.getDifficulty() < 1 || reviewDTO.getDifficulty() > 5) {
				return ErrorsEnum.REVIEW_DIFFICULTY_ERROR;
			}
		}
		return ErrorsEnum.NO_ERROR;
	}

	private ErrorsEnum validateReviewUpdate(ReviewDTO reviewDTO) {
		if (reviewDTO.getGeneralRating() != null) {
			if (reviewDTO.getGeneralRating() < 1 || reviewDTO.getGeneralRating() > 5) {
				return ErrorsEnum.REVIEW_GENERAL_RATING_ERROR;
			}
		}
		if (reviewDTO.getDifficulty() != null) {
			if (reviewDTO.getDifficulty() < 1 || reviewDTO.getDifficulty() > 5) {
				return ErrorsEnum.REVIEW_DIFFICULTY_ERROR;
			}
		}
		return ErrorsEnum.NO_ERROR;
	}

	private void mapToReview(ReviewDTO reviewDTO, Review review) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		mapper.map(reviewDTO, review);
	}

	private ReviewResponseDTO mapToReviewResponseDTO(Review review) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper.map(review, ReviewResponseDTO.class);
	}

//	private void updateNewFacultyRating(ReviewDTO reviewDTO) {
//		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(reviewDTO.getFacultyId());
//		if (!facultyOpt.isPresent()) {
//			// error
//		}
//		Faculty faculty = facultyOpt.get();
////		long noOfReviews = reviewDAO.countByFacultyId(faculty.getFacultyId());
//		int noOfReviews = faculty.getCountRev();
//		int newNoOfReviews = noOfReviews + 1;
//
//		double facultyRating = faculty.getAvgRating();
//		int reviewRating = reviewDTO.getGeneralRating();
//		double newFacultyRating = ((facultyRating * noOfReviews) + reviewRating) / newNoOfReviews;
//
//		double facultyDifficulty = faculty.getAvgDifficulty();
//		int difficulty = reviewDTO.getDifficulty();
//		double newFacultyDifficulty = ((facultyDifficulty * noOfReviews) + difficulty) / newNoOfReviews;
//
//		faculty.setAvgRating(newFacultyRating);
//		faculty.setAvgDifficulty(newFacultyDifficulty);
//		faculty.setCountRev(newNoOfReviews);
//		facultyDAO.saveFaculty(faculty);
//	}
//
//	private void updateNewFacultyRatingUpdatedReview(ReviewDTO reviewDTO, Review oldReview) {
//		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(reviewDTO.getFacultyId());
//		if (!facultyOpt.isPresent()) {
//			// error
//		}
//		Faculty faculty = facultyOpt.get();
////		long noOfReviews = reviewDAO.countByFacultyId(faculty.getFacultyId());
//		int noOfReviews = faculty.getCountRev();
//
//		double facultyRating = faculty.getAvgRating();
//		int reviewRating = reviewDTO.getGeneralRating();
//		int oldReviewRating = oldReview.getGeneralRating();
//		double newFacultyRating = (((facultyRating * noOfReviews) - oldReviewRating) + reviewRating) / noOfReviews;
//
//		double facultyDifficulty = faculty.getAvgDifficulty();
//		int difficulty = reviewDTO.getDifficulty();
//		int oldDifficulty = oldReview.getDifficulty();
//		double newFacultyDifficulty = (((facultyDifficulty * noOfReviews) - oldDifficulty) + difficulty) / noOfReviews;
//
//		faculty.setAvgRating(newFacultyRating);
//		faculty.setAvgDifficulty(newFacultyDifficulty);
//		facultyDAO.saveFaculty(faculty);
//	}

}
