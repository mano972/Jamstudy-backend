package com.app.studentromania.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
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
import com.app.studentromania.dao.ReviewDAO;
import com.app.studentromania.dao.UserProfileDAO;
import com.app.studentromania.dto.AuthResponseDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.dto.UserProfileDTO;
import com.app.studentromania.dto.UserProfileResponseDTO;
import com.app.studentromania.email.EmailHandler;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.UserProfile;
import com.app.studentromania.model.UserProfileFaculty;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.SecurityUtils;
import com.app.studentromania.util.Utilities;

@Service
public class UserProfileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

	@Autowired
	private CustomRequestContext customRequestContext;

	@Autowired
	private NewsletterService newsletterService;

	@Autowired
	private UserProfileDAO userProfileDAO;

	@Autowired
	private FacultyDAO facultyDAO;

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private ReviewDAO reviewDAO;

	@Autowired
	private EmailHandler emailHandler;

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

	public ResponseDTO loginWithFacebook(UserProfileDTO userProfileDTO) {
		if (StringUtils.isEmpty(userProfileDTO.getEmail())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EMAIL_PASSWORD_MISSING);
		}
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(userProfileDTO.getEmail());
		if (userProfileOpt.isPresent()) {
			UserProfile userProfile = userProfileOpt.get();
			mapToUserProfile(userProfileDTO, userProfile);
			userProfile.setLastLogin(new Date());
			userProfileDAO.updateUserProfile(userProfile);

			String jwtToken = JWTAuthenticationService.generateJWT(userProfile.getUserId());
			if (StringUtils.isBlank(jwtToken)) {
				LogUtils.logMessage(LOGGER, "Error when generating JWT token for userId " + userProfile.getUserId());
				return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_GENERATION_ERROR);
			}

			AuthResponseDTO authResponseDTO = new AuthResponseDTO();
			authResponseDTO.setJwtToken(jwtToken);
			authResponseDTO.setFirstName(userProfile.getFirstName());
			authResponseDTO.setLastName(userProfile.getLastName());
			authResponseDTO.setFavoriteFaculties(userProfile.getFavoriteFaculties());
			authResponseDTO.setRecentFaculties(userProfile.getRecentFaculties());
			authResponseDTO.setAddedReviews(userProfile.getAddedReviews());
			authResponseDTO.setLikedReviews(userProfile.getLikedReviews());
			JSONObject response = new JSONObject(authResponseDTO);

			return ResponseDTO.createSuccessResponse(response);
		} else {
			UserProfile userProfile = new UserProfile();
			String generatedId = configDAO.generateDocumentId(Constants.USERPROFILE_PREFIX_ID);
			userProfile.setUserId(generatedId);
			mapToUserProfile(userProfileDTO, userProfile);
			if (newsletterService.emailExists(userProfile.getEmail())) {
				userProfile.setSubscribeToNewsletter(true);
			}
			userProfile.setAcceptTermsAndConditions(true);
			userProfile.setEmailConfirmed(true);
			userProfile.setLastLogin(new Date());
			userProfileDAO.createUserProfile(userProfile);

			String jwtToken = JWTAuthenticationService.generateJWT(userProfile.getUserId());
			if (StringUtils.isBlank(jwtToken)) {
				LogUtils.logMessage(LOGGER, "Error when generating JWT token for userId " + userProfile.getUserId());
				return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_GENERATION_ERROR);
			}

			AuthResponseDTO authResponseDTO = new AuthResponseDTO();
			authResponseDTO.setJwtToken(jwtToken);
			authResponseDTO.setFirstName(userProfile.getFirstName());
			authResponseDTO.setLastName(userProfile.getLastName());
			authResponseDTO.setFavoriteFaculties(userProfile.getFavoriteFaculties());
			authResponseDTO.setRecentFaculties(userProfile.getRecentFaculties());
			authResponseDTO.setAddedReviews(userProfile.getAddedReviews());
			authResponseDTO.setLikedReviews(userProfile.getLikedReviews());
			JSONObject response = new JSONObject(authResponseDTO);

			return ResponseDTO.createSuccessResponse(response);
		}

	}

	public ResponseDTO login(UserProfileDTO userProfileDTO) throws NoSuchAlgorithmException, InvalidKeySpecException {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(userProfileDTO.getEmail());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_LOGIN_WRONG_CREDENTIALS);
		}
		UserProfile userProfile = userProfileOpt.get();
		if (StringUtils.isEmpty(userProfile.getPassword())
				|| !SecurityUtils.validatePassword(userProfileDTO.getPassword(), userProfile.getPassword())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_LOGIN_WRONG_CREDENTIALS);
		}
		if (BooleanUtils.isNotTrue(userProfile.getEmailConfirmed())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_LOGIN_EMAIL_NOT_CONFIRMED);
		}

		String jwtToken = JWTAuthenticationService.generateJWT(userProfile.getUserId());
		if (StringUtils.isBlank(jwtToken)) {
			LogUtils.logMessage(LOGGER, "Error when generating JWT token for userId " + userProfile.getUserId());
			return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_GENERATION_ERROR);
		}

		userProfile.setLastLogin(new Date());
		userProfileDAO.updateUserProfile(userProfile);

		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		authResponseDTO.setJwtToken(jwtToken);
		authResponseDTO.setFirstName(userProfile.getFirstName());
		authResponseDTO.setLastName(userProfile.getLastName());
		authResponseDTO.setFavoriteFaculties(userProfile.getFavoriteFaculties());
		authResponseDTO.setRecentFaculties(userProfile.getRecentFaculties());
		authResponseDTO.setAddedReviews(userProfile.getAddedReviews());
		authResponseDTO.setLikedReviews(userProfile.getLikedReviews());
		JSONObject response = new JSONObject(authResponseDTO);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO register(UserProfileDTO userProfileDTO)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (StringUtils.isEmpty(userProfileDTO.getEmail()) || StringUtils.isEmpty(userProfileDTO.getPassword())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EMAIL_PASSWORD_MISSING);
		}

		ErrorsEnum error = validateRegister(userProfileDTO);
		if (ErrorsEnum.NO_ERROR != error) {
			return ResponseDTO.createErrorResponse(error);
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
		if (BooleanUtils.isTrue(userProfileDTO.getSubscribeToNewsletter())) {
			newsletterService.addEmailToNewsletter(userProfile.getEmail());
		} else if (newsletterService.emailExists(userProfile.getEmail())) {
			userProfile.setSubscribeToNewsletter(true);
		}
//		userProfile.setAcceptTermsAndConditions(true);
		createRegistrationVerificationToken(userProfile);
		userProfileDAO.createUserProfile(userProfile);
		ErrorsEnum emailError = sendRegistrationConfirmationEmail(userProfile);
		if (ErrorsEnum.NO_ERROR != emailError) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EMAIL_CONFIRMATION_FAILED);
		}
		UserProfileResponseDTO userProfileResponseDTO = new UserProfileResponseDTO();
		userProfileResponseDTO.setUserId(userProfile.getUserId());

		return ResponseDTO.createSuccessResponse(new JSONObject(userProfileResponseDTO));
	}

	public ResponseDTO resendConfirmation(UserProfileDTO userProfileDTO) {
		Optional<UserProfile> userProfileOpt = userProfileDAO
				.getByEmailConfirmationToken(userProfileDTO.getEmailConfirmationToken());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EMAIL_CONFIRMATION_FAILED);
		}
		UserProfile userProfile = userProfileOpt.get();
		createRegistrationVerificationToken(userProfile);
		userProfileDAO.updateUserProfile(userProfile);
		sendRegistrationConfirmationEmail(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO changePassword(UserProfileDTO userProfileDTO)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = Optional.empty();
		if (StringUtils.isNotEmpty(userId)) {
			userProfileOpt = userProfileDAO.getByUserId(userId);
		} else if (StringUtils.isNotEmpty(userProfileDTO.getPasswordResetToken())) {
			userProfileOpt = userProfileDAO.getByPasswordResetToken(userProfileDTO.getPasswordResetToken());
		}
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		String pass = userProfileDTO.getPassword();
		if (!isPasswordValid(pass)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.REGISTER_PASSWORD_ERROR);
		}
		String securePassword = SecurityUtils.generateSecurePassword(pass);
		userProfile.setPassword(securePassword);
		userProfile.setPasswordResetToken(null);
		userProfile.setPasswordResetTokenExpiration(null);
		userProfileDAO.updateUserProfile(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO resetPassword(UserProfileDTO userProfileDTO) {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(userProfileDTO.getEmail());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		createPasswordResetToken(userProfile);
		userProfileDAO.updateUserProfile(userProfile);
		ErrorsEnum emailError = sendPasswordResetEmail(userProfile);
		if (ErrorsEnum.NO_ERROR != emailError) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EMAIL_CONFIRMATION_FAILED);
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO verifyRegistration(UserProfileDTO userProfileDTO) {
		Optional<UserProfile> userProfileOpt = userProfileDAO
				.getByEmailConfirmationToken(userProfileDTO.getEmailConfirmationToken());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_REGISTER_VERIFICATION_FAILED);
		}
		UserProfile userProfile = userProfileOpt.get();
//		if ((userProfile.getEmailConfirmationTokenExpiration().before(new Date()))) {
//			return ErrorsEnum.USERPROFILE_REGISTER_VERIFICATION_EXPIRED;
//		}

		userProfile.setEmailConfirmationToken(null);
		userProfile.setEmailConfirmationTokenExpiration(null);
		userProfile.setEmailConfirmed(true);
		userProfileDAO.updateUserProfile(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO verifyPasswordReset(UserProfileDTO userProfileDTO) {
		Optional<UserProfile> userProfileOpt = userProfileDAO
				.getByPasswordResetToken(userProfileDTO.getPasswordResetToken());
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_RESET_PASS_VERIFICATION_FAILED);
		}
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO updateUserProfile(UserProfileDTO userProfileDTO) {
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		mapToUserProfile(userProfileDTO, userProfile);
		if (userProfileDTO.getBirthDate() != null) {
			userProfile.setFormattedBirthDate(Utilities.getFormattedBirthDate(userProfileDTO.getBirthDate()));
		}
		if (BooleanUtils.isTrue(userProfileDTO.getSubscribeToNewsletter())) {
			newsletterService.addEmailToNewsletter(userProfile.getEmail());
		} else if (BooleanUtils.isFalse(userProfileDTO.getSubscribeToNewsletter())) {
			newsletterService.removeEmailFromNewsletter(userProfile.getEmail());
		}
		userProfileDAO.updateUserProfile(userProfile);
		UserProfileResponseDTO userProfileResponseDTO = new UserProfileResponseDTO();
		userProfileResponseDTO.setUserId(userProfile.getUserId());
		userProfileResponseDTO.setUserName(userProfile.getUserName());

		return ResponseDTO.createSuccessResponse(new JSONObject(userProfileResponseDTO));
	}

	public ResponseDTO addRecentFaculty(String facultyId) {
		String userId = customRequestContext.getUserId();

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
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
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
			if (favoriteFaculties.size() >= Constants.MAX_FAVORITE_FACULTIES) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_MAX_FAVORITE_FACULTIES);
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
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
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

	private void createRegistrationVerificationToken(UserProfile userProfile) {
		String emailConfirmationToken = UUID.randomUUID().toString();
//		Integer hours = 24;
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(new Date());
//		cal.add(Calendar.HOUR, hours);

		userProfile.setEmailConfirmationToken(emailConfirmationToken);
//		userProfile.setEmailConfirmationTokenExpiration(cal.getTime());
	}

	private ErrorsEnum sendRegistrationConfirmationEmail(UserProfile userProfile) {
		String to = userProfile.getEmail();
		String subject = "Confirmă crearea unui cont nou pe platforma Unistart";
		String confirmationUrl = "https://unistart.ro" + "/login.html?token=" + userProfile.getEmailConfirmationToken();
		StringBuilder sb = new StringBuilder();
		sb.append("Bun venit in comunitatea Unistart! \nAcceseaza link-ul pentru a confirma crearea unui cont nou.\n")
				.append(confirmationUrl).append("\n").append("Parerea ta conteaza!\n")
				.append("Foloseste platforma Unistart pentru a gasi facultatea potrivita pentru tine.\n")
				.append("In cazul in care esti student sau absolvent, lasa o evaluarea facultatii tale si ajuta un elev sa ia decizia potrivita.\n")
				.append("Cu drag, \nEchipa Unistart");
		String message = sb.toString();

		return emailHandler.sendEmail(to, subject, message);
	}

	private void createPasswordResetToken(UserProfile userProfile) {
		String passwordResetToken = UUID.randomUUID().toString();
//		Integer hours = 24;
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(new Date());
//		cal.add(Calendar.HOUR, hours);

		userProfile.setPasswordResetToken(passwordResetToken);
//		userProfile.setPasswordResetTokenExpiration(cal.getTime());
	}

	private ErrorsEnum sendPasswordResetEmail(UserProfile userProfile) {
		String to = userProfile.getEmail();
		String subject = "Resetează parola";
		String confirmationUrl = "https://unistart.ro" + "/change.html?token=" + userProfile.getPasswordResetToken();
		StringBuilder sb = new StringBuilder();
		sb.append("Salut")
				.append(!StringUtils.isEmpty(userProfile.getFirstName()) ? (", " + userProfile.getFirstName()) : "")
				.append("!");
		sb.append("Ai cerut resetarea parolei. Te rugăm accesează link-ul de mai jos\n").append(confirmationUrl)
				.append("\n").append("Cu drag, \nEchipa Unistart");
		String message = sb.toString();

		return emailHandler.sendEmail(to, subject, message);
	}

	private ErrorsEnum validateRegister(UserProfileDTO userProfileDTO) {
		String email = userProfileDTO.getEmail();
		String pass = userProfileDTO.getPassword();
		Matcher emailMatcher = Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(email);
		if (!emailMatcher.find()) {
			return ErrorsEnum.REGISTER_EMAIL_NOT_VALID;
		}
		if (BooleanUtils.isNotTrue(userProfileDTO.getAcceptTermsAndConditions())) {
			return ErrorsEnum.REGISTER_TERMS_AND_CONDITIONS;
		}
		if (!isPasswordValid(pass)) {
			return ErrorsEnum.REGISTER_PASSWORD_ERROR;
		}

		return ErrorsEnum.NO_ERROR;
	}

	private boolean isPasswordValid(String pass) {
		Matcher passMatcher = Constants.VALID_PASSWORD_REGEX.matcher(pass);
		if (pass.length() < 7 || !passMatcher.find()) {
			return false;
		}
		return true;
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
