package com.app.studentromania.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.slf4j.Logger;

public class LogUtils {

	public static void logMessage(Logger logger, String message) {
		logger.info(message);
	}
	
	public static void logAuth(Logger logger, String endPointName, String userId) {
		logger.info("*** [" + userId + "] Authenticating for <" + endPointName + "> at [" + new Date() + "]");
	}

	public static void logStart(Logger logger, String endPointName) {
		logStart(logger, endPointName, "visitor");
	}

	public static void logStart(Logger logger, String endPointName, String userId) {
		logger.info("*** [" + userId + "] Starting <" + endPointName + "> at [" + new Date() + "]");
	}

	public static void logSuccess(Logger logger, String endPointName) {
		logSuccess(logger, endPointName, "visitor");
	}

	public static void logSuccess(Logger logger, String endPointName, String userId) {
		logger.info("*** [" + userId + "]  Finished with success <" + endPointName + "> at [" + new Date() + "]");
	}

	public static void logError(Logger logger, String endPointName, Throwable e) {
		logError(logger, endPointName, e, "visitor");
	}

	public static void logError(Logger logger, String endPointName, Throwable e, String userId) {
		logger.error("*** [" + userId + "] Failed to run <" + endPointName + "> at [" + new Date() + "]", e);
	}

	public static void logExecutionTime(Logger logger, String endPointName, long totalTime) {
		logger.info(">>> Execution time for <" + endPointName + "> is " + getDurationInSeconds(totalTime) + " seconds");
	}

	public static void logDataBaseExecutionTime(Logger logger, String methodName, long totalTime) {
		logger.info(">>> Execution time for database call <" + methodName + "> is " + getDurationInSeconds(totalTime)
				+ " seconds");
	}

	public static void logEndPointExecutionTime(Logger logger, String endPointName, long totalTime) {
		logEndPointExecutionTime(logger, endPointName, totalTime, "visitor");
	}

	public static void logEndPointExecutionTime(Logger logger, String endPointName, long totalTime, String userId) {
		logger.info(">>> *** [" + userId + "] Execution time for endpoint <" + endPointName + "> is "
				+ getDurationInSeconds(totalTime) + " seconds");
	}

	public static void logParameter(Logger logger, String paramaterName, String parameterValue) {
		logger.info(">>> Input parameter: " + paramaterName + " = [" + parameterValue + "]");
	}

	private static String getDurationInSeconds(long duration) {
		NumberFormat numberFormat = new DecimalFormat("#0.00");
		return numberFormat.format(duration / 1000d);
	}

}
