package com.group6.librarymanager.controller;

import com.group6.librarymanager.model.dao.CategoryDAO;
import com.group6.librarymanager.model.dao.RoleDAO;
import com.group6.librarymanager.model.dao.StaffDAO;
import com.group6.librarymanager.model.dao.StudentDAO;
import com.group6.librarymanager.model.entity.Category;
import com.group6.librarymanager.model.entity.Role;
import com.group6.librarymanager.model.entity.Staff;
import com.group6.librarymanager.model.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class DBTestController {

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private StaffDAO staffDAO;

    /**
     * Test endpoint to check database connection
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Count records in each table
            long roleCount = roleDAO.count();
            long studentCount = studentDAO.count();
            long categoryCount = categoryDAO.count();
            long staffCount = staffDAO.count();

            response.put("status", "SUCCESS");
            response.put("message", "Database connection successful!");
            response.put("data", Map.of(
                    "roleCount", roleCount,
                    "studentCount", studentCount,
                    "categoryCount", categoryCount,
                    "staffCount", staffCount));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Database connection failed!");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all students
     */
    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentDAO.findAll());
    }

    /**
     * Get all roles
     */
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleDAO.findAll());
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryDAO.findAll());
    }

    /**
     * Get all staff
     */
    @GetMapping("/staff")
    public ResponseEntity<List<Staff>> getAllStaff() {
        return ResponseEntity.ok(staffDAO.findAll());
    }

    /**
     * Create a test student
     */
    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student savedStudent = studentDAO.save(student);
        return ResponseEntity.ok(savedStudent);
    }

    /**
     * Create a test role
     */
    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role savedRole = roleDAO.save(role);
        return ResponseEntity.ok(savedRole);
    }

    /**
     * Create a test category
     */
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryDAO.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    /**
     * Create a test staff
     */
    @PostMapping("/staff")
    public ResponseEntity<Staff> createStaff(@RequestBody Staff staff) {
        Staff savedStaff = staffDAO.save(staff);
        return ResponseEntity.ok(savedStaff);
    }

    /**
     * Initialize sample data
     */
    @PostMapping("/init-data")
    public ResponseEntity<Map<String, String>> initializeData() {
        Map<String, String> response = new HashMap<>();

        try {
            // Create sample roles
            if (roleDAO.count() == 0) {
                roleDAO.save(new Role(null, "Admin"));
                roleDAO.save(new Role(null, "Librarian"));
                roleDAO.save(new Role(null, "Assistant"));
            }

            // Create sample categories
            if (categoryDAO.count() == 0) {
                categoryDAO.save(new Category(null, "Science"));
                categoryDAO.save(new Category(null, "Fiction"));
                categoryDAO.save(new Category(null, "History"));
                categoryDAO.save(new Category(null, "Technology"));
            }

            // Create sample students
            if (studentDAO.count() == 0) {
                studentDAO.save(new Student(null, "Nguyen Van A", "nva@example.com", "0123456789"));
                studentDAO.save(new Student(null, "Tran Thi B", "ttb@example.com", "0987654321"));
                studentDAO.save(new Student(null, "Le Van C", "lvc@example.com", "0912345678"));
            }

            // Create sample staff
            if (staffDAO.count() == 0) {
                staffDAO.save(new Staff(null, "Pham Van D"));
                staffDAO.save(new Staff(null, "Hoang Thi E"));
            }

            response.put("status", "SUCCESS");
            response.put("message", "Sample data initialized successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to initialize data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
