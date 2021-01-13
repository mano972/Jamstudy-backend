package com.app.studentromania.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.app.studentromania.util.LogUtils;

@Aspect
@Component
public class LogExecutionTimeAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogExecutionTimeAspect.class);

	@Around("@annotation(com.app.studentromania.annotation.LogExecutionTime)")
	public Object logExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		long start = System.currentTimeMillis();

		Object proceed = proceedingJoinPoint.proceed();
		final String fullClassName = proceedingJoinPoint.getSignature().getDeclaringTypeName();
		final String methodName = proceedingJoinPoint.getSignature().getName();

		long totalTime = System.currentTimeMillis() - start;
		LogUtils.logDataBaseExecutionTime(LOGGER, fullClassName + "." + methodName, totalTime);

		return proceed;
	}

}
