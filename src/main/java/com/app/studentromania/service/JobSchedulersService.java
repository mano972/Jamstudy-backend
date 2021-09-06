package com.app.studentromania.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.app.studentromania.util.LogUtils;

@Service
public class JobSchedulersService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulersService.class);

	@Autowired
	private FacultyService facultyService;

	@Scheduled(fixedDelay = 1_800_000)
	public void facultyReviewDetailsUpdateScheduler() {
		LogUtils.logStart(LOGGER, "facultyReviewDetailsUpdateScheduler", "scheduler");
		facultyService.updateAllFacultiesReviewDetails();
		LogUtils.logSuccess(LOGGER, "facultyReviewDetailsUpdateScheduler", "scheduler");
	}

}
