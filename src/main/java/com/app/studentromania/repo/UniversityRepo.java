package com.app.studentromania.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.stereotype.Repository;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.model.University;
import com.app.studentromania.util.Constants;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;

@Repository
public class UniversityRepo {

	@Autowired
	private CouchbaseTemplate template;

	@Query
	@LogExecutionTime
	@LogParameters
	public List<University> findAll() {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.UNIVERSITY.getValue())),
				University.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<University> findByUniversityId(String universityId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET
				+ " WHERE docType = $1 AND universityId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.UNIVERSITY.getValue(), universityId)),
				University.class);
	}

	public void save(University university) {
		template.save(university);
	}

	@LogExecutionTime
	public void deleteAll() {
		template.remove(findAll());
	}

}
