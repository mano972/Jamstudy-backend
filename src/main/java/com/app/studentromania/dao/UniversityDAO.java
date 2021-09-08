package com.app.studentromania.dao;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.model.University;
import com.app.studentromania.repo.UniversityRepo;
import com.app.studentromania.util.LogUtils;

@Component
public class UniversityDAO {

	@Autowired
	private LogUtils logUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(UniversityDAO.class);

	@Autowired
	private UniversityRepo universityRepo;

	public List<University> getAllUniversities() {
		return universityRepo.findAll();
	}

	public Optional<University> getByUniversityId(String universityId) {
		return universityRepo.findByUniversityId(universityId).stream().findFirst();
	}

	public void createUniversity(University university) {
		universityRepo.save(university);
		logUtils.logMessage(LOGGER, "University " + university.getUniversityId() + " was created!");
	}

	public void updateUniversity(University university) {
		university.setUpdateDateWithCurrentDate();
		universityRepo.save(university);
		logUtils.logMessage(LOGGER, "University " + university.getUniversityId() + " was updated!");
	}

	public void deleteAllUniversities() {
		universityRepo.deleteAll();
	}

}
