package com.app.studentromania.dao;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.model.Config;
import com.app.studentromania.repo.ConfigRepo;

@Component
public class ConfigDAO {

	@Autowired
	private ConfigRepo configRepo;

	@LogExecutionTime
	@LogParameters
	public Optional<Config> getByConfigKey(String configKey) {
		return configRepo.findByConfigKey(configKey).stream().findFirst();
	}

	@LogExecutionTime
	@LogParameters
	public String getValueByConfigKey(String configKey, String defaultValue) {
		Optional<Config> configOpt = configRepo.findByConfigKey(configKey).stream().findFirst();
		return configOpt.isPresent() ? configOpt.get().getConfigValue() : defaultValue;
	}

	@LogExecutionTime
	@LogParameters
	public List<Config> getAllConfigs() {
		return configRepo.findAll();
	}

	public void saveConfig(Config config) {
		configRepo.save(config);
	}

	@LogExecutionTime
	public void deleteAllConfigs() {
		configRepo.deleteAll();
	}

	public String generateDocumentId(String documentPrefix) {
		String randomId = RandomStringUtils.randomAlphanumeric(10);
		return documentPrefix + "_" + randomId;
	}

}
