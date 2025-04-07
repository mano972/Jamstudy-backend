package com.app.studentromania.model;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

import java.util.Date;

public class EntityProfile extends ParentEntity {

    public EntityProfile() {
        super(DocTypeEnum.ENTITY_PROFILE);
    }

    @Field
    private String entityId;

    @Field
    private String facultyId;

    @Field
    private String universityId;

    @Field
    private String entityType;

    @Field
    private String username;

    @Field
    private String password;

    @Field
    private Date lastLogin;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getUniversityId() {
        return universityId;
    }

    public void setUniversityId(String universityId) {
        this.universityId = universityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
