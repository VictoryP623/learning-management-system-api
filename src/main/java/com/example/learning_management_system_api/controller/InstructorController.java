package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.CourseDTO;
import com.example.learning_management_system_api.dto.response.EarningDTO;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.service.InstructorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @PostMapping("/courses")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseVO<?> createCourse(@Valid @RequestBody CourseDTO courseDTO) {

        return ResponseVO.success(instructorService.createCourse(courseDTO));
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseVO<?> updateCourse(@PathVariable Long id,@Valid @RequestBody CourseDTO courseDTO) {
        courseDTO.setId(id);

        return ResponseVO.success(instructorService.updateCourse(courseDTO));
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseVO<String> deleteCourse(@PathVariable Long id) {
        return ResponseVO.success(instructorService.deleteCourse(id));
    }

    @GetMapping("/instructors/{id}/earnings")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseVO<List<EarningDTO>> getEarning(@PathVariable Long id,
                                      @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date from,
                                      @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date to) {
        System.out.println("getEarning called with: id=" + id + ", from=" + from + ", to=" + to);

        LocalDateTime fromLocal;
        LocalDateTime toLocal;
        if (from == null) fromLocal = LocalDateTime.now().withYear(0);
        else {
            fromLocal = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (to == null) toLocal = LocalDateTime.now().withYear(9999);
        else {
            toLocal = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return ResponseVO.success(instructorService.getEarning(id, fromLocal, toLocal));
    }

    @PostMapping("/instructors/{id}/withdraws")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseVO<?> withdrawMoney(@PathVariable("id") Long instructorId, @RequestParam Double amount){
        return ResponseVO.success(instructorService.withdraw(instructorId, amount));
    }

}
