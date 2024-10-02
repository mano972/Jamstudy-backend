package com.app.studentromania.model;

import java.util.ArrayList;
import java.util.List;
public class CompanyJobRole {

    private String jobRoleName;

    private double minSalary;

    private double maxSalary;

    private double averageSalary;

    private List<String> facultyIds = new ArrayList<>();;

    public String getJobRoleName() {
        return jobRoleName;
    }
    public void setJobRoleName(String jobRoleName) {
        this.jobRoleName = jobRoleName;
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
    public List<String> getFacultyIds() {
        return facultyIds;
    }
    public void setFacultyIds(List<String> facultyIds) {
        this.facultyIds = facultyIds;
    }
}
