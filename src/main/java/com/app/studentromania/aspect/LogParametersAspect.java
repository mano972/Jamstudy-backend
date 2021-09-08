package com.app.studentromania.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.util.LogUtils;

@Aspect
@Component
public class LogParametersAspect {

	@Autowired
	private LogUtils logUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(LogParametersAspect.class);

	@Around("@annotation(com.app.studentromania.annotation.LogParameters)")
	public Object logParameters(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		final String methodName = proceedingJoinPoint.getSignature().getName();
		final String fullClassName = proceedingJoinPoint.getSignature().getDeclaringTypeName();
		logUtils.logMessage(LOGGER, fullClassName + "." + methodName + "() input parameters: ");

		CodeSignature codeSignature = (CodeSignature) proceedingJoinPoint.getSignature();
		String[] parameterNames = codeSignature.getParameterNames();
		Object[] parameterValues = proceedingJoinPoint.getArgs();
		for (int i = 0; i < parameterNames.length; i++) {
			logUtils.logParameter(LOGGER, parameterNames[i],
					parameterValues[i] == null ? "null" : parameterValues[i].toString());
		}
		Object proceed = proceedingJoinPoint.proceed();

		return proceed;

	}

}
