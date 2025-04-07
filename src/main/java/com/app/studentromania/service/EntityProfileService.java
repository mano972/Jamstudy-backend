package com.app.studentromania.service;

import com.app.studentromania.auth.JwtAuthenticationService;
import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dao.EntityProfileDAO;
import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dao.UniversityDAO;
import com.app.studentromania.dto.AuthResponseDTO;
import com.app.studentromania.dto.EntityProfileDTO;
import com.app.studentromania.dto.EntityProfileResponseDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.EntityProfile;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.University;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EntityProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityProfileService.class);

    @Autowired
    private ConfigDAO configDAO;

    @Autowired
    private UniversityDAO universityDAO;

    @Autowired
    private FacultyDAO facultyDAO;

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private EntityProfileDAO entityProfileDAO;

    @Autowired
    private CustomRequestContext customRequestContext;

    @Autowired
    private JwtAuthenticationService jwtAuthenticationService;

    @Autowired
    private LogUtils logUtils;

    public ResponseDTO createEntityProfile(EntityProfileDTO entityProfileDto) {
        Optional<University> universityOpt = universityDAO.getByUniversityId(entityProfileDto.getUniversityId());
        if (!universityOpt.isPresent()) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.UNIVERSITY_NOT_FOUND);
        }
        Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(entityProfileDto.getFacultyId());
        if (!facultyOpt.isPresent()) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
        }
        EntityProfile entityProfile = new EntityProfile();
        String generatedId = configDAO.generateDocumentId(Constants.FACULTY_PREFIX_ID);
        entityProfile.setEntityId(generatedId);
        mapToEntityProfile(entityProfileDto, entityProfile);
        entityProfileDAO.createEntityProfile(entityProfile);
        EntityProfileResponseDTO entityProfileResponseDTO = new EntityProfileResponseDTO();
        entityProfileResponseDTO.setEntityId(entityProfile.getEntityId());
        entityProfileResponseDTO.setFacultyId(entityProfile.getFacultyId());
        entityProfileResponseDTO.setUniversityId(entityProfile.getUniversityId());

        return ResponseDTO.createSuccessResponse(new JSONObject(entityProfileResponseDTO));
    }

    public ResponseDTO login(EntityProfileDTO entityProfileDTO) {
        Optional<EntityProfile> entityProfileOpt = entityProfileDAO.getByUsername(entityProfileDTO.getUsername());
        if (!entityProfileOpt.isPresent()) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.ENTITY_PROFILE_NOT_FOUND);
        }
        EntityProfile entityProfile = entityProfileOpt.get();
        if (StringUtils.isEmpty(entityProfile.getPassword())
                || !StringUtils.equals(entityProfileDTO.getPassword(), entityProfile.getPassword())) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.ENTITY_PROFILE_NOT_FOUND);
        }

        String jwtToken = jwtAuthenticationService.generateJWT(entityProfile.getEntityId());
        if (StringUtils.isBlank(jwtToken)) {
            logUtils.logMessage(LOGGER, "Error when generating JWT token for userId " + entityProfile.getEntityId());
            return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_GENERATION_ERROR);
        }

        AuthResponseDTO authResponseDTO = new AuthResponseDTO();
        authResponseDTO.setJwtToken(jwtToken);
        authResponseDTO.setFirstName(entityProfile.getUsername());
        JSONObject response = new JSONObject(authResponseDTO);

        return ResponseDTO.createSuccessResponse(response);
    }

    public ResponseDTO getEntityAdminFaculty() {
        String entityProfileId = customRequestContext.getEntityProfileId();
        Optional<EntityProfile> entityProfileOpt = entityProfileDAO.getByEntityId(entityProfileId);
        if (!entityProfileOpt.isPresent()) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.ENTITY_PROFILE_NOT_FOUND);
        }
        return facultyService.getByFacultyId(entityProfileOpt.get().getFacultyId());
    }

    public ResponseDTO getEntityAdminReviews() {
        String entityProfileId = customRequestContext.getEntityProfileId();
        Optional<EntityProfile> entityProfileOpt = entityProfileDAO.getByEntityId(entityProfileId);
        if (!entityProfileOpt.isPresent()) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.ENTITY_PROFILE_NOT_FOUND);
        }
        EntityProfile entityProfile = entityProfileOpt.get();
        Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(entityProfile.getFacultyId());
        if (!facultyOpt.isPresent()) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
        }
        return reviewService.getAllReviewsByFacultyId(entityProfile.getFacultyId());
    }

    private void mapToEntityProfile(EntityProfileDTO entityProfileDto, EntityProfile entityProfile) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.map(entityProfileDto, entityProfile);
    }

    private EntityProfileResponseDTO mapToEntityProfileResponseDTO(EntityProfile entityProfile) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper.map(entityProfile, EntityProfileResponseDTO.class);
    }

}
