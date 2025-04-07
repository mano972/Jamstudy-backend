package com.app.studentromania.controller;

import com.app.studentromania.annotation.Auth;
import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.annotation.VerifyEntityAdmin;
import com.app.studentromania.dto.EntityProfileDTO;
import com.app.studentromania.service.EntityProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/entityprofile")
public class EntityProfileController {

    @Autowired
    private EntityProfileService entityProfileService;

    @PostMapping()
    @RestCall
    @VerifyAdmin
    public ResponseEntity<String> createEntityProfile(@RequestBody EntityProfileDTO entityProfileDTO) {
        return entityProfileService.createEntityProfile(entityProfileDTO).createRestResponse();
    }

    @PostMapping("/login")
    @RestCall
    public ResponseEntity<String> login(@RequestBody EntityProfileDTO entityProfileDTO) {
        return entityProfileService.login(entityProfileDTO).createRestResponse();
    }

    @GetMapping("/faculty")
    @RestCall
    @Auth
    @VerifyEntityAdmin
    public ResponseEntity<String> getFaculty() {
        return entityProfileService.getEntityAdminFaculty().createRestResponse();
    }

    @GetMapping("/reviews")
    @RestCall
    @Auth
    @VerifyEntityAdmin
    public ResponseEntity<String> getReviews() {
        return entityProfileService.getEntityAdminReviews().createRestResponse();
    }

}
