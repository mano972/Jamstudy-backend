package com.app.studentromania.config;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.convert.MappingCouchbaseConverter;

import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

@Configuration
public class DBGeneral extends AbstractCouchbaseConfiguration {

	private String host;
	private String bucket; // also username
	private String password;

	@Autowired
	public DBGeneral(CouchbaseProperties couchbaseProperties) {
		this.host = couchbaseProperties.getCouchbaseHost();
		this.bucket = couchbaseProperties.getCouchbaseBucket();
		this.password = couchbaseProperties.getCouchbasePassword();
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

	/**
	 * Short {@code disconnectTimeout} (default is 25s) so the SDK's non-daemon IO
	 * threads drain quickly when the Spring context closes — otherwise the JVM
	 * lingers after a WildFly stop and the Windows service hangs in "Stopping".
	 * Spring Data already registers {@code shutdown} as this bean's destroy method.
	 */
	@Override
	protected CouchbaseEnvironment getEnvironment() {
		return DefaultCouchbaseEnvironment.builder()
				.disconnectTimeout(TimeUnit.SECONDS.toMillis(5))
				.connectTimeout(TimeUnit.SECONDS.toMillis(10))
				.kvTimeout(TimeUnit.SECONDS.toMillis(5))
				.queryTimeout(TimeUnit.SECONDS.toMillis(30))
				.build();
	}

	@Override
	public String typeKey() {
		return MappingCouchbaseConverter.TYPEKEY_SYNCGATEWAY_COMPATIBLE;
	}

}
