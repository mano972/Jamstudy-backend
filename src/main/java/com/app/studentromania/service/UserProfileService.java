package com.app.studentromania.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.auth.JWTAuthenticationService;
import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dao.UserProfileDAO;
import com.app.studentromania.dto.AuthResponseDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.dto.UserProfileDTO;
import com.app.studentromania.dto.UserProfileResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.UserProfile;
import com.app.studentromania.model.UserProfileFaculty;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.SecurityUtils;

@Service
public class UserProfileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

	@Autowired
	private CustomRequestContext customRequestContext;

	@Autowired
	private UserProfileDAO userProfileDAO;

	@Autowired
	private FacultyDAO facultyDAO;

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	public UserProfileService() {
		LogUtils.logMessage(LOGGER, "UserProfileService initialized");
	}

	public ResponseDTO getAllUserProfiles() {
		List<UserProfile> userProfiles = userProfileDAO.getAllUserProfiles();
		JSONArray userProfilesArray = new JSONArray();
		userProfiles.forEach(userProfile -> {
			UserProfileResponseDTO userProfileResponseDTO = mapToUserProfileResponseDTO(userProfile);
			userProfilesArray.put(new JSONObject(userProfileResponseDTO));
		});
		JSONObject response = new JSONObject();
		response.put("userProfiles", userProfilesArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getByUserId(String userId) {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		UserProfileResponseDTO userProfileResponseDTO = mapToUserProfileResponseDTO(userProfile);
		JSONObject response = new JSONObject(userProfileResponseDTO);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getUserProfile() {
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}

		UserProfile userProfile = userProfileOpt.get();
		UserProfileResponseDTO userProfileResponseDTO = mapToUserProfileResponseDTO(userProfile);
		JSONObject response = new JSONObject(userProfileResponseDTO);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getUsersToNotify() {
		Map<String, List<String>> facultiesToNotifyPerEmail = new HashMap<>();

		List<UserProfile> usersToNotify = userProfileDAO.getUsersToNotify();
		usersToNotify.forEach(userProfile -> {
			List<String> facultyNames = userProfile.getFavoriteFaculties().stream()
					.filter(faculty -> faculty.isAllowNotification()).map(UserProfileFaculty::getFacultyName)
					.collect(Collectors.toList());
			facultiesToNotifyPerEmail.put(userProfile.getEmail(), facultyNames);
		});
		JSONObject response = new JSONObject();
		response.put("userProfiles", facultiesToNotifyPerEmail);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO login(UserProfileDTO userProfileDTO) throws NoSuchAlgorithmException, InvalidKeySpecException {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(userProfileDTO.getEmail());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_LOGIN_WRONG_CREDENTIALS);
		}
		UserProfile userProfile = userProfileOpt.get();
		if (!SecurityUtils.validatePassword(userProfileDTO.getPassword(), userProfile.getPassword())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_LOGIN_WRONG_CREDENTIALS);
		}

		String jwtToken = JWTAuthenticationService.generateJWT(userProfile.getUserId());
		if (StringUtils.isBlank(jwtToken)) {
			LogUtils.logMessage(LOGGER, "Error when generating JWT token for email " + userProfile.getEmail());
			return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_GENERATION_ERROR);
		}

		userProfile.setLastLogin(new Date());
		userProfileDAO.updateUserProfile(userProfile);

		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		authResponseDTO.setJwtToken(jwtToken);
		JSONObject response = new JSONObject(authResponseDTO);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO register(UserProfileDTO userProfileDTO)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (StringUtils.isEmpty(userProfileDTO.getEmail()) || StringUtils.isEmpty(userProfileDTO.getPassword())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EMAIL_PASSWORD_MISSING);
		}
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(userProfileDTO.getEmail());
		if (userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EXISTS);
		}
		UserProfile userProfile = new UserProfile();
		String generatedId = configDAO.generateDocumentId(Constants.USERPROFILE_PREFIX_ID);
		userProfile.setUserId(generatedId);
		String securePassword = SecurityUtils.generateSecurePassword(userProfileDTO.getPassword());
		mapToUserProfile(userProfileDTO, userProfile);
		userProfile.setPassword(securePassword);
		userProfileDAO.createUserProfile(userProfile);
		UserProfileResponseDTO userProfileResponseDTO = new UserProfileResponseDTO();
		userProfileResponseDTO.setUserId(userProfile.getUserId());
		userProfileResponseDTO.setUserName(userProfile.getUserName());

		return ResponseDTO.createSuccessResponse(new JSONObject(userProfileResponseDTO));
	}

	public ResponseDTO updateUserProfile(UserProfileDTO userProfileDTO) {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userProfileDTO.getUserId());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		mapToUserProfile(userProfileDTO, userProfile);
		userProfileDAO.updateUserProfile(userProfile);
		UserProfileResponseDTO userProfileResponseDTO = new UserProfileResponseDTO();
		userProfileResponseDTO.setUserId(userProfile.getUserId());
		userProfileResponseDTO.setUserName(userProfile.getUserName());

		return ResponseDTO.createSuccessResponse(new JSONObject(userProfileResponseDTO));
	}

	public ResponseDTO addRecentFaculty(String userId, String facultyId) {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
		if (!facultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		List<UserProfileFaculty> recentFaculties = userProfile.getRecentFaculties().stream()
				.sorted(Comparator.comparing(UserProfileFaculty::getAddedDate).reversed()).collect(Collectors.toList());
		if (!recentFaculties.stream().anyMatch(fac -> fac.getFacultyId().equals(facultyId))) {
			Faculty faculty = facultyOpt.get();
			UserProfileFaculty recentFaculty = new UserProfileFaculty();
			recentFaculty.setFacultyId(faculty.getFacultyId());
			recentFaculty.setFacultyName(faculty.getFacultyName());
			recentFaculty.setUniversityId(faculty.getUniversityId());
			recentFaculty.setUniversityName(faculty.getUniversityName());
			recentFaculty.setAddedDate(new Date());
			if (recentFaculties.size() == 5) {
				recentFaculties.remove(recentFaculties.size() - 1);
			}
			recentFaculties.add(0, recentFaculty);
			userProfile.setRecentFaculties(recentFaculties);
			userProfileDAO.updateUserProfile(userProfile);
			LogUtils.logMessage(LOGGER, "Faculty " + recentFaculty.getFacultyName()
					+ " was added to recent faculties for User Profile " + userProfile.getUserId());
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO addFavoriteFaculty(String facultyId, UserProfileDTO userProfileDTO) {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userProfileDTO.getUserId());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		List<UserProfileFaculty> favoriteFaculties = userProfile.getFavoriteFaculties();
		if (userProfileDTO.getAddFavoriteFaculty()) { // add
			Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
			if (!facultyOpt.isPresent()) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
			}
			if (favoriteFaculties.stream().anyMatch(fac -> fac.getFacultyId().equals(facultyId))) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_EXISTS);
			}
			UserProfileFaculty favoriteFaculty = new UserProfileFaculty();
			favoriteFaculty.setFacultyId(facultyOpt.get().getFacultyId());
			favoriteFaculty.setFacultyName(facultyOpt.get().getFacultyName());
			favoriteFaculty.setUniversityId(facultyOpt.get().getUniversityId());
			favoriteFaculty.setUniversityName(facultyOpt.get().getUniversityName());
			favoriteFaculty.setAddedDate(new Date());
			favoriteFaculties.add(favoriteFaculty);
			LogUtils.logMessage(LOGGER, "Faculty " + favoriteFaculty.getFacultyName()
					+ " was added to favorite faculties for User Profile " + userProfile.getUserId());
		} else { // remove
			boolean removedFavoriteFaculty = favoriteFaculties
					.removeIf(faculty -> faculty.getFacultyId().equals(facultyId));
			if (!removedFavoriteFaculty) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
			}
			LogUtils.logMessage(LOGGER, "Faculty " + facultyId
					+ " was removed from favorite faculties for User Profile " + userProfile.getUserId());
		}

		userProfile.setFavoriteFaculties(favoriteFaculties);
		userProfileDAO.updateUserProfile(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO allowNotificationForFaculty(String facultyId, UserProfileDTO userProfileDTO) {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userProfileDTO.getUserId());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		List<UserProfileFaculty> favoriteFaculties = userProfile.getFavoriteFaculties();
		Optional<UserProfileFaculty> favoriteFacultyOpt = favoriteFaculties.stream()
				.filter(fac -> fac.getFacultyId().equals(facultyId)).findFirst();
		if (!favoriteFacultyOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
		}
		if (userProfileDTO.getAllowNotification()) {
			favoriteFacultyOpt.get().setAllowNotification(true);
			LogUtils.logMessage(LOGGER, "Notification are now allowed for faculty "
					+ favoriteFacultyOpt.get().getFacultyName() + " for User Profile " + userProfile.getUserId());
		} else {
			favoriteFacultyOpt.get().setAllowNotification(false);
			LogUtils.logMessage(LOGGER, "Notification are no longer allowed for faculty "
					+ favoriteFacultyOpt.get().getFacultyName() + " for User Profile " + userProfile.getUserId());
		}
		userProfile.setFavoriteFaculties(favoriteFaculties);
		userProfileDAO.updateUserProfile(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO deleteAllUserProfiles() {
		userProfileDAO.deleteAllUserProfiles();
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	private void mapToUserProfile(UserProfileDTO userProfileDTO, UserProfile userProfile) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		mapper.map(userProfileDTO, userProfile);
	}

	private UserProfileResponseDTO mapToUserProfileResponseDTO(UserProfile userProfile) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper.map(userProfile, UserProfileResponseDTO.class);
	}

}
