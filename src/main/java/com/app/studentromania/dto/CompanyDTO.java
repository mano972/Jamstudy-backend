package com.app.studentromania.dto;

import java.util.List;

import com.app.studentromania.model.CompanyJobRole;

public class CompanyDTO {

    private String companyId;

    private String companyName;

    private List<String> companyCities;

    private double minSalary;

    private double maxSalary;

    private double averageSalary;

    private List<CompanyJobRole> companyJobRoles;

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
