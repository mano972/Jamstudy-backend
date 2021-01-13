package com.app.studentromania.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.stereotype.Repository;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.model.UserProfile;
import com.app.studentromania.util.Constants;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;

@Repository
public class UserProfileRepo {

	@Autowired
	private CouchbaseTemplate template;

	@Query
	@LogExecutionTime
	@LogParameters
	public List<UserProfile> findAll() {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.USER_PROFILE.getValue())),
				UserProfile.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<UserProfile> findByUserId(String userId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND userId = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.USER_PROFILE.getValue(), userId)),
				UserProfile.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<UserProfile> findByUserName(String userName) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND userName = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.USER_PROFILE.getValue(), userName)),
				UserProfile.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<UserProfile> findByEmail(String email) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 AND email = $2 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.USER_PROFILE.getValue(), email)),
				UserProfile.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<UserProfile> findUsersToNotify() {
		String query = "SELECT u.*, meta(u).cas AS _CAS, meta(u).id AS _ID FROM " + Constants.BUCKET
				+ " AS u WHERE u.docType = $1 AND ANY n IN u.favoriteFaculties SATISFIES n.allowNotification = true END ";

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.USER_PROFILE.getValue())),
				UserProfile.class);
	}

	public void save(UserProfile userProfile) {
		template.save(userProfile);
	}

	@LogExecutionTime
	public void deleteAll() {
		template.remove(findAll());
	}

}
