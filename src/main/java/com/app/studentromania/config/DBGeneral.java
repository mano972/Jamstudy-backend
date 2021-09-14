package com.app.studentromania.config;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.convert.MappingCouchbaseConverter;

@Configuration
public class DBGeneral extends AbstractCouchbaseConfiguration {

	private String host;
	private String bucket; // also username
	private String password;

	@Autowired
	public DBGeneral(CouchbaseProperties couchbaseProperties) {
		host = couchbaseProperties.getCouchbaseHost();
		bucket = couchbaseProperties.getCouchbaseBucket();
		password = couchbaseProperties.getCouchbasePassword();
	}

	@Override
	protected List<String> getBootstrapHosts() {
		return Collections.singletonList(host);
	}

	@Override
	protected String getBucketName() {
		return bucket;
	}

	@Override
	protected String getBucketPassword() {
		return password;
	}

	@Override
	public String typeKey() {
		return MappingCouchbaseConverter.TYPEKEY_SYNCGATEWAY_COMPATIBLE;
	}

}
