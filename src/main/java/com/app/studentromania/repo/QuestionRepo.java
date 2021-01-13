package com.app.studentromania.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.stereotype.Repository;

import com.app.studentromania.annotation.LogExecutionTime;
import com.app.studentromania.annotation.LogParameters;
import com.app.studentromania.enumtype.DocTypeEnum;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.Question;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.QuestionFilter;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;

@Repository
public class QuestionRepo {

	@Autowired
	private CouchbaseTemplate template;

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Faculty> findAll() {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";

		return template.findByN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.FACULTY.getValue())),
				Faculty.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public List<Question> findByQuestionId(String questionId) {
		String query = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 ";

		return template.findByN1QL(
				N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.QUESTION.getValue(), questionId)),
				Question.class);
	}

	// folositor daca tinem Q si A in acelasi document
	@Query
	@LogExecutionTime
	@LogParameters
	public List<Question> getFilteredQuestionsByFacultyId(String facultyId, QuestionFilter questionFilter) {
		String selectStatement = "SELECT " + Constants.BUCKET + ".*, meta(" + Constants.BUCKET + ").cas AS _CAS, meta("
				+ Constants.BUCKET + ").id AS _ID FROM " + Constants.BUCKET + " WHERE docType = $1 and facultyId = $2 ";
		String paginationStatement = " offset $3 limit $4 ";
		String query = selectStatement + questionFilter.getOrderByClause() + paginationStatement;

		return template
				.findByN1QL(
						N1qlQuery
								.parameterized(query,
										JsonArray.from(DocTypeEnum.QUESTION.getValue(), facultyId,
												questionFilter.getOffset(), questionFilter.getLimit())),
						Question.class);
	}

	@Query
	@LogExecutionTime
	@LogParameters
	public long countByFacultyId(String facultyId) {
		String query = "SELECT count(*) as countResults FROM " + Constants.BUCKET
				+ " WHERE docType = $1 AND facultyId = $2 ";

		N1qlQueryResult result = template
				.queryN1QL(N1qlQuery.parameterized(query, JsonArray.from(DocTypeEnum.QUESTION.getValue(), facultyId)));
		long count = result.allRows().get(0).value().getLong("countResults");

		return count;
	}

	public void save(Question question) {
		template.save(question);
	}

	@LogExecutionTime
	public void deleteAll() {
		template.remove(findAll());
	}

	// folositor daca tinem Q si A in documente diferite
//	@Query
//	@LogExecutionTime
//	@LogParameters
//	public List<Question> getFilteredByFacultyId(String facultyId, QuestionFilter questionFilter) {
//		String selectStatement = "SELECT q.questionId, q.facultyId, q.questionText, q.useful, q.questionDate, array_agg(a) AS answers FROM "
//				+ Constants.BUCKET + " AS q LEFT JOIN " + Constants.BUCKET
//				+ " AS a ON q.questionId = a.questionId AND a.docType = $1 WHERE q.docType = $2 AND q.facultyId = $3 ";
//		String groupByStatement = " GROUP BY q.questionId, q.facultyId, q.questionText, q.useful, q.questionDate ";
//		String paginationStatement = " offset $4 limit $5 ";
//		String query = selectStatement + groupByStatement + questionFilter.getOrderByClause() + paginationStatement;
//
//		N1qlQueryResult result = template.queryN1QL(N1qlQuery.parameterized(query,
//				JsonArray.from(DocTypeEnum.ANSWER.getValue(), DocTypeEnum.QUESTION.getValue(), facultyId,
//						questionFilter.getOffset(), questionFilter.getLimit())));
//
//		List<Question> questions = mapResultToQuestion(result);
//
//		return questions;
//	}

}
