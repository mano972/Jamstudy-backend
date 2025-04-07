package com.app.studentromania.repo;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.model.EntityProfile;
import com.app.studentromania.util.Constants;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EntityProfileRepo {

    @Autowired
    private CouchbaseTemplate template;

    @Query
    @LogExecutionTime
    @LogParameters
    public List<EntityProfile> findByEntityId(String entityId) {
        String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
                + Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND entityId = $2 ";

        return template.findByN1QL(
                N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.ENTITY_PROFILE.getValue(), entityId)),
                EntityProfile.class);
    }

    @Query
    @LogExecutionTime
    @LogParameters
    public List<EntityProfile> findByUsername(String username) {
        String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
                + Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND username = $2 ";

        return template.findByN1QL(
                N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.ENTITY_PROFILE.getValue(), username)),
                EntityProfile.class);
    }

    public void save(EntityProfile entityProfile) {
        template.save(entityProfile);
    }

}
