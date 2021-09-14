package com.app.studentromania.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

	private int expirationHours;
	private String secret;
	private String issuer;

	public int getExpirationHours() {
		return expirationHours;
	}

	public void setExpirationHours(int expirationHours) {
		this.expirationHours = expirationHours;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

}
