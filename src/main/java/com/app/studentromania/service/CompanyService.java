package com.app.studentromania.service;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.app.studentromania.dao.CompanyDAO;
import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dto.CompanyDTO;
import com.app.studentromania.dto.CompanyResponseDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Company;
import com.app.studentromania.model.CompanyJobRole;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.util.Constants;

@Service
public class CompanyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

    @Autowired
    private CompanyDAO companyDAO;

    @Autowired
    private FacultyDAO facultyDAO;

    @Autowired
    private ConfigDAO configDAO;

    public ResponseDTO getByCompanyIds(List<String> companyIds, String facultyId) {
        List<Company> companies = companyDAO.getByCompanyIds(companyIds);
        JSONArray companiesArray = new JSONArray();
        companies.forEach(company -> {
            CompanyResponseDTO companyResponseDTO = mapToCompanyResponseDTO(company);
            if (StringUtils.isNotEmpty(facultyId)) {
                companyResponseDTO.getCompanyJobRoles().removeIf(jr -> !jr.getFacultyIds().contains(facultyId));
            }
            companiesArray.put(new JSONObject(companyResponseDTO));
        });
        JSONObject response = new JSONObject();
        response.put("companies", companiesArray);

        return ResponseDTO.createSuccessResponse(response);
    }

    public ResponseDTO createCompany(CompanyDTO companyDTO) {
        Company company = new Company();
        String generatedId = configDAO.generateDocumentId(Constants.COMPANY_PREFIX_ID);
        company.setCompanyId(generatedId);
        mapToCompany(companyDTO, company);
        companyDAO.createCompany(company);
        CompanyResponseDTO companyResponseDTO = new CompanyResponseDTO();
        companyResponseDTO.setCompanyId(company.getCompanyId());
        companyResponseDTO.setCompanyName(company.getCompanyName());

        return ResponseDTO.createSuccessResponse(new JSONObject(companyResponseDTO));
    }

    public ResponseDTO createCompaniesAndJobRolesForFaculty(List<CompanyDTO> companyDTOs, String facultyId) {
        JSONArray companiesArray = new JSONArray();
        Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
        if (!facultyOpt.isPresent()) {
            return ResponseDTO.createErrorResponse(ErrorsEnum.FACULTY_NOT_FOUND);
        }
        Faculty faculty = facultyOpt.get();
        companyDTOs.forEach(companyDTO -> {
            Optional<Company> companyOpt = companyDAO.getByCompanyName(companyDTO.getCompanyName());
            Company company;
            if (!companyOpt.isPresent()) {
                company = new Company();
                String generatedId = configDAO.generateDocumentId(Constants.COMPANY_PREFIX_ID);
                company.setCompanyId(generatedId);
                mapToCompany(companyDTO, company);
                company.getCompanyJobRoles().forEach(jr -> {
                    if (!jr.getFacultyIds().contains(facultyId)) {
                        jr.getFacultyIds().add(facultyId);
                    }
                });
            } else {
                company = companyOpt.get();
                createJobRoleIfNotExists(company.getCompanyJobRoles(), companyDTO.getCompanyJobRoles());
                company.getCompanyJobRoles()
                        .forEach(existingJr -> companyDTO.getCompanyJobRoles()
                                .forEach(incomingJr -> {
                                    if (StringUtils.equals(existingJr.getJobRoleName(), incomingJr.getJobRoleName()) && (!existingJr.getFacultyIds().contains(facultyId))) {
                                        existingJr.getFacultyIds().add(facultyId);
                                    }
                                }));
            }
            if (!faculty.getCompanyIds().contains(company.getCompanyId())) {
                faculty.getCompanyIds().add(company.getCompanyId());
                facultyDAO.updateFaculty(faculty);
            }
            companyDAO.updateCompany(company);
            CompanyResponseDTO companyResponseDTO = mapToCompanyResponseDTO(company);
            companiesArray.put(new JSONObject(companyResponseDTO));
        });
        JSONObject response = new JSONObject();
        response.put("companies", companiesArray);

        return ResponseDTO.createSuccessResponse(response);
    }

    private void mapToCompany(CompanyDTO companyDTO, Company company) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.map(companyDTO, company);
    }

    private CompanyResponseDTO mapToCompanyResponseDTO(Company company) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper.map(company, CompanyResponseDTO.class);
    }

    private void createJobRoleIfNotExists(List<CompanyJobRole> existingJobRoles, List<CompanyJobRole> incomingJobRoles) {
        List<String> existingJobRoleNames = existingJobRoles.stream()
                .map(CompanyJobRole::getJobRoleName)
                .collect(Collectors.toList());
        incomingJobRoles.stream()
                .filter(incomingJr -> !existingJobRoleNames.contains(incomingJr.getJobRoleName()))
                .forEach(existingJobRoles::add);
    }

}
