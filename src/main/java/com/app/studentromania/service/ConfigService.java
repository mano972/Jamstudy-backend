package com.app.studentromania.service;

import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Config;
import com.app.studentromania.util.LogUtils;

@Service
public class ConfigService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionService.class);

	@Autowired
	private ConfigDAO configDAO;
	
	@Autowired
	private LogUtils logUtils;

	@Autowired
	public ConfigService() {
		logUtils.logMessage(LOGGER, "ConfigService initialized");
	}

	public ResponseDTO getAllConfigs() {
		List<Config> configs = configDAO.getAllConfigs();
		JSONArray configsArray = new JSONArray();
		configs.forEach(config -> {
			configsArray.put(new JSONObject(config));
		});
		JSONObject response = new JSONObject();
		response.put("configs", configsArray);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO getByConfigKey(String configKey) {
		Optional<Config> configOpt = configDAO.getByConfigKey(configKey);
		if (!configOpt.isPresent()) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.CONFIG_NOT_FOUND);
		}
		Config config = configOpt.get();
		JSONObject response = new JSONObject(config);

		return ResponseDTO.createSuccessResponse(response);
	}

	public ResponseDTO saveConfig(Config newConfig) {
		String configKey = newConfig.getConfigKey();
		Optional<Config> existingConfigOpt = configDAO.getByConfigKey(configKey);
		if (existingConfigOpt.isPresent()) {
			existingConfigOpt.get().setConfigValue(newConfig.getConfigValue());
			configDAO.saveConfig(existingConfigOpt.get());
			logUtils.logMessage(LOGGER, "Config " + configKey + "was updated!");
		} else {
			configDAO.saveConfig(newConfig);
			logUtils.logMessage(LOGGER, "Config " + configKey + "was created!");
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO saveConfigs(List<Config> newConfigs) {
		for (Config newConfig : newConfigs) {
			String configKey = newConfig.getConfigKey();
			Optional<Config> existingConfigOpt = configDAO.getByConfigKey(configKey);
			if (existingConfigOpt.isPresent()) {
				existingConfigOpt.get().setConfigValue(newConfig.getConfigValue());
				configDAO.saveConfig(existingConfigOpt.get());
				logUtils.logMessage(LOGGER, "Config " + configKey + "was updated!");
			} else {
				configDAO.saveConfig(newConfig);
				logUtils.logMessage(LOGGER, "Config " + configKey + "was created!");
			}
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO deleteAllConfigs() {
		configDAO.deleteAllConfigs();
		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

}
