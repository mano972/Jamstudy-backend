package com.app.studentromania.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dao.UserProfileDAO;
import com.app.studentromania.dto.NewsletterDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.email.EmailHandler;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Newsletter;
import com.app.studentromania.model.UserProfile;
import com.app.studentromania.repo.NewsletterRepo;
import com.app.studentromania.util.LogUtils;

@Service
public class NewsletterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NewsletterService.class);

	@Autowired
	private NewsletterRepo newsletterRepo;

	@Autowired
	private UserProfileDAO userProfileDAO;

	@Autowired
	private EmailHandler emailHandler;

	@Autowired
	private LogUtils logUtils;

	public ResponseDTO saveNewsletter() {
		Newsletter newsletter = new Newsletter();
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (!CollectionUtils.isEmpty(newsletters)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.NEWSLETTER_ALREADY_EXISTS);
		}
		newsletterRepo.save(newsletter);
		logUtils.logMessage(LOGGER, "Newsletter document created!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO resetNewsletter() {
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (CollectionUtils.isEmpty(newsletters)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.NEWSLETTER_NOT_FOUND);
		}
		Newsletter newsletter = newsletters.get(0);
		newsletter.getEmails().clear();
		newsletterRepo.save(newsletter);
		logUtils.logMessage(LOGGER, "Newsletter emails reset!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO addEmail(String email) {
		ErrorsEnum errors = addEmailToNewsletter(email.toLowerCase());
		if (errors != ErrorsEnum.NO_ERROR) {
			return ResponseDTO.createErrorResponse(errors);
		}
		logUtils.logMessage(LOGGER, "Email added to newsletter!");
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(email);
		if (userProfileOpt.isPresent()) {
			userProfileOpt.get().setSubscribeToNewsletter(true);
			userProfileDAO.updateUserProfile(userProfileOpt.get());
		}
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ErrorsEnum addEmailToNewsletter(String email) {
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (CollectionUtils.isEmpty(newsletters)) {
			return ErrorsEnum.NEWSLETTER_NOT_FOUND;
		}
		Newsletter newsletter = newsletters.get(0);
		List<String> emails = newsletter.getEmails();
		if (emails.contains(email)) {
			return ErrorsEnum.NEWSLETTER_EMAIL_ALREADY_EXISTS;
		}
		emails.add(email);
		newsletter.setEmails(emails);
		newsletterRepo.save(newsletter);
		return ErrorsEnum.NO_ERROR;
	}

	public ResponseDTO removeEmail(String email) {
		ErrorsEnum errors = removeEmailFromNewsletter(email.toLowerCase());
		if (errors != ErrorsEnum.NO_ERROR) {
			return ResponseDTO.createErrorResponse(errors);
		}
		logUtils.logMessage(LOGGER, "Email removed from newsletter!");
		Optional<UserProfile> userProfileOpt = userProfileDAO.getByEmail(email);
		if (userProfileOpt.isPresent()) {
			userProfileOpt.get().setSubscribeToNewsletter(false);
			userProfileDAO.updateUserProfile(userProfileOpt.get());
		}
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ErrorsEnum removeEmailFromNewsletter(String emailToRemove) {
		return removeEmailFromNewsletter(Arrays.asList(emailToRemove));
	}

	public ErrorsEnum removeEmailFromNewsletter(List<String> emailsToRemove) {
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (CollectionUtils.isEmpty(newsletters)) {
			return ErrorsEnum.NEWSLETTER_NOT_FOUND;
		}
		Newsletter newsletter = newsletters.get(0);
		List<String> emails = newsletter.getEmails();
		emails.removeAll(emailsToRemove);
		newsletter.setEmails(emails);
		newsletterRepo.save(newsletter);
		return ErrorsEnum.NO_ERROR;
	}

	public boolean emailExists(String email) {
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (CollectionUtils.isEmpty(newsletters)) {
			return false;
		}
		Newsletter newsletter = newsletters.get(0);
		List<String> emails = newsletter.getEmails();
		if (emails.contains(email)) {
			return true;
		}
		return false;
	}

	public ResponseDTO getNewsletter() {
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (CollectionUtils.isEmpty(newsletters)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.NEWSLETTER_NOT_FOUND);
		}
		Newsletter newsletter = newsletters.get(0);
		JSONObject response = new JSONObject(newsletter);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO sendNewsletter(NewsletterDTO newsletterDTO) {
		startSendNewsletter(newsletterDTO.getTo(), newsletterDTO.getSubject(), newsletterDTO.getNewsletterBody(),
				newsletterDTO.getInlineImages(), newsletterDTO.getIsHtml());
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO startSendNewsletter(String to, String subject, String newsletterBody,
			Map<String, String> inlineImages, Boolean isHtml) {

		if (!StringUtils.isEmpty(to)) {
			ErrorsEnum errors = emailHandler.sendEmail(to, subject, newsletterBody, inlineImages);
			if (errors != ErrorsEnum.NO_ERROR) {
				return ResponseDTO.createErrorResponse(errors);
			}
		} else {
			List<Newsletter> newsletters = newsletterRepo.findAll();
			if (CollectionUtils.isEmpty(newsletters)) {
				return ResponseDTO.createErrorResponse(ErrorsEnum.NEWSLETTER_NOT_FOUND);
			}
			Newsletter newsletter = newsletters.get(0);
			List<String> userEmails = newsletter.getEmails();
			logUtils.logMessage(LOGGER, "Number of newsletter emails to send: " + userEmails.size());
			for (String userEmail : userEmails) {
				emailHandler.sendEmail(userEmail, subject, newsletterBody, inlineImages, isHtml);
			}
		}
		logUtils.logMessage(LOGGER, "Newsletter emails were sent!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

}
