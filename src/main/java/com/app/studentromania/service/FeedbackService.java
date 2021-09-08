package com.app.studentromania.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dto.FeedbackEntryDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Feedback;
import com.app.studentromania.model.FeedbackEntry;
import com.app.studentromania.repo.FeedbackRepo;
import com.app.studentromania.util.LogUtils;

@Service
public class FeedbackService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackService.class);
	
	@Autowired
	private LogUtils logUtils;

	@Autowired
	public FeedbackService() {
//		logUtils.logMessage(LOGGER, "FeedbackService initialized");
	}

	@Autowired
	private FeedbackRepo feedbackRepo;

	public ResponseDTO saveFeedback() {
		Feedback feedback = new Feedback();
		List<Feedback> feedbacks = feedbackRepo.findAll();
		if (!CollectionUtils.isEmpty(feedbacks)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FEEDBACK_ALREADY_EXISTS);
		}
		feedbackRepo.save(feedback);
		logUtils.logMessage(LOGGER, "Feedback document created!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO resetFeedback() {
		List<Feedback> feedbacks = feedbackRepo.findAll();
		if (CollectionUtils.isEmpty(feedbacks)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FEEDBACK_DOCUMENT_NOT_FOUND);
		}
		Feedback feedback = feedbacks.get(0);
		feedback.getFeedbackEntries().clear();
		feedbackRepo.save(feedback);
		logUtils.logMessage(LOGGER, "Feedback entries reset!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO addFeedbackEntry(FeedbackEntryDTO feedbackEntryDTO) {
		List<Feedback> feedbacks = feedbackRepo.findAll();
		if (CollectionUtils.isEmpty(feedbacks)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FEEDBACK_DOCUMENT_NOT_FOUND);
		}
		Feedback feedback = feedbacks.get(0);
		List<FeedbackEntry> feedbackEntries = feedback.getFeedbackEntries();
		FeedbackEntry feedbackEntry = new FeedbackEntry();
		feedbackEntry.setName(feedbackEntryDTO.getName());
		feedbackEntry.setEmail(feedbackEntryDTO.getEmail());
		feedbackEntry.setMessage(feedbackEntryDTO.getMessage());
		feedbackEntries.add(feedbackEntry);
		feedback.setFeedbackEntries(feedbackEntries);
		feedbackRepo.save(feedback);
		logUtils.logMessage(LOGGER, "Feedback entry added to feedback document!");
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO getFeedback() {
		List<Feedback> feedbacks = feedbackRepo.findAll();
		if (CollectionUtils.isEmpty(feedbacks)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FEEDBACK_DOCUMENT_NOT_FOUND);
		}
		Feedback feedback = feedbacks.get(0);
		JSONObject response = new JSONObject(feedback);

		return ResponseDTO.createSuccessResponse(response);
	}

}
