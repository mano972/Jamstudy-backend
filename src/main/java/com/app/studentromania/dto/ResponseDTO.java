package com.app.studentromania.dto;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.app.studentromania.enumtype.ErrorsEnum;

public class ResponseDTO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseDTO.class);

	private JSONObject jsonObject;
	private HttpStatus httpStatus;

	byte[] media;

	public static final String RESULT = "result";
	public static final String ERROR_CODE = "errorCode";
	public static final String ERROR_DESCRIPTION = "errorDescription";
	public static final String DATE = "date";

	public static final String CONTROL = "control";
	public static final int SUCCESS_CODE = 0;

	public static final JSONObject JSON_SUCCESS = new JSONObject("{\"success\":true}");
	public static final JSONObject JSON_FAIL = new JSONObject("{\"success\":false}");

	public ResponseEntity<String> createRestResponse() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		return new ResponseEntity<>(this.getJsonObject().toString(), headers, this.getHttpStatus());
	}

	public ResponseEntity<byte[]> createMediaRestResponse() {
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		return new ResponseEntity<>(this.getMedia(), headers, this.getHttpStatus());
	}

	public ResponseEntity<byte[]> createCacheableMediaRestResponse(long maxAgeSeconds) {
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl(CacheControl.maxAge(maxAgeSeconds, TimeUnit.SECONDS).cachePublic().getHeaderValue());
		return new ResponseEntity<>(this.getMedia(), headers, this.getHttpStatus());
	}

	public static ResponseDTO createSuccessResponse(JSONObject jsonObject) {
		ResponseDTO responseDTO = new ResponseDTO();
		responseDTO.setHttpStatus(HttpStatus.OK);
		responseDTO.setJsonObject(createResponse(jsonObject, SUCCESS_CODE, null));

		return responseDTO;
	}

	public static ResponseDTO createMediaSuccessResponse(byte[] byteArray) {
		ResponseDTO responseDTO = new ResponseDTO();
		responseDTO.setHttpStatus(HttpStatus.OK);
		responseDTO.setMedia(byteArray);

		return responseDTO;
	}

	public static ResponseDTO createErrorResponse(ErrorsEnum error) {
		ResponseDTO responseDTO = new ResponseDTO();
		responseDTO.setHttpStatus(error.getHttpStatus());
		responseDTO.setJsonObject(createResponse(JSON_FAIL, error.getErrorCode(), error.getErrorDescription()));

		logError(error);
		return responseDTO;
	}

	private static JSONObject createResponse(JSONObject jsonObject, int errorCode, String errorDescription) {
		JSONObject controlJsonObject = new JSONObject();
		controlJsonObject.put(ERROR_DESCRIPTION, errorDescription);
		controlJsonObject.put(ERROR_CODE, errorCode);
		controlJsonObject.put(DATE, new Date());

		JSONObject response = new JSONObject();
		response.put(RESULT, jsonObject);
		response.put(CONTROL, controlJsonObject);

		return response;
	}

	private static void logError(ErrorsEnum error) {
		LOGGER.info("-> Error returned: " + error.getErrorDescription() + "[" + error.getErrorCode() + "]");
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public byte[] getMedia() {
		return media;
	}

	public void setMedia(byte[] media) {
		this.media = media;
	}

}
