package com.app.studentromania.repo;

import java.util.List;

import org.springframework.data.couchbase.repository.CouchbasePagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.app.studentromania.model.Config;

@Repository
public interface ConfigRepo extends CouchbasePagingAndSortingRepository<Config, String> {

	public List<Config> findAll();

	public List<Config> findByConfigKey(String configKey);

}
