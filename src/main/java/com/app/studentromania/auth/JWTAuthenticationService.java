package com.app.studentromania.auth;

import java.util.Calendar;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

public class JWTAuthenticationService {
	
//	public static Cookie createAuthCookie() {
//		Cookie cookie = new Cookie(name, value); //name and value of the cookie
//		cookie.setMaxAge(expire); //expire could be 60 (seconds)
//		cookie.setHttpOnly(true);
//		cookie.setSecure(true);
//		cookie.setPath("/");
//		return cookie;
//	}

	public static String generateJWT(String email) {
		String jwtToken = null;
		try {
			final Integer hours = 72;
			final String algorithmSecret = "amFtc3R1ZHlzZWNyZXQ";
			final String issuer = "unistart";

			Algorithm algorithm = Algorithm.HMAC256(algorithmSecret);
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR_OF_DAY, hours);
			jwtToken = JWT.create().withIssuer(issuer).withSubject(email).withIssuedAt(new Date())
					.withExpiresAt(cal.getTime()).sign(algorithm);
		} catch (JWTCreationException e) {
			jwtToken = null;
		}

		return jwtToken;
	}

}
