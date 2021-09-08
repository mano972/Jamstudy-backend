package com.app.studentromania.aspect;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.service.CustomRequestContext;
import com.app.studentromania.util.LogUtils;

@Aspect
@Component
public class VerifyLoggedInAspect {

	@Autowired
	private CustomRequestContext customRequestContext;

	@Autowired
	private LogUtils logUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(VerifyLoggedInAspect.class);

	@Around("@annotation(com.app.studentromania.annotation.VerifyLoggedIn)")
	public Object VerifyLoggedIn(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		String userId = customRequestContext.getUserId();
		if (StringUtils.isBlank(userId)) {
			final String endPointName = proceedingJoinPoint.getSignature().getName();
			logUtils.logMessage(LOGGER,
					"User is not logged in. Cannot access <" + endPointName + "> at [" + new Date() + "]");
			return ResponseDTO.createErrorResponse(ErrorsEnum.USER_NOT_LOGGED_IN);
		}

		Object proceed = proceedingJoinPoint.proceed();

		return proceed;

	}

}
