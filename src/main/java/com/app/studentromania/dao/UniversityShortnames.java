package com.app.studentromania.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.studentromania.model.University;

/**
 * universityId -> universityShortname (UNIVERSITY docs), for page titles and hub
 * labels. Faculty docs only carry the university's name, not its shortname, so
 * the faculty page / hubs look it up here. Cached for 10 minutes, like the hub
 * faculty list - a shortname edited in the DB shows up within that window.
 */
@Component
public class UniversityShortnames {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniversityShortnames.class);
    private static final long CACHE_MILLIS = 10 * 60 * 1000L;

    @Autowired
    private UniversityDAO universityDAO;

    private volatile Map<String, String> byId;
    private volatile long loadedAt;

    /** The university's shortname, or null when it has none (or the lookup failed). */
    public String forUniversityId(String universityId) {
        if (StringUtils.isBlank(universityId)) {
            return null;
        }
        return all().get(universityId);
    }

    private Map<String, String> all() {
        long now = System.currentTimeMillis();
        Map<String, String> cached = byId;
        if (cached == null || now - loadedAt > CACHE_MILLIS) {
            try {
                Map<String, String> fresh = new HashMap<>();
                for (University u : universityDAO.getAllUniversities()) {
                    if (StringUtils.isNotBlank(u.getUniversityId()) && StringUtils.isNotBlank(u.getUniversityShortname())) {
                        fresh.put(u.getUniversityId(), u.getUniversityShortname().trim());
                    }
                }
                cached = Collections.unmodifiableMap(fresh);
            } catch (RuntimeException e) {
                // Titles fall back to the acronym in the university name.
                LOGGER.warn("Could not load university shortnames: {}", e.getMessage());
                cached = cached != null ? cached : Collections.emptyMap();
            }
            byId = cached;
            loadedAt = now;
        }
        return cached;
    }
}
