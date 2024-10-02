package com.app.studentromania.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.app.studentromania.annotation.Auth;
import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.dto.CompanyDTO;
import com.app.studentromania.service.CompanyService;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    @RestCall
    @Auth
    public ResponseEntity<String> getByCompanyIds(@RequestParam List<String> companyIds, @RequestParam String facultyId) {
        return companyService.getByCompanyIds(companyIds, facultyId).createRestResponse();
    }

    @PostMapping
    @RestCall
    @VerifyAdmin
    public ResponseEntity<String> createCompany(@RequestBody CompanyDTO companyDTO) {
        return companyService.createCompany(companyDTO).createRestResponse();
    }

    @PostMapping("/faculty/{facultyId}")
    @RestCall
    @VerifyAdmin
    public ResponseEntity<String> createCompaniesAndJobRolesForFaculty(@PathVariable String facultyId, @RequestBody List<CompanyDTO> companyDTOs) {
        return companyService.createCompaniesAndJobRolesForFaculty(companyDTOs, facultyId).createRestResponse();
    }

}
