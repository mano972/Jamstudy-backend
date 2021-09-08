package com.app.studentromania.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.util.LogUtils;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RestCallAspect {

	@Autowired
	private LogUtils logUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(RestCallAspect.class);

	@Around("@annotation(com.app.studentromania.annotation.RestCall)")
	public Object manageRestCall(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		final String endPointName = proceedingJoinPoint.getSignature().getName();
		logUtils.logStart(LOGGER, endPointName);

		CodeSignature codeSignature = (CodeSignature) proceedingJoinPoint.getSignature();
		String[] parameterNames = codeSignature.getParameterNames();
		Object[] parameterValues = proceedingJoinPoint.getArgs();
		logUtils.logMessage(LOGGER, "Endpoint " + endPointName + " input parameters: ");
		for (int i = 0; i < parameterNames.length; i++) {
			logUtils.logParameter(LOGGER, parameterNames[i],
					parameterValues[i] == null ? "null" : parameterValues[i].toString());
		}

		long start = System.currentTimeMillis();

		ResponseEntity<?> response = null;
		try {
			response = (ResponseEntity<?>) proceedingJoinPoint.proceed();
			logUtils.logSuccess(LOGGER, endPointName);
		} catch (Exception e) {
			logUtils.logError(LOGGER, endPointName, e);
			return ResponseDTO.createErrorResponse(ErrorsEnum.GENERAL_ERROR).createRestResponse();
		} finally {
			long totalTime = System.currentTimeMillis() - start;
			logUtils.logEndPointExecutionTime(LOGGER, endPointName, totalTime);
		}

		return response;

	}

}
