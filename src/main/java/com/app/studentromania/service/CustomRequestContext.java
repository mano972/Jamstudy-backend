package com.app.studentromania.service;

import com.app.studentromania.model.UserProfile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CustomRequestContext {

    private String userId;

    private String entityProfileId;

    private String traceId;

    private String countryCode;

    /**
     * The end user's profile, loaded once by {@code AuthAspect} while verifying the
     * token. Request-scoped, so it never leaks past this request. It's an OPTIONAL
     * cache: {@code null} for anonymous calls and for entity-admin tokens. A handler
     * may reuse it to skip a redundant {@code getByUserId} lookup, but must fall back
     * to {@code UserProfileDAO} when it's null, and must not assume a value cached
     * here reflects a copy that other code in the same request loaded separately.
     */
    private UserProfile userProfile;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntityProfileId() {
        return entityProfileId;
    }

    public void setEntityProfileId(String entityProfileId) {
        this.entityProfileId = entityProfileId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
