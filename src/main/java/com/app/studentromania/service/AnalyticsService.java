package com.app.studentromania.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Analytics;
import com.app.studentromania.repo.AnalyticsRepo;
import com.app.studentromania.util.LogUtils;

@Service
public class AnalyticsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

	@Autowired
	public AnalyticsService() {
		LogUtils.logMessage(LOGGER, "StatisticService initialized");
	}

	@Autowired
	private AnalyticsRepo analyticsRepo;

	public ResponseDTO saveAnalytics() {
		Analytics analytics = new Analytics();
		List<Analytics> statistics = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(statistics)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANALYTICS_DOCUMENT_ALREADY_EXISTS);
		}
		analyticsRepo.save(analytics);
		LogUtils.logMessage(LOGGER, "Analytics document created!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO resetAnalytics() {
		List<Analytics> analytics = analyticsRepo.findAll();
		if (CollectionUtils.isEmpty(analytics)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANALYTICS_DOCUMENT_ALREADY_EXISTS);
		}
		Analytics statistic = analytics.get(0);
		statistic.setHomePage(0);
		statistic.setSearch(0);
		statistic.setReviewIntention(0);
		statistic.setReviewAdded(0);
		analyticsRepo.save(statistic);
		LogUtils.logMessage(LOGGER, "Analytics fields reset!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public void increaseHomePageStatistic() {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long homePageStatistic = analytics.getHomePage();
			homePageStatistic++;
			analytics.setHomePage(homePageStatistic);
			analyticsRepo.save(analytics);
			LogUtils.logMessage(LOGGER, "Analytics statistic 'homepage' increased");
		}
	}

	public void increaseSearchStatistic() {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long searchStatistic = analytics.getSearch();
			searchStatistic++;
			analytics.setSearch(searchStatistic);
			analyticsRepo.save(analytics);
			LogUtils.logMessage(LOGGER, "Analytics statistic 'search' increased");
		}
	}

	public void increaseReviewIntentionStatistic() {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long reviewIntentionStatistic = analytics.getReviewIntention();
			reviewIntentionStatistic++;
			analytics.setReviewIntention(reviewIntentionStatistic);
			analyticsRepo.save(analytics);
			LogUtils.logMessage(LOGGER, "Analytics statistic 'review intentions' increased");
		}
	}

	public void increaseReviewAddedStatistic() {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long reviewAdded = analytics.getReviewAdded();
			reviewAdded++;
			analytics.setReviewAdded(reviewAdded);
			analyticsRepo.save(analytics);
			LogUtils.logMessage(LOGGER, "Analytics statistic 'review added' increased");
		}
	}

	public ResponseDTO getAnalytics() {
		List<Analytics> statistics = analyticsRepo.findAll();
		if (CollectionUtils.isEmpty(statistics)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANALYTICS_NOT_FOUND);
		}
		Analytics analytics = statistics.get(0);
		JSONObject response = new JSONObject(analytics);

		return ResponseDTO.createSuccessResponse(response);
	}

}
