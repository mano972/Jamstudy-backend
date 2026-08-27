package com.app.studentromania.controller;

import com.app.studentromania.annotation.Auth;
import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.annotation.VerifyEntityAdmin;
import com.app.studentromania.dto.FacultyDTO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.service.FacultyService;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.FacultyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin()
@RestController
@RequestMapping("${base.path}/faculty")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @GetMapping
    @RestCall
    @Auth
    public ResponseEntity<String> getFilteredFaculties(FacultyFilter facultyFilter) {
        return facultyService.getFilteredFaculties(facultyFilter).createRestResponse();
    }

    @GetMapping("/details")
    @RestCall
    @Auth
    public ResponseEntity<String> getFilteredFacultiesDetailed(FacultyFilter facultyFilter) {
        return facultyService.getFilteredFacultiesDetailed(facultyFilter).createRestResponse();
    }

    @GetMapping("/universityfilter")
    @RestCall
    @Auth
    public ResponseEntity<String> getUniversitiesForFilter(FacultyFilter facultyFilter) {
        return facultyService.getUniversitiesForFilter(facultyFilter).createRestResponse();
    }

    @GetMapping("/cityfilter")
    @RestCall
    @Auth
    public ResponseEntity<String> getFacultyCitiesForFilter(FacultyFilter facultyFilter) {
        return facultyService.getFacultyCitiesForFilter(facultyFilter).createRestResponse();
    }

    @GetMapping("/{facultyId}")
    @RestCall
    @Auth
    public ResponseEntity<String> getByFacultyId(@PathVariable String facultyId) {
        return facultyService.getByFacultyId(facultyId).createRestResponse();
    }

    @GetMapping("/university/{universityId}")
    @RestCall
    @Auth
    public ResponseEntity<String> getByUniversityId(@PathVariable String universityId) {
        return facultyService.getByUniversityId(universityId).createRestResponse();
    }

    @GetMapping("/{facultyId}/reviewdetails")
    @RestCall
    @Auth
    public ResponseEntity<String> getFacultyReviewDetails(@PathVariable String facultyId) {
        return facultyService.getFacultyReviewsDetails(facultyId).createRestResponse();
    }

    @GetMapping(value = "/{facultyId}/logo", produces = MediaType.IMAGE_PNG_VALUE)
    @RestCall
    public @ResponseBody ResponseEntity<?> getFacultyLogo(@PathVariable String facultyId) throws IOException {
        ResponseDTO responseDTO = facultyService.getFacultyLogo(facultyId);
        if (responseDTO.getJsonObject() != null) {
            return responseDTO.createRestResponse();
        }
        return responseDTO.createCacheableMediaRestResponse(Constants.FACULTY_LOGO_CACHE_MAX_AGE_SECONDS);
    }

    @GetMapping(value = "/{facultyId}/cover", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @RestCall
    public @ResponseBody ResponseEntity<?> getFacultyCover(@PathVariable String facultyId) throws IOException {
        ResponseDTO responseDTO = facultyService.getFacultyCover(facultyId);
        if (responseDTO.getJsonObject() != null) {
            return responseDTO.createRestResponse();
        }
        return responseDTO.createMediaRestResponse();
    }

    @PostMapping(value = "/{facultyId}/cover")
    @RestCall
    @Auth
    @VerifyEntityAdmin
    public ResponseEntity<Void> saveFacultyCover(@PathVariable String facultyId, @RequestParam(value = "file") MultipartFile file) throws Exception {
        facultyService.saveFacultyCover(facultyId, file);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{universityId}")
    @RestCall
    @Auth
    @VerifyAdmin
    public ResponseEntity<String> createFaculty(@PathVariable String universityId, @RequestBody FacultyDTO facultyDTO) {
        facultyDTO.setUniversityId(universityId);
        return facultyService.createFaculty(facultyDTO).createRestResponse();
    }

    @PutMapping("/{facultyId}")
    @RestCall
    @VerifyAdmin
    public ResponseEntity<String> updateFaculty(@PathVariable String facultyId, @RequestBody FacultyDTO facultyDTO) {
        facultyDTO.setFacultyId(facultyId);
        return facultyService.updateFaculty(facultyDTO).createRestResponse();
    }

    @PutMapping("/{facultyId}/presentation")
    @RestCall
    @Auth
    @VerifyEntityAdmin
    public ResponseEntity<String> updatePresentation(@PathVariable String facultyId, @RequestBody FacultyDTO facultyDTO) {
        return facultyService.updatePresentation(facultyId, facultyDTO).createRestResponse();
    }

    @DeleteMapping
    @VerifyAdmin
    public ResponseEntity<String> deleteAllFaculties() {
        return facultyService.deleteAllFaculties().createRestResponse();
    }

    // not used
    @GetMapping("/v2")
    @RestCall
    public ResponseEntity<String> getFilteredFacultiesV2(FacultyFilter facultyFilter) {
        return facultyService.getFilteredFacultiesV2(facultyFilter).createRestResponse();
    }

    // not used
    @GetMapping("/universityfilter/v2")
    @RestCall
    public ResponseEntity<String> getUniversitiesForFilterV2(FacultyFilter facultyFilter) {
        return facultyService.getUniversitiesForFilter(facultyFilter).createRestResponse();
    }

    // not used
    @GetMapping("/cityfilter/v2")
    @RestCall
    public ResponseEntity<String> getFacultyCitiesForFilterV2(FacultyFilter facultyFilter) {
        return facultyService.getFacultyCitiesForFilter(facultyFilter).createRestResponse();
    }

}
