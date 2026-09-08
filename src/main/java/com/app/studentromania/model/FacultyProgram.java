package com.app.studentromania.model;

public class FacultyProgram {

	private String programName;

	/*
	 * LICENSE or MASTER
	 */
	private String programType;

	private Integer programAvailablePlaces;

	private Integer programBudgetPlaces;

	private Integer programTaxPlaces;

	private Double lastGrade;

	private Double candidatesPerPlace;

	private String specialization;

	private String domainOfLicenseOrMaster;

	private String programDomain;

	private String admissionType;

	private String programAccreditation;

	/*
	 * Free-form text: amount + currency + unit, with an optional per-year
	 * breakdown when the tax is not flat across the study years, e.g.
	 * "5500 RON/an" or
	 * "8800 RON (anul I), 8750 RON (anul II), 8240 RON (anul III), 7550 RON (anul IV)".
	 * Was Integer; legacy documents still hold a bare number, which Jackson
	 * coerces to String on read.
	 */
	private String annualTax;

	private Integer noOfYears;

	private String programVolume;

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getProgramType() {
		return programType;
	}

	public void setProgramType(String programType) {
		this.programType = programType;
	}

	public Integer getProgramAvailablePlaces() {
		return programAvailablePlaces;
	}

	public void setProgramAvailablePlaces(Integer programAvailablePlaces) {
		this.programAvailablePlaces = programAvailablePlaces;
	}

	public Integer getProgramBudgetPlaces() {
		return programBudgetPlaces;
	}

	public void setProgramBudgetPlaces(Integer programBudgetPlaces) {
		this.programBudgetPlaces = programBudgetPlaces;
	}

	public Integer getProgramTaxPlaces() {
		return programTaxPlaces;
	}

	public void setProgramTaxPlaces(Integer programTaxPlaces) {
		this.programTaxPlaces = programTaxPlaces;
	}

	public Double getLastGrade() {
		return lastGrade;
	}

	public void setLastGrade(Double lastGrade) {
		this.lastGrade = lastGrade;
	}

	public Double getCandidatesPerPlace() {
		return candidatesPerPlace;
	}

	public void setCandidatesPerPlace(Double candidatesPerPlace) {
		this.candidatesPerPlace = candidatesPerPlace;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public String getDomainOfLicenseOrMaster() {
		return domainOfLicenseOrMaster;
	}

	public void setDomainOfLicenseOrMaster(String domainOfLicenseOrMaster) {
		this.domainOfLicenseOrMaster = domainOfLicenseOrMaster;
	}

	public String getProgramDomain() {
		return programDomain;
	}

	public void setProgramDomain(String programDomain) {
		this.programDomain = programDomain;
	}

	public String getAdmissionType() {
		return admissionType;
	}

	public void setAdmissionType(String admissionType) {
		this.admissionType = admissionType;
	}

	public String getProgramAccreditation() {
		return programAccreditation;
	}

	public void setProgramAccreditation(String programAccreditation) {
		this.programAccreditation = programAccreditation;
	}

	public String getAnnualTax() {
		return annualTax;
	}

	public void setAnnualTax(String annualTax) {
		this.annualTax = annualTax;
	}

	public Integer getNoOfYears() {
		return noOfYears;
	}

	public void setNoOfYears(Integer noOfYears) {
		this.noOfYears = noOfYears;
	}

	public String getProgramVolume() {
		return programVolume;
	}

	public void setProgramVolume(String programVolume) {
		this.programVolume = programVolume;
	}
}
