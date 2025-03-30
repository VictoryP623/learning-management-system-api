package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.response.LessonResourceDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.LessonResource;
import com.example.learning_management_system_api.service.LessonResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lesson-resources")
public class LessonResourceController {

    private final LessonResourceService lessonResourceService;

    public LessonResourceController(LessonResourceService lessonResourceService) {
        this.lessonResourceService = lessonResourceService;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseEntity<LessonResourceDto> uploadResource(@RequestParam("file") MultipartFile multipartFile,
                                                            @RequestParam("lessonId") Long lessonId,
                                                            @RequestParam("resourceName") String resourceName){
        return new ResponseEntity<>(lessonResourceService.addLessonResource(multipartFile, lessonId, resourceName), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id){
        lessonResourceService.deleteResourceFile(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
    public ResponseEntity<PageDto> getAllLessonResources(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int limit,
                                                         @RequestParam Long lessonId){
        return new ResponseEntity<>(lessonResourceService.getAllLessonResource(lessonId, page, limit), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
    public ResponseEntity<LessonResourceDto> getLessonResource(@PathVariable Long id){
        return new ResponseEntity<>(lessonResourceService.getLessonResource(id), HttpStatus.OK);
    }
}
