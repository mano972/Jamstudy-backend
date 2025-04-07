package com.app.studentromania.aspect;

import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.service.CustomRequestContext;
import com.app.studentromania.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
public class VerifyEntityAdminAspect {

    @Autowired
    private CustomRequestContext customRequestContext;

    @Autowired
    private LogUtils logUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyEntityAdminAspect.class);

    @Around("@annotation(com.app.studentromania.annotation.VerifyEntityAdmin)")
    public Object verifyEntityAdmin(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        String entityAdminId = customRequestContext.getEntityProfileId();
        if (StringUtils.isBlank(entityAdminId)) {
            final String endPointName = proceedingJoinPoint.getSignature().getName();
            logUtils.logMessage(LOGGER,
                    "Entity Admin is not logged in. Cannot access <" + endPointName + "> at [" + new Date() + "]");
            return ResponseDTO.createErrorResponse(ErrorsEnum.USER_NOT_LOGGED_IN);
        }

        Object proceed = proceedingJoinPoint.proceed();

        return proceed;

    }

}
