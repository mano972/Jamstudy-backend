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
	
	@Autowired
	private LogUtils logUtils;

	@Scheduled(fixedDelay = 1_800_000)
	public void facultyReviewDetailsUpdateScheduler() {
		logUtils.logStart(LOGGER, "facultyReviewDetailsUpdateScheduler");
		facultyService.updateAllFacultiesReviewDetails();
		logUtils.logSuccess(LOGGER, "facultyReviewDetailsUpdateScheduler");
	}

}
