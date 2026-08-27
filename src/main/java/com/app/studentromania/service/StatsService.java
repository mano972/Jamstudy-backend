package com.app.studentromania.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dao.ReviewDAO;
import com.app.studentromania.dao.UniversityDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.util.FacultyFilter;
import com.app.studentromania.util.ReviewFilter;

@Service
public class StatsService {

	@Autowired
	private UniversityDAO universityDAO;

	@Autowired
	private FacultyDAO facultyDAO;

	@Autowired
	private ReviewDAO reviewDAO;

	@Autowired
	private CustomRequestContext customRequestContext;

	/**
	 * The three totals behind the homepage stats bar, in a single call instead of the
	 * three the page used to fire. Nothing new is counted: universities is the full
	 * university list size (not country-scoped, exactly like {@code GET /university}),
	 * and faculties/reviews reuse the same country-scoped {@code countFiltered...}
	 * queries that back {@code GET /faculty} and {@code GET /review}.
	 */
	public ResponseDTO getHomepageStats() {
		String countryCode = customRequestContext.getCountryCode();

		long universities = universityDAO.getAllUniversities().size();
		long faculties = facultyDAO.countFilteredFaculties(emptyFacultyFilter(countryCode));
		long reviews = reviewDAO.countFilteredReviews(emptyReviewFilter(countryCode));

		JSONObject response = new JSONObject();
		response.put("universities", universities);
		response.put("faculties", faculties);
		response.put("reviews", reviews);

		return ResponseDTO.createSuccessResponse(response);
	}

	// An otherwise-empty filter carrying only the country code — the count queries
	// null-guard every other clause, so this matches an unfiltered listing call.
	private FacultyFilter emptyFacultyFilter(String countryCode) {
		return new FacultyFilter(null, null, null, null, null, null, null, null, countryCode, null, null, null, null);
	}

	private ReviewFilter emptyReviewFilter(String countryCode) {
		return new ReviewFilter(null, null, null, countryCode, null, null, null, null, null);
	}

}
