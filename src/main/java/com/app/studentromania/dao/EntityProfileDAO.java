package com.app.studentromania.dao;

import com.app.studentromania.model.EntityProfile;
import com.app.studentromania.repo.EntityProfileRepo;
import com.app.studentromania.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EntityProfileDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityProfileDAO.class);

    @Autowired
    private EntityProfileRepo entityProfileRepo;

    @Autowired
    private LogUtils logUtils;

    public Optional<EntityProfile> getByEntityId(String entityId) {
        return entityProfileRepo.findByEntityId(entityId).stream().findFirst();
    }

    public Optional<EntityProfile> getByUsername(String username) {
        return entityProfileRepo.findByUsername(username).stream().findFirst();
    }

    public void createEntityProfile(EntityProfile entityProfile) {
        entityProfileRepo.save(entityProfile);
        logUtils.logMessage(LOGGER, "Entity Profile " + entityProfile.getEntityId() + " was created");
    }

}
