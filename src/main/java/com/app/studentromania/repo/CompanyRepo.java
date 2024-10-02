package com.app.studentromania.repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.model.Company;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.util.Constants;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;

@Repository
public class CompanyRepo {

    @Autowired
    private CouchbaseTemplate template;

    @Query
    @LogExecutionTime
    @LogParameters
    public List<Company> findByCompanyId(String companyId) {
        String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
                + Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND companyId = $2 ";

        return template.findByN1QL(
                N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.COMPANY.getValue(), companyId)),
                Company.class);
    }

    @Query
    @LogExecutionTime
    @LogParameters
    public List<Company> findByCompanyIds(List<String> companyIds) {
        String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
                + Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND companyId IN " + JsonArray.from(companyIds);

        return template.findByN1QL(
                N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.COMPANY.getValue())),
                Company.class);
    }

    public void save(Company company) {
        template.save(company);
    }

    @Query
    @LogExecutionTime
    @LogParameters
    public List<Company> findByCompanyName(String companyName) {
        String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
                + Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND companyName = $2 ";

        return template.findByN1QL(
                N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.COMPANY.getValue(), companyName)),
                Company.class);
    }

}
