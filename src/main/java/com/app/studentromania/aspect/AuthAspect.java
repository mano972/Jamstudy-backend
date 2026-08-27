package com.app.studentromania.aspect;

import com.app.studentromania.config.AuthProperties;
import com.app.studentromania.dao.EntityProfileDAO;
import com.app.studentromania.dao.UserProfileDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.EntityProfile;
import com.app.studentromania.model.UserProfile;
import com.app.studentromania.service.CustomRequestContext;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.Utilities;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAspect.class);

    @Autowired
    private UserProfileDAO userProfileDAO;

    @Autowired
    private EntityProfileDAO entityProfileDAO;

    @Autowired
    private CustomRequestContext customRequestContext;

    @Autowired
    private LogUtils logUtils;

    @Autowired
    private AuthProperties authProperties;

    @Around("@annotation(com.app.studentromania.annotation.Auth)")
    public Object auth(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        final String algorithmSecret = authProperties.getSecret();
        final String issuer = authProperties.getIssuer();

        customRequestContext.setUserId(null);
        customRequestContext.setEntityProfileId(null);
        customRequestContext.setUserProfile(null);
        customRequestContext.setTraceId(Utilities.getTraceId());

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        final String countryCode = request.getHeader(Constants.COUNTRY_CODE_TOKEN_REQUEST_HEADER);
        customRequestContext.setCountryCode(countryCode != null ? countryCode : "RO");
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
                    logUtils.logMessage(LOGGER, "Error when verifying JWT token. UserId is missing from the token");
                    return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
                }

                final String endPointName = proceedingJoinPoint.getSignature().getName();
                logUtils.logAuth(LOGGER, endPointName, userId);

                // Look the subject up as an end user first — that's the common case — and
                // only fall back to the entity-profile lookup when it isn't one, instead
                // of always paying for both N1QL queries.
                Optional<UserProfile> userProfileOpt = userProfileDAO.getByUserId(userId);
                if (userProfileOpt.isPresent()) {
                    customRequestContext.setUserId(userId);
                    customRequestContext.setUserProfile(userProfileOpt.get());
                } else {
                    Optional<EntityProfile> entityProfileOpt = entityProfileDAO.getByEntityId(userId);
                    if (entityProfileOpt.isPresent()) {
                        customRequestContext.setEntityProfileId(userId);
                    } else {
                        logUtils.logMessage(LOGGER, "Error when verifying JWT token. User/EntityProfile with id does not exist: " + userId);
                        return ResponseDTO.createErrorResponse(ErrorsEnum.USERPROFILE_NOT_FOUND);
                    }
                }

                if (isTokenExpired(decodedJwtToken)) {
                    logUtils.logMessage(LOGGER,
                            "Error when verifying JWT token. Token has expired for userId: " + userId);
                    return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_EXPIRED);
                }
            }

        } catch (JWTVerificationException e) {
            logUtils.logMessage(LOGGER, "Error when verifying JWT token ");
            return ResponseDTO.createErrorResponse(ErrorsEnum.JWT_VERIFY_ERROR).createRestResponse();
        }

        Object proceed = proceedingJoinPoint.proceed();

        return proceed;

    }

    private boolean isTokenExpired(DecodedJWT decodedJwtToken) {
        return decodedJwtToken.getExpiresAt().before(new Date());
    }

}
