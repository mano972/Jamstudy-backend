package com.app.studentromania.auth;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.config.AuthProperties;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

@Service
public class JwtAuthenticationService {

	@Autowired
	private AuthProperties authProperties;

//	public static Cookie createAuthCookie() {
//		Cookie cookie = new Cookie(name, value); //name and value of the cookie
//		cookie.setMaxAge(expire);
//		cookie.setHttpOnly(true);
//		cookie.setSecure(true);
//		cookie.setPath("/");
//		return cookie;
//	}

	public String generateJWT(String userId) {
		String jwtToken = null;
		try {
			final Integer hours = authProperties.getExpirationHours();
			final String algorithmSecret = authProperties.getSecret();
			final String issuer = authProperties.getIssuer();

			Algorithm algorithm = Algorithm.HMAC256(algorithmSecret);
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, hours);
			jwtToken = JWT.create().withIssuer(issuer).withSubject(userId).withIssuedAt(new Date())
					.withExpiresAt(cal.getTime()).sign(algorithm);
		} catch (JWTCreationException e) {
			jwtToken = null;
		}

		return jwtToken;
	}

}
