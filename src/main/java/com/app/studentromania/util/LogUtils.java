package com.app.studentromania.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.service.CustomRequestContext;

@Component
public class LogUtils {

	@Autowired(required = false)
	private CustomRequestContext customRequestContext;

	public void logMessage(Logger logger, String message) {
		logger.info(message + ". TraceId [" + getTraceId() + "]");
	}

	public void logMessageNoRequest(Logger logger, String message) {
		logger.info(message);
	}

	public void logAuth(Logger logger, String endPointName, String userId) {
		logger.info("*** [" + userId + "] Authorizing for <" + endPointName + "> at [" + new Date() + "]"
				+ ". TraceId [" + getTraceId() + "]");
	}

	public void logStart(Logger logger, String endPointName) {
		logger.info("*** [" + getUserId() + "] Starting <" + endPointName + "> at [" + new Date() + "]" + ". TraceId ["
				+ getTraceId() + "]");
	}

	public void logSuccess(Logger logger, String endPointName) {
		logger.info("*** [" + getUserId() + "]  Finished with success <" + endPointName + "> at [" + new Date() + "]"
				+ ". TraceId [" + getTraceId() + "]");
	}

	public void logError(Logger logger, String endPointName, Throwable e) {
		logger.error("*** [" + getUserId() + "] Failed to run <" + endPointName + "> at [" + new Date() + "]"
				+ ". TraceId [" + getTraceId() + "]", e);
	}

	public void logExecutionTime(Logger logger, String endPointName, long totalTime) {
		logger.info(">>> Execution time for <" + endPointName + "> is " + getDurationInSeconds(totalTime) + " seconds"
				+ ". TraceId [" + getTraceId() + "]");
	}

	public void logDataBaseExecutionTime(Logger logger, String methodName, long totalTime) {
		logger.info(">>> Execution time for database call <" + methodName + "> is " + getDurationInSeconds(totalTime)
				+ " seconds" + ". TraceId [" + getTraceId() + "]");
	}

	public void logEndPointExecutionTime(Logger logger, String endPointName, long totalTime) {
		logger.info(">>> *** [" + getUserId() + "] Execution time for endpoint <" + endPointName + "> is "
				+ getDurationInSeconds(totalTime) + " seconds" + ". TraceId [" + getTraceId() + "]");
	}

	public void logParameter(Logger logger, String paramaterName, String parameterValue) {
		logger.info(">>> Input parameter: " + paramaterName + " = [" + parameterValue + "]");
	}

	private String getDurationInSeconds(long duration) {
		NumberFormat numberFormat = new DecimalFormat("#0.00");
		return numberFormat.format(duration / 1000d);
	}

	private String getUserId() {
		if (StringUtils.isNotBlank(customRequestContext.getUserId())) {
			return customRequestContext.getUserId();
		} else {
			return "visitor";
		}
	}

	private String getTraceId() {
		return customRequestContext.getTraceId();
	}

}
