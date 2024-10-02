package com.app.studentromania.model;

import org.springframework.data.couchbase.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import com.app.studentromania.enumtype.DocTypeEnum;
import com.couchbase.client.java.repository.annotation.Field;

@Document
public class Company extends ParentEntity {

    public Company() {
        super(DocTypeEnum.COMPANY);
    }

    @Field
    private String companyId;

    @Field
    private String companyName;

    @Field
    private List<String> companyCities = new ArrayList<>();

    @Field
    private double minSalary;

    @Field
    private double maxSalary;

    @Field
    private double averageSalary;

    @Field
    private List<CompanyJobRole> companyJobRoles = new ArrayList<>();;

    public String getCompanyId() {
        return companyId;
    }
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public List<String> getCompanyCities() {
        return companyCities;
    }
    public void setCompanyCities(List<String> companyCities) {
        this.companyCities = companyCities;
    }
    public double getMinSalary() {
        return minSalary;
    }
    public void setMinSalary(double minSalary) {
        this.minSalary = minSalary;
    }
    public double getMaxSalary() {
        return maxSalary;
    }
    public void setMaxSalary(double maxSalary) {
        this.maxSalary = maxSalary;
    }
    public double getAverageSalary() {
        return averageSalary;
    }
    public void setAverageSalary(double averageSalary) {
        this.averageSalary = averageSalary;
    }
    public List<CompanyJobRole> getCompanyJobRoles() {
        return companyJobRoles;
    }
    public void setCompanyJobRoles(List<CompanyJobRole> companyJobRoles) {
        this.companyJobRoles = companyJobRoles;
    }
}
