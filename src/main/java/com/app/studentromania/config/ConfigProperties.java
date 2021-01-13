package com.app.studentromania.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config.properties")
@ConfigurationProperties(prefix = "app")
public class ConfigProperties {

	@Value("couchbaseHost")
	private String couchbaseHost;
	private String couchbaseBucket;
	private String couchbasePassword;

	public String getCouchbaseHost() {
		return couchbaseHost;
	}

	public void setCouchbaseHost(String couchbaseHost) {
		this.couchbaseHost = couchbaseHost;
	}

	public String getCouchbaseBucket() {
		return couchbaseBucket;
	}

	public void setCouchbaseBucket(String couchbaseBucket) {
		this.couchbaseBucket = couchbaseBucket;
	}

	public String getCouchbasePassword() {
		return couchbasePassword;
	}

	public void setCouchbasePassword(String couchbasePassword) {
		this.couchbasePassword = couchbasePassword;
	}

}
