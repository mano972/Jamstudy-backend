package com.app.studentromania.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.service.CustomRequestContext;
import com.app.studentromania.util.LogUtils;

@Aspect
@Component
public class RestCallAspect {

	@Autowired
	private CustomRequestContext customRequestContext;

	private static final Logger LOGGER = LoggerFactory.getLogger(RestCallAspect.class);

	@Around("@annotation(com.app.studentromania.annotation.RestCall)")
	public Object manageRestCall(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		String userId = customRequestContext.getUserId();

		final String endPointName = proceedingJoinPoint.getSignature().getName();
		LogUtils.logStart(LOGGER, endPointName, userId);

		CodeSignature codeSignature = (CodeSignature) proceedingJoinPoint.getSignature();
		String[] parameterNames = codeSignature.getParameterNames();
		Object[] parameterValues = proceedingJoinPoint.getArgs();
		LogUtils.logMessage(LOGGER, "Endpoint " + endPointName + " input parameters: ");
		for (int i = 0; i < parameterNames.length; i++) {
			LogUtils.logParameter(LOGGER, parameterNames[i],
					parameterValues[i] == null ? "null" : parameterValues[i].toString());
		}

		long start = System.currentTimeMillis();

		ResponseEntity<?> response = null;
		try {
			response = (ResponseEntity<?>) proceedingJoinPoint.proceed();
			LogUtils.logSuccess(LOGGER, endPointName, userId);
		} catch (Exception e) {
			LogUtils.logError(LOGGER, endPointName, e, userId);
			return ResponseDTO.createErrorResponse(ErrorsEnum.GENERAL_ERROR).createRestResponse();
		} finally {
			long totalTime = System.currentTimeMillis() - start;
			LogUtils.logEndPointExecutionTime(LOGGER, endPointName, totalTime, userId);
		}

		return response;

	}

}
