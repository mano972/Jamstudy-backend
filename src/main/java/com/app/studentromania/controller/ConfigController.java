package com.app.studentromania.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.model.Config;
import com.app.studentromania.service.ConfigService;

@RestController
@RequestMapping("${base.path}/config")
public class ConfigController {

	@Autowired
	private ConfigService configService;

	@GetMapping
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> getAllConfigs() {
		return configService.getAllConfigs().createRestResponse();
	}

	@GetMapping("/{configKey}")
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> getByConfigKey(@PathVariable String configKey) {
		return configService.getByConfigKey(configKey).createRestResponse();
	}

	@PostMapping
	@RestCall
	@VerifyAdmin
	public ResponseEntity<String> saveConfig(@RequestBody Config config) {
		return configService.saveConfig(config).createRestResponse();
	}

	@PostMapping("/configs")
	@RestCall
	public ResponseEntity<String> saveConfigs(@RequestBody List<Config> configs) {
		return configService.saveConfigs(configs).createRestResponse();
	}

	@DeleteMapping
	@VerifyAdmin
	public ResponseEntity<String> deleteAllConfigs() {
		return configService.deleteAllConfigs().createRestResponse();
	}

}
