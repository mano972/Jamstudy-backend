package com.app.studentromania;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableCouchbaseRepositories
@EnableAutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public class JamstudyApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(JamstudyApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(JamstudyApplication.class);
	}

	@RestController
	class HelloController {

		@RequestMapping("/hello/{name}")
		String hello(@PathVariable String name) {

			return "Hi " + name + "!!! asdadsa";

		}
	}

}
