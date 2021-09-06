package com.app.studentromania.aspect;

import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.app.studentromania.dao.UserProfileDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.UserProfile;
import com.app.studentromania.service.CustomRequestContext;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JWTAuthAspect {

	@Autowired
	private UserProfileDAO userProfileDAO;

	@Autowired
	private CustomRequestContext customRequestContext;

	private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthAspect.class);

	@Around("@annotation(com.app.studentromania.annotation.JWTAuth)")
	public Object auth(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		final String algorithmSecret = "amFtc3R1ZHlzZWNyZXQ";
		final String issuer = "unistart";

		customRequestContext.setUserId(null);

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();

		final String jwtAuthHeader = request.getHeader(Constants.JWT_TOKEN_REQUEST_HEADER);

		try {
			if (!StringUtils.isBlank(jwtAuthHeader) && jwtAuthHeader.startsWith("Bearer")) {

				String jwtToken = jwtAuthHeader.replace("Bearer", "").trim();

				Algorithm algorithm = Algorithm.HMAC256(algorithmSecret);
				JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
				verifier.verify(jwtToken);

				DecodedJWT decodedJwtToken = JWT.decode(jwtToken);
				String userId = decodedJwtToken.getSubject();
				if (StringUtils.isBlank(userId)) {
					LogUtils.logMessage(LOGGER, "Error when verifying JWT token. UserId is missing from the token");
					return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
				}
				
				final String endPointName = proceedingJoinPoint.getSignature().getName();
				LogUtils.logAuth(LOGGER, endPointName, userId);
				
				Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
				if (!userProfileOpt.isPresent()) {
					LogUtils.logMessage(LOGGER, "Error when verifying JWT token. UserId does not exist: " + userId);
					return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
				}

				if (isTokenExpired(decodedJwtToken)) {
					LogUtils.logMessage(LOGGER,
							"Error when verifying JWT token. Token has expired for userId: " + userId);
					return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_EXPIRED);
				}

				customRequestContext.setUserId(userId);
			}

		} catch (JWTVerificationException e) {
			LogUtils.logMessage(LOGGER, "Error when verifying JWT token ");
			return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_VERIFY_ERROR).createRestResponse();
		}

		Object proceed = proceedingJoinPoint.proceed();

		return proceed;

	}

	private boolean isTokenExpired(DecodedJWT decodedJwtToken) {
		return decodedJwtToken.getExpiresAt().before(new Date());
	}

}
