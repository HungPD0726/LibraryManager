package com.group6.librarymanager.controller;

import com.group6.librarymanager.model.dao.StaffDAO;
import com.group6.librarymanager.model.dao.StudentDAO;
import com.group6.librarymanager.model.entity.Staff;
import com.group6.librarymanager.model.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentApiController {

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private StaffDAO staffDAO;

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentDAO.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Integer id) {
        return studentDAO.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.ok(studentDAO.save(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
        return studentDAO.findById(id).map(existing -> {
            existing.setStudentName(student.getStudentName());
            existing.setEmail(student.getEmail());
            existing.setPhone(student.getPhone());
            return ResponseEntity.ok(studentDAO.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
        if (studentDAO.existsById(id)) {
            studentDAO.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<Student> student = studentDAO.findAll().stream().filter(s -> email.equalsIgnoreCase(s.getEmail()))
                .findFirst();
        Map<String, Object> response = new HashMap<>();
        if (student.isPresent()) {
            response.put("success", true);
            response.put("user", student.get());
            response.put("role", "student");
            return ResponseEntity.ok(response);
        }
        Optional<Staff> staff = staffDAO.findAll().stream().filter(s -> email.equalsIgnoreCase(s.getStaffName()))
                .findFirst(); // Using name as login for staff as they don't have email in entity yet
        if (staff.isPresent()) {
            response.put("success", true);
            response.put("user", staff.get());
            response.put("role", "staff");
            return ResponseEntity.ok(response);
        }
        response.put("success", false);
        response.put("message", "Email không tồn tại");
        return ResponseEntity.status(401).body(response);
    }
}
