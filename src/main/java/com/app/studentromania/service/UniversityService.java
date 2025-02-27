package com.app.studentromania.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dao.UniversityDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.dto.UniversityDTO;
import com.app.studentromania.dto.UniversityResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.University;
import com.app.studentromania.util.Constants;

@Service
public class UniversityService {

	@Autowired
	private UniversityDAO universityDAO;

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private CustomRequestContext customRequestContext;

	public ResponseDTO getAllUniversities() {
		List<University> universities = universityDAO.getAllUniversities();
		JSONArray universitiesArray = new JSONArray();
		universities.forEach(university -> {
			UniversityResponseDTO universityResponseDTO = mapToUniversityResponseDTO(university);
			universitiesArray.put(new JSONObject(universityResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("universities", universitiesArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getByUniversityId(String universityId) {
		Optional<University> universityOpt = universityDAO.getByUniversityId(universityId);
		if (!universityOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.UNIVERSITY_NOT_FOUND);
		}
		University university = universityOpt.get();
		UniversityResponseDTO universityResponseDTO = mapToUniversityResponseDTO(university);
		JSONObject response = new JSONObject(universityResponseDTO);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO createUniversity(UniversityDTO universityDTO) {
		String generatedId = configDAO.generateDocumentId(Constants.UNIVERSITY_PREFIX_ID);
		University university = new University();
		university.setUniversityId(generatedId);
		university.setCountryCode(customRequestContext.getCountryCode());
		mapToUniversity(universityDTO, university);
		universityDAO.createUniversity(university);
		UniversityResponseDTO universityResponseDTO = new UniversityResponseDTO();
		universityResponseDTO.setUniversityId(university.getUniversityId());
		universityResponseDTO.setUniversityName(university.getUniversityName());
		return ResponseDTO.createSuccessResponse(new JSONObject(universityResponseDTO));
	}

	public ResponseDTO updateUniversity(UniversityDTO universityDTO) {
		if (StringUtils.isEmpty(universityDTO.getUniversityId())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.UNIVERSITY_ID_MISSING);
		}
		Optional<University> universityOpt = universityDAO.getByUniversityId(universityDTO.getUniversityId());
		if (!universityOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.UNIVERSITY_NOT_FOUND);
		}
		University university = universityOpt.get();
		mapToUniversity(universityDTO, university);
		universityDAO.updateUniversity(university);
		UniversityResponseDTO universityResponseDTO = new UniversityResponseDTO();
		universityResponseDTO.setUniversityId(university.getUniversityId());
		universityResponseDTO.setUniversityName(university.getUniversityName());
		return ResponseDTO.createSuccessResponse(new JSONObject(universityResponseDTO));
	}

	public ResponseDTO deleteAllUniversities() {
		universityDAO.deleteAllUniversities();
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	private void mapToUniversity(UniversityDTO universityDTO, University university) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		mapper.map(universityDTO, university);
	}

	private UniversityResponseDTO mapToUniversityResponseDTO(University university) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper.map(university, UniversityResponseDTO.class);
	}

}
