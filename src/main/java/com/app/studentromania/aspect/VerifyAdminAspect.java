package com.app.studentromania.aspect;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;

@Aspect
@Component
public class VerifyAdminAspect {

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private LogUtils logUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(VerifyAdminAspect.class);

	@Around("@annotation(com.app.studentromania.annotation.VerifyAdmin)")
	public Object verifyAdmin(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();

		final String authorization = request.getHeader("Authorization");

		if (authorization == null || !authorization.startsWith("Basic")) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ADMIN_LOGIN_MISSING_HEADER).createRestResponse();
		}

		// Authorization: Basic base64credentials
		String base64Credentials = authorization.substring("Basic".length()).trim();
		byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
		String credentials = new String(credDecoded, StandardCharsets.UTF_8);
		// credentials = username:password
		final String[] values = credentials.split(":", 2);
		final String userName = values[0];
		final String pass = values[1];

		final String configAdminUserName = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_ADMIN_USERNAME, null);
		final String configAdminPass = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_ADMIN_PASS, null);

		final String endPointName = proceedingJoinPoint.getSignature().getName();
		if (!StringUtils.equals(configAdminUserName, userName) || !StringUtils.equals(configAdminPass, pass)) {
			logUtils.logMessage(LOGGER,
					"Admin credentials are wrong. Cannot access <" + endPointName + "> at [" + new Date() + "]");
			return ResponseDTO.createErrorResponse(ErrorsEnum.ADMIN_LOGIN_WRONG_CREDENTIALS).createRestResponse();
		}

		logUtils.logMessage(LOGGER, "Admin accessed <" + endPointName + "> at [" + new Date() + "]");

		Object proceed = proceedingJoinPoint.proceed();

		return proceed;
	}

}
