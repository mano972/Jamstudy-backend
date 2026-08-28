package com.app.studentromania.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;

/**
 * Tears the Couchbase client down, in order and with hard timeouts, when the
 * Spring context closes (WildFly undeploy / shutdown).
 *
 * The SDK's IO threads are non-daemon: if they aren't stopped the JVM never
 * exits and the Windows service hangs in "Stopping". Spring Data also registers
 * destroy methods for the bucket / cluster / environment beans, but the ordering
 * during a container undeploy isn't guaranteed and the environment's default
 * disconnect timeout is long. Doing it here first, bounded, makes shutdown
 * deterministic; the framework's later destroy calls then no-op.
 */
@Component
public class CouchbaseLifecycle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseLifecycle.class);
	private static final long TIMEOUT_SECONDS = 5;

	@Autowired
	private CouchbaseTemplate couchbaseTemplate;

	@Autowired(required = false)
	private Cluster cluster;

	@Autowired(required = false)
	private CouchbaseEnvironment environment;

	@PreDestroy
	public void shutdown() {
		close("bucket", () -> couchbaseTemplate.getCouchbaseBucket().close(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		if (cluster != null) {
			close("cluster", () -> cluster.disconnect(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		}
		if (environment != null) {
			close("environment", () -> environment.shutdown(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		}
	}

	private void close(String what, Runnable action) {
		try {
			action.run();
			LOGGER.info("Couchbase {} closed", what);
		} catch (RuntimeException e) {
			LOGGER.warn("Couchbase {} did not close cleanly: {}", what, e.toString());
		}
	}
}
