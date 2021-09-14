package com.app.studentromania.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.convert.MappingCouchbaseConverter;

@Configuration
public class DBGeneral extends AbstractCouchbaseConfiguration {

	@Autowired
	private CouchbaseProperties couchbaseProperties;

	private final String HOST = couchbaseProperties.getCouchbaseHost();
	private final String BUCKET = couchbaseProperties.getCouchbaseBucket(); // also username
	private final String PASSWORD = couchbaseProperties.getCouchbasePassword();

	@Override
	protected List<String> getBootstrapHosts() {
		return new ArrayList<String>(Arrays.asList(HOST));
	}

	@Override
	protected String getBucketName() {
		return BUCKET;
	}

	@Override
	protected String getBucketPassword() {
		return PASSWORD;
	}

	@Override
	public String typeKey() {
		return MappingCouchbaseConverter.TYPEKEY_SYNCGATEWAY_COMPATIBLE;
	}

}
