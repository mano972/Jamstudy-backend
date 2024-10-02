package com.app.studentromania.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import com.app.studentromania.model.Company;
import com.app.studentromania.repo.CompanyRepo;
import com.app.studentromania.util.LogUtils;

@Component
public class CompanyDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyDAO.class);

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private LogUtils logUtils;

    public Optional<Company> getByCompanyId(String companyId) {
        return companyRepo.findByCompanyId(companyId).stream().findFirst();
    }

    public List<Company> getByCompanyIds(List<String> companyIds) {
        return companyRepo.findByCompanyIds(companyIds);
    }

    public Optional<Company> getByCompanyName(String companyName) {
        return companyRepo.findByCompanyName(companyName).stream().findFirst();
    }

    public void createCompany(Company company) {
        companyRepo.save(company);
        logUtils.logMessage(LOGGER, "Company " + company.getCompanyId() + " was created!");
    }

    public void updateCompany(Company company) {
        company.setUpdateDateWithCurrentDate();
        companyRepo.save(company);
        logUtils.logMessage(LOGGER, "Company " + company.getCompanyId() + " was updated!");
    }

}
