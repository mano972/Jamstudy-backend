package com.app.studentromania.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.dto.CityFilterResponseDTO;
import com.app.studentromania.dto.UniversityFilterRowResponseDTO;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.repo.FacultyRepo;
import com.app.studentromania.util.FacultyFilter;
import com.app.studentromania.util.LogUtils;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

@Component
public class FacultyDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacultyDAO.class);

	@Autowired
	private FacultyRepo facultyRepo;

	public List<Faculty> getAllFaculties() {
		return facultyRepo.findAll();
	}

	public List<Faculty> getFilteredFaculties(FacultyFilter facultyFilter) {
		return facultyRepo.getFilteredFaculties(facultyFilter);
	}

	/*
	 * For each faculty search the universities list filter updates with relevant
	 * universities and the number of relevant containing faculties for each one.
	 */
	public List<UniversityFilterRowResponseDTO> getUniversitiesForFilter(FacultyFilter facultyFilter) {
		N1qlQueryResult result = facultyRepo.getUniversitiesForFilter(facultyFilter);
		List<UniversityFilterRowResponseDTO> listResponseDTOs = mapResultToUniversityFilterRows(result);

		return listResponseDTOs;
	}

	/*
	 * For each faculty search the cities list filter updates with relevant cities
	 * and the number of relevant containing faculties for each one.
	 */
	public List<CityFilterResponseDTO> getFacultyCitiesForFilter(FacultyFilter facultyFilter) {
		N1qlQueryResult result = facultyRepo.getFacultyCitiesForFilter(facultyFilter);
		List<CityFilterResponseDTO> listResponseDTOs = mapResultToCityFilterRows(result);

		return listResponseDTOs;
	}

	public long countFilteredFaculties(FacultyFilter facultyFilter) {
		return facultyRepo.countFilteredFaculties(facultyFilter);
	}

	public Optional<Faculty> getByFacultyId(String facultyId) {
		return facultyRepo.findByFacultyId(facultyId).stream().findFirst();
	}

	public List<Faculty> getByUniversityId(String universityId) {
		return facultyRepo.findByUniversityId(universityId);
	}

	public void createFaculty(Faculty faculty) {
		facultyRepo.save(faculty);
		LogUtils.logMessage(LOGGER, "Faculty " + faculty.getFacultyId() + " was created!");
	}

	public void updateFaculty(Faculty faculty) {
		faculty.setUpdateDateWithCurrentDate();
		facultyRepo.save(faculty);
		LogUtils.logMessage(LOGGER, "Faculty " + faculty.getFacultyId() + " was updated!");
	}

	public void deleteAllFaculties() {
		facultyRepo.deleteAll();
	}

	private List<Faculty> mapResultToFaculties(N1qlQueryResult result) {
		List<Faculty> faculties = new ArrayList<>();

		for (N1qlQueryRow row : result) {
			Faculty faculty = new Faculty();
			JsonObject jsonObject = row.value();
			if (jsonObject.containsKey("facultyId") && jsonObject.getString("facultyId") != null) {
				String facultyId = jsonObject.getString("facultyId");
				faculty.setFacultyId(facultyId);
			}
			if (jsonObject.containsKey("facultyName") && jsonObject.getString("facultyName") != null) {
				String facultyName = jsonObject.getString("facultyName");
				faculty.setFacultyName(facultyName);
			}
			if (jsonObject.containsKey("facultyCity") && jsonObject.getString("facultyCity") != null) {
				String facultyCity = jsonObject.getString("facultyCity");
				faculty.setFacultyCity(facultyCity);
			}
			if (jsonObject.containsKey("universityId") && jsonObject.getString("universityId") != null) {
				String universityId = jsonObject.getString("universityId");
				faculty.setUniversityId(universityId);
			}
			if (jsonObject.containsKey("universityName") && jsonObject.getString("universityName") != null) {
				String universityName = jsonObject.getString("universityName");
				faculty.setUniversityName(universityName);
			}
//			if (jsonObject.containsKey("admissionType") && jsonObject.getString("admissionType") != null) {
//				String admissionType = jsonObject.getString("admissionType");
//				faculty.setAdmissionType(admissionType);
//			}
			if (jsonObject.containsKey("facultyDomainsLicense") && jsonObject.get("facultyDomainsLicense") != null) {
				JsonArray jsonArray = (JsonArray) jsonObject.get("facultyDomainsLicense");
				for (int i = 0; i < jsonArray.size(); i++) {
					String facultyDomain = (String) jsonArray.get(i);
					faculty.getFacultyDomainsLicense().add(facultyDomain);
				}
			}
			if (jsonObject.containsKey("avgRating") && jsonObject.getDouble("avgRating") != null) {
				double avgRating = jsonObject.getDouble("avgRating");
				faculty.setAvgRating(avgRating);
			}
			if (jsonObject.containsKey("countRev") && jsonObject.getInt("countRev") != null) {
				int countRev = jsonObject.getInt("countRev");
				faculty.setCountRev(countRev);
			}
			faculties.add(faculty);
		}

		return faculties;
	}

	private List<UniversityFilterRowResponseDTO> mapResultToUniversityFilterRows(N1qlQueryResult result) {
		List<UniversityFilterRowResponseDTO> listResponseDTOs = new ArrayList<>();

		for (N1qlQueryRow row : result) {
			UniversityFilterRowResponseDTO universityFilterRowResponseDTO = new UniversityFilterRowResponseDTO();
			JsonObject jsonObject = row.value();
			if (jsonObject.containsKey("universityId") && jsonObject.getString("universityId") != null) {
				String universityId = jsonObject.getString("universityId");
				universityFilterRowResponseDTO.setUniversityId(universityId);
			}
			if (jsonObject.containsKey("universityName") && jsonObject.getString("universityName") != null) {
				String universityName = jsonObject.getString("universityName");
				universityFilterRowResponseDTO.setUniversityName(universityName);
			}
			if (jsonObject.containsKey("countFac") && jsonObject.getInt("countFac") != null) {
				int countFac = jsonObject.getInt("countFac");
				universityFilterRowResponseDTO.setCountFac(countFac);
			}
			listResponseDTOs.add(universityFilterRowResponseDTO);
		}

		return listResponseDTOs;
	}

	private List<CityFilterResponseDTO> mapResultToCityFilterRows(N1qlQueryResult result) {
		List<CityFilterResponseDTO> listResponseDTOs = new ArrayList<>();

		for (N1qlQueryRow row : result) {
			CityFilterResponseDTO cityFilterResponseDTO = new CityFilterResponseDTO();
			JsonObject jsonObject = row.value();
			if (jsonObject.containsKey("facultyCity") && jsonObject.getString("facultyCity") != null) {
				String facultyCity = jsonObject.getString("facultyCity");
				cityFilterResponseDTO.setFacultyCity(facultyCity);
			}
			if (jsonObject.containsKey("countFac") && jsonObject.getInt("countFac") != null) {
				int countFac = jsonObject.getInt("countFac");
				cityFilterResponseDTO.setCountFac(countFac);
			}
			listResponseDTOs.add(cityFilterResponseDTO);
		}

		return listResponseDTOs;
	}

	// not used
	public List<Faculty> getFilteredFacultiesV2(FacultyFilter facultyFilter) {
		N1qlQueryResult result = facultyRepo.getFilteredFacultiesV2(facultyFilter);
		List<Faculty> faculties = mapResultToFaculties(result);

		return faculties;
	}

	/*
	 * For each faculty search the universities list filter updates with relevant
	 * universities and the number of relevant containing faculties for each one.
	 */
	// not used
	public List<UniversityFilterRowResponseDTO> getUniversitiesForFilterV2(FacultyFilter facultyFilter) {
		N1qlQueryResult result = facultyRepo.getUniversitiesForFilterV2(facultyFilter);
		List<UniversityFilterRowResponseDTO> listResponseDTOs = mapResultToUniversityFilterRows(result);

		return listResponseDTOs;
	}

	/*
	 * For each faculty search the cities list filter updates with relevant cities
	 * and the number of relevant containing faculties for each one.
	 */
	// not used
	public List<CityFilterResponseDTO> getFacultyCitiesForFilterV2(FacultyFilter facultyFilter) {
		N1qlQueryResult result = facultyRepo.getFacultyCitiesForFilterV2(facultyFilter);
		List<CityFilterResponseDTO> listResponseDTOs = mapResultToCityFilterRows(result);

		return listResponseDTOs;
	}

}
