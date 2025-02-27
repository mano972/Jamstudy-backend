package com.app.studentromania.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.app.studentromania.dao.UniversityDAO;
import com.app.studentromania.dto.CityFilterResponseDTO;
import com.app.studentromania.dto.FacultyDTO;
import com.app.studentromania.dto.FacultyListRowResponseDTO;
import com.app.studentromania.dto.FacultyResponseDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.dto.UniversityFilterRowResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.University;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.FacultyFilter;
import com.app.studentromania.util.LogUtils;

@Service
public class FacultyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacultyService.class);

	@Autowired
	private FacultyDAO facultyDAO;

	@Autowired
	private UniversityDAO universityDAO;

	@Autowired
	private ReviewDAO reviewDAO;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private CustomRequestContext customRequestContext;

	@Autowired
	private LogUtils logUtils;

	public List<Faculty> getAllFaculties() {
		return facultyDAO.getAllFaculties();
	}

	public ResponseDTO getFilteredFaculties(FacultyFilter facultyFilter) {
		facultyFilter.setCountryCode(customRequestContext.getCountryCode());
		List<Faculty> faculties = facultyDAO.getFilteredFaculties(facultyFilter);
		long count = facultyDAO.countFilteredFaculties(facultyFilter);
		JSONArray facultiesArray = new JSONArray();
		faculties.forEach(faculty -> {
			FacultyListRowResponseDTO facultyListRowResponseDTO = mapToFacultyListRowResponseDTO(faculty);
			facultiesArray.put(new JSONObject(facultyListRowResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("faculties", facultiesArray);
		response.put("count", count);

		logUtils.logMessage(LOGGER,
				"Searched faculties criteria " + facultyFilter.toString() + "with result count: " + count);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getFilteredFacultiesDetailed(FacultyFilter facultyFilter) {
		facultyFilter.setCountryCode(customRequestContext.getCountryCode());
		List<Faculty> faculties = facultyDAO.getFilteredFaculties(facultyFilter);
		long count = facultyDAO.countFilteredFaculties(facultyFilter);
		JSONArray facultiesArray = new JSONArray();
		faculties.forEach(faculty -> {
			FacultyResponseDTO facultyResponseDTO = mapToFacultyResponseDTO(faculty);
			facultiesArray.put(new JSONObject(facultyResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("faculties", facultiesArray);
		response.put("count", count);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getUniversitiesForFilter(FacultyFilter facultyFilter) {
		facultyFilter.setCountryCode(customRequestContext.getCountryCode());
		List<UniversityFilterRowResponseDTO> listResponseDTOs = facultyDAO.getUniversitiesForFilter(facultyFilter);
		JSONArray listResponseArray = new JSONArray();
		listResponseDTOs.forEach(universityFilterRowResponseDTO -> {
			listResponseArray.put(new JSONObject(universityFilterRowResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("universityfilter", listResponseArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getFacultyCitiesForFilter(FacultyFilter facultyFilter) {
		facultyFilter.setCountryCode(customRequestContext.getCountryCode());
		List<CityFilterResponseDTO> listResponseDTOs = facultyDAO.getFacultyCitiesForFilter(facultyFilter);
		JSONArray listResponseArray = new JSONArray();
		listResponseDTOs.forEach(cityFilterResponseDTO -> {
			listResponseArray.put(new JSONObject(cityFilterResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("facultyCityfilter", listResponseArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getByFacultyId(String facultyId) {
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = facultyOpt.get();
		FacultyResponseDTO facultyResponseDTO = mapToFacultyResponseDTO(faculty);
		JSONObject response = new JSONObject(facultyResponseDTO);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getByUniversityId(String universityId) {
		List<Faculty> faculties = facultyDAO.getByUniversityId(universityId);
		JSONArray facultiesArray = new JSONArray();
		faculties.forEach(faculty -> {
			FacultyResponseDTO facultyResponseDTO = mapToFacultyResponseDTO(faculty);
			facultiesArray.put(new JSONObject(facultyResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("faculties", facultiesArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getFacultyReviewsDetails(String facultyId) {
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = new Faculty();
		reviewDAO.updateReviewsDetailsForFaculty(faculty);
		FacultyResponseDTO facultyResponseDTO = mapToFacultyResponseDTO(faculty);
		JSONObject response = new JSONObject();
		response.put("facultyDetails", new JSONObject(facultyResponseDTO));

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getFacultyLogo(String facultyId) throws IOException {
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = facultyOpt.get();
		String facultyName = faculty.getFacultyName();
		String universityName = faculty.getUniversityName();
		String logoName = facultyName + universityName;
		String logoPath = Constants.LOGO_PATH_ROOT + logoName + Constants.LOGO_IMAGE_TYPE;
		File tmpDir = new File(logoPath);
		if (tmpDir.exists()) {
			InputStream in = getClass().getClassLoader().getResourceAsStream(logoPath);
			return ResponseDTO.createMediaSuccessResponse(IOUtils.toByteArray(in));
		}
		universityName = universityName.replace("\"", "_");
		logoName = facultyName + universityName;
		logoPath = Constants.LOGO_PATH_ROOT + logoName + Constants.LOGO_IMAGE_TYPE;
		tmpDir = new File(logoPath);
		if (tmpDir.exists()) {
			InputStream in = getClass().getClassLoader().getResourceAsStream(logoPath);
			return ResponseDTO.createMediaSuccessResponse(IOUtils.toByteArray(in));
		}
		logoPath = Constants.LOGO_PATH_ROOT + Constants.DEFAULT_IMAGE + Constants.LOGO_IMAGE_TYPE;
		tmpDir = new File(logoPath);
		InputStream in = getClass().getClassLoader().getResourceAsStream(logoPath);
		return ResponseDTO.createMediaSuccessResponse(IOUtils.toByteArray(in));
	}

	public ResponseDTO createFaculty(FacultyDTO facultyDTO) {
		Optional<University> universityOpt = universityDAO.getByUniversityId(facultyDTO.getUniversityId());
		if (!universityOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.UNIVERSITY_NOT_FOUND);
		}
		Faculty faculty = new Faculty();
		String generatedId = configDAO.generateDocumentId(Constants.FACULTY_PREFIX_ID);
		faculty.setFacultyId(generatedId);
		faculty.setUniversityName(universityOpt.get().getUniversityName());
		mapToFaculty(facultyDTO, faculty);
		faculty.setCountryCode(customRequestContext.getCountryCode());
		facultyDAO.createFaculty(faculty);
		FacultyResponseDTO facultyResponseDTO = new FacultyResponseDTO();
		facultyResponseDTO.setFacultyId(faculty.getFacultyId());
		facultyResponseDTO.setFacultyName(faculty.getFacultyName());
		facultyResponseDTO.setUniversityId(faculty.getUniversityId());
		facultyResponseDTO.setUniversityName(faculty.getUniversityName());

		return ResponseDTO.createSuccessResponse(new JSONObject(facultyResponseDTO));
	}

	public ResponseDTO updateFaculty(FacultyDTO facultyDTO) {
		if (StringUtils.isEmpty(facultyDTO.getFacultyId())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_ID_MISSING);
		}
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyDTO.getFacultyId());
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		Faculty faculty = facultyOpt.get();
		/*
		 * mapper has an issue. it cannot map internal lists from which elements were
		 * removed.
		 */
		if (facultyDTO.getFacultyDomainsLicense() != null) {
			faculty.setFacultyDomainsLicense(new ArrayList<>());
		}
		if (facultyDTO.getFacultyDomainsMaster() != null) {
			faculty.setFacultyDomainsMaster(new ArrayList<>());
		}
		mapToFaculty(facultyDTO, faculty);
		facultyDAO.updateFaculty(faculty);
		FacultyResponseDTO facultyResponseDTO = new FacultyResponseDTO();
		facultyResponseDTO.setFacultyId(faculty.getFacultyId());
		facultyResponseDTO.setFacultyName(faculty.getFacultyName());
		facultyResponseDTO.setUniversityId(faculty.getUniversityId());
		facultyResponseDTO.setUniversityName(faculty.getUniversityName());

		return ResponseDTO.createSuccessResponse(new JSONObject(facultyResponseDTO));
	}

	public void updateAllFacultiesReviewDetails() {
		List<Faculty> faculties = facultyDAO.getAllFaculties();
		for (Faculty faculty : faculties) {
			reviewService.updateFacultyReviewDetails(faculty);
		}
	}

	public ResponseDTO deleteAllFaculties() {
		facultyDAO.deleteAllFaculties();
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	private void mapToFaculty(FacultyDTO facultyDTO, Faculty faculty) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		mapper.map(facultyDTO, faculty);
	}

	private FacultyResponseDTO mapToFacultyResponseDTO(Faculty faculty) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper.map(faculty, FacultyResponseDTO.class);
	}

	private FacultyListRowResponseDTO mapToFacultyListRowResponseDTO(Faculty faculty) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper.map(faculty, FacultyListRowResponseDTO.class);
	}

	// not used
	public ResponseDTO getFilteredFacultiesV2(FacultyFilter facultyFilter) {
		List<Faculty> faculties = facultyDAO.getFilteredFacultiesV2(facultyFilter);
		long count = facultyDAO.countFilteredFaculties(facultyFilter);
		JSONArray facultiesArray = new JSONArray();
		faculties.forEach(faculty -> {
			FacultyListRowResponseDTO facultyListRowResponseDTO = mapToFacultyListRowResponseDTO(faculty);
			facultiesArray.put(new JSONObject(facultyListRowResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("faculties", facultiesArray);
		response.put("count", count);

		return ResponseDTO.createSuccessResponse(response);
	}

	// not used
	public ResponseDTO getUniversitiesForFilterV2(FacultyFilter facultyFilter) {
		List<UniversityFilterRowResponseDTO> listResponseDTOs = facultyDAO.getUniversitiesForFilterV2(facultyFilter);
		JSONArray listResponseArray = new JSONArray();
		listResponseDTOs.forEach(universityFilterRowResponseDTO -> {
			listResponseArray.put(new JSONObject(universityFilterRowResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("universityfilter", listResponseArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	// not used
	public ResponseDTO getFacultyCitiesForFilterV2(FacultyFilter facultyFilter) {
		List<CityFilterResponseDTO> listResponseDTOs = facultyDAO.getFacultyCitiesForFilterV2(facultyFilter);
		JSONArray listResponseArray = new JSONArray();
		listResponseDTOs.forEach(cityFilterResponseDTO -> {
			listResponseArray.put(new JSONObject(cityFilterResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("facultyCityfilter", listResponseArray);

		return ResponseDTO.createSuccessResponse(response);
	}

}
