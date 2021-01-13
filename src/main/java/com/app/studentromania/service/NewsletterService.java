package com.app.studentromania.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Newsletter;
import com.app.studentromania.repo.NewsletterRepo;
import com.app.studentromania.util.LogUtils;

@Service
public class NewsletterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NewsletterService.class);

	@Autowired
	public NewsletterService() {
		LogUtils.logMessage(LOGGER, "NewsletterService initialized");
	}

	@Autowired
	private NewsletterRepo newsletterRepo;

	public ResponseDTO saveNewsletter() {
		Newsletter newsletter = new Newsletter();
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (!CollectionUtils.isEmpty(newsletters)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.NEWSLETTER_ALREADY_EXISTS);
		}
		newsletterRepo.save(newsletter);
		LogUtils.logMessage(LOGGER, "Newsletter document created!");

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
		LogUtils.logMessage(LOGGER, "Newsletter emails reset!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO addEmail(String email) {
		List<Newsletter> newsletters = newsletterRepo.findAll();
		if (CollectionUtils.isEmpty(newsletters)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.NEWSLETTER_NOT_FOUND);
		}
		Newsletter newsletter = newsletters.get(0);
		List<String> emails = newsletter.getEmails();
		if (emails.contains(email)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.NEWSLETTER_EMAIL_ALREADY_EXISTS);
		}
		emails.add(email);
		newsletter.setEmails(emails);
		newsletterRepo.save(newsletter);
		LogUtils.logMessage(LOGGER, "Email added to newsletter!");
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
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

}
