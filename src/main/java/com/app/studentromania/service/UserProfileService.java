package com.app.studentromania.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.auth.JwtAuthenticationService;
import com.app.studentromania.dao.CompanyDAO;
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
import com.app.studentromania.enumtype.RegisterTypeEnum;
import com.app.studentromania.model.Company;
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
	private CompanyDAO companyDAO;

	@Autowired
	private ReviewDAO reviewDAO;

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private EmailHandler emailHandler;

	@Autowired
	private LogUtils logUtils;

	@Autowired
	private JwtAuthenticationService jwtAuthenticationService;

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

	public ResponseDTO notifyUsers() {
		startNotifyUsers();
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public void startNotifyUsers() {
		List<UserProfile> usersToNotify = userProfileDAO.getUsersToNotify();

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, -7);
		Date oneWeekAgo = cal.getTime();
		Map<String, Long> facultyReviewsMap = reviewDAO.getReviewsByReviewDate(oneWeekAgo).stream()
				.map(review -> review.getFacultyId())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		int notifiedUsersCount = 0;
		for (UserProfile user : usersToNotify) {
			boolean sendEmail = false;
			StringBuilder sb = new StringBuilder();
			sb.append("Salut! \n Au fost adaugate evaluari noi pentru facultatile pe care le urmaresti.");
			for (UserProfileFaculty f : user.getFavoriteFaculties()) {
				if (f.isAllowNotification() && facultyReviewsMap.keySet().contains(f.getFacultyId())) {
					sb.append("\n").append(f.getFacultyName()).append(": ")
							.append(facultyReviewsMap.get(f.getFacultyId())).append(" evaluari noi.");
					sendEmail = true;
				}
			}
			if (sendEmail) {
				String emailMessage = sb.toString();
				String subject = "Noi evaluari pentru facultatile favorite";
				emailHandler.sendEmail(user.getEmail(), subject, emailMessage);
				notifiedUsersCount++;
			}
		}
		logUtils.logMessage(LOGGER, "Number of notified users: " + notifiedUsersCount);
	}

	public ResponseDTO loginWithSocialMedia(UserProfileDTO userProfileDTO) {
		if (StringUtils.isEmpty(userProfileDTO.getEmail())) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_EMAIL_PASSWORD_MISSING);
		}
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(userProfileDTO.getEmail());
		UserProfile userProfile = null;
		if (userProfileOpt.isPresent()) { // login
			userProfile = userProfileOpt.get();
			mapToUserProfile(userProfileDTO, userProfile);
			userProfile.setEmail(userProfileDTO.getEmail().toLowerCase());
			userProfile.setEmailConfirmed(true);
			if (BooleanUtils.isTrue(userProfileDTO.getSubscribeToNewsletter())) {
				newsletterService.addEmailToNewsletter(userProfile.getEmail());
			} else if (newsletterService.emailExists(userProfile.getEmail())) {
				userProfile.setSubscribeToNewsletter(true);
			}
			userProfile.setLastLogin(new Date());
			userProfileDAO.updateUserProfile(userProfile);
		} else { // register
			userProfile = new UserProfile();
			String generatedId = configDAO.generateDocumentId(Constants.USERPROFILE_PREFIX_ID);
			userProfile.setUserId(generatedId);
			mapToUserProfile(userProfileDTO, userProfile);
			userProfile.setEmail(userProfileDTO.getEmail().toLowerCase());
			if (BooleanUtils.isTrue(userProfileDTO.getSubscribeToNewsletter())) {
				newsletterService.addEmailToNewsletter(userProfile.getEmail());
			} else if (newsletterService.emailExists(userProfile.getEmail())) {
				userProfile.setSubscribeToNewsletter(true);
			}
			userProfile.setAcceptTermsAndConditions(true);
			userProfile.setEmailConfirmed(true);
			userProfile.setRegisterType(userProfileDTO.getRegisterType());
			userProfile.setLastLogin(new Date());
			userProfileDAO.createUserProfile(userProfile);
		}

		String jwtToken = jwtAuthenticationService.generateJWT(userProfile.getUserId());
		if (StringUtils.isBlank(jwtToken)) {
			logUtils.logMessage(LOGGER, "Error when generating JWT token for userId " + userProfile.getUserId());
			return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_GENERATION_ERROR);
		}

		AuthResponseDTO authResponseDTO = buildAuthReponseDto(userProfile, jwtToken);
		JSONObject response = new JSONObject(authResponseDTO);

		return ResponseDTO.createSuccessResponse(response);

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

		String jwtToken = jwtAuthenticationService.generateJWT(userProfile.getUserId());
		if (StringUtils.isBlank(jwtToken)) {
			logUtils.logMessage(LOGGER, "Error when generating JWT token for userId " + userProfile.getUserId());
			return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_GENERATION_ERROR);
		}

		userProfile.setLastLogin(new Date());
		userProfileDAO.updateUserProfile(userProfile);

		AuthResponseDTO authResponseDTO = buildAuthReponseDto(userProfile, jwtToken);
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
		userProfile.setEmail(userProfileDTO.getEmail().toLowerCase());
		userProfile.setPassword(securePassword);
		if (BooleanUtils.isTrue(userProfileDTO.getSubscribeToNewsletter())) {
			newsletterService.addEmailToNewsletter(userProfile.getEmail());
		} else if (newsletterService.emailExists(userProfile.getEmail())) {
			userProfile.setSubscribeToNewsletter(true);
		}
		userProfile.setRegisterType(RegisterTypeEnum.REGULAR.getValue());
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
		Optional<UserProfile> userProfileOpt = Optional.empty();
		if (StringUtils.isNotEmpty(userProfileDTO.getEmail())) {
			userProfileOpt = userProfileDAO.getByEmail(userProfileDTO.getEmail());
		} else if (StringUtils.isNotEmpty(userProfileDTO.getEmailConfirmationToken())) {
			userProfileOpt = userProfileDAO.getByEmailConfirmationToken(userProfileDTO.getEmailConfirmationToken());
		}
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
		/*
		 * mapper has an issue. it cannot map internal lists from which elements were
		 * removed.
		 */
		if (userProfileDTO.getUserDomainInterest() != null) {
			userProfile.setUserDomainInterest(new ArrayList<>());
		}
		if (userProfileDTO.getUserCityInterest() != null) {
			userProfile.setUserCityInterest(new ArrayList<>());
		}
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
			logUtils.logMessage(LOGGER, "Faculty " + recentFaculty.getFacultyName()
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
			favoriteFaculty.setAllowNotification(true);
			favoriteFaculty.setAddedDate(new Date());
			favoriteFaculties.add(favoriteFaculty);
			logUtils.logMessage(LOGGER, "Faculty " + favoriteFaculty.getFacultyName()
					+ " was added to favorite faculties for User Profile " + userProfile.getUserId());
		} else { // remove
			boolean removedFavoriteFaculty = favoriteFaculties
					.removeIf(faculty -> faculty.getFacultyId().equals(facultyId));
			if (!removedFavoriteFaculty) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
			}
			logUtils.logMessage(LOGGER, "Faculty " + facultyId
					+ " was removed from favorite faculties for User Profile " + userProfile.getUserId());
		}

		userProfile.setFavoriteFaculties(favoriteFaculties);
		userProfileDAO.updateUserProfile(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO addFavoriteCompany(String companyId, UserProfileDTO userProfileDTO) {
		String userId = customRequestContext.getUserId();

		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		UserProfile userProfile = userProfileOpt.get();
		List<String> favoriteCompanies = userProfile.getFavoriteCompanies();
		if (userProfileDTO.getAddFavoriteCompany()) { // add
			Optional<Company> companyOpt = companyDAO.getByCompanyId(companyId);
			if (!companyOpt.isPresent()) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.COMPANY_NOT_FOUND);
			}
			if (favoriteCompanies.contains(companyId)) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.COMPANY_EXISTS);
			}
			if (favoriteCompanies.size() >= Constants.MAX_FAVORITE_COMPANIES) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_MAX_FAVORITE_FACULTIES);
			}
			favoriteCompanies.add(companyId);
			logUtils.logMessage(LOGGER, "Company " + companyOpt.get().getCompanyName()
					+ " was added to favorite companies for User Profile " + userProfile.getUserId());
		} else { // remove
			boolean removedFavoriteCompany = favoriteCompanies.remove(companyId);
			if (!removedFavoriteCompany) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.COMPANY_NOT_FOUND);
			}
			logUtils.logMessage(LOGGER, "Company " + companyId
					+ " was removed from favorite companies for User Profile " + userProfile.getUserId());
		}

		userProfile.setFavoriteCompanies(favoriteCompanies);
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
			logUtils.logMessage(LOGGER, "Notifications are now allowed for faculty "
					+ favoriteFacultyOpt.get().getFacultyName() + " for User Profile " + userProfile.getUserId());
		} else {
			favoriteFacultyOpt.get().setAllowNotification(false);
			logUtils.logMessage(LOGGER, "Notifications are no longer allowed for faculty "
					+ favoriteFacultyOpt.get().getFacultyName() + " for User Profile " + userProfile.getUserId());
		}
		userProfile.setFavoriteFaculties(favoriteFaculties);
		userProfileDAO.updateUserProfile(userProfile);

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO deleteByUserId(String userId) {
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
		if (!userProfileOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
		}
		newsletterService.removeEmailFromNewsletter(userProfileOpt.get().getEmail());
		userProfileDAO.deleteUserProfile(userProfileOpt.get());
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO deleteAllUserProfiles() {
		List<String> emailsToRemove = new ArrayList<>();
		for (UserProfile userProfile : userProfileDAO.getAllUserProfiles()) {
			emailsToRemove.add(userProfile.getEmail());
		}
		newsletterService.removeEmailFromNewsletter(emailsToRemove);
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
		String subject = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_REGISTER_CONFIRMATION_EMAIL_SUBJECT,
				Constants.DEFAULT_REGISTER_CONFIRMATION_EMAIL_SUBJECT);
		String confirmationUrl = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_REGISTER_CONFIRMATION_EMAIL_URL,
				Constants.DEFAULT_REGISTER_CONFIRMATION_EMAIL_URL) + userProfile.getEmailConfirmationToken();
		String message = String
				.format(configDAO.getValueByConfigKey(Constants.CONFIG_KEY_REGISTER_CONFIRMATION_EMAIL_TEXT,
						Constants.DEFAULT_REGISTER_CONFIRMATION_EMAIL_TEXT), confirmationUrl);

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
		String subject = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_RESET_PASS_EMAIL_SUBJECT,
				Constants.DEFAULT_RESET_PASS_EMAIL_SUBJECT);
		String resetUrl = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_RESET_PASS_EMAIL_URL,
				Constants.DEFAULT_RESET_PASS_EMAIL_URL) + userProfile.getPasswordResetToken();
		String userFirstName = !StringUtils.isEmpty(userProfile.getFirstName()) ? (", " + userProfile.getFirstName())
				: "";
		String message = String.format(configDAO.getValueByConfigKey(Constants.CONFIG_KEY_RESET_PASS_EMAIL_TEXT,
				Constants.DEFAULT_RESET_PASS_EMAIL_TEXT), userFirstName, resetUrl);

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

	private AuthResponseDTO buildAuthReponseDto(UserProfile userProfile, String jwtToken) {
		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		authResponseDTO.setJwtToken(jwtToken);
		authResponseDTO.setFirstName(userProfile.getFirstName());
		authResponseDTO.setLastName(userProfile.getLastName());
		authResponseDTO.setFavoriteFaculties(userProfile.getFavoriteFaculties());
		authResponseDTO.setFavoriteCompanies(userProfile.getFavoriteCompanies());
		authResponseDTO.setRecentFaculties(userProfile.getRecentFaculties());
		authResponseDTO.setAddedReviews(userProfile.getAddedReviews());
		authResponseDTO.setLikedReviews(userProfile.getLikedReviews());
		return authResponseDTO;
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
		mapper.addMappings(new PropertyMap<UserProfileDTO, UserProfile>() {
			@Override
			protected void configure() {
				skip(destination.getPassword());
				skip(destination.getEmail());
			}
		});
		mapper.map(userProfileDTO, userProfile);
	}

	private UserProfileResponseDTO mapToUserProfileResponseDTO(UserProfile userProfile) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper.map(userProfile, UserProfileResponseDTO.class);
	}

}
