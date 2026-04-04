package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentMirrorService {

    private final StudentRepository studentRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Student ensureStudentMirror(Staff staff) {
        if (staff == null || staff.getStaffId() == null) {
            throw new IllegalArgumentException("Tài khoản đăng nhập không hợp lệ.");
        }

        return studentRepository.findById(staff.getStaffId())
                .orElseGet(() -> createMirrorStudent(staff));
    }

    private Student createMirrorStudent(Staff staff) {
        String email = resolveMirrorEmail(staff);
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (var identityStatement = connection.createStatement()) {
                identityStatement.execute("SET IDENTITY_INSERT dbo.Student ON");
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO Student(StudentID, StudentName, Email, Phone) VALUES(?, ?, ?, ?)")) {
                    insert.setInt(1, staff.getStaffId());
                    insert.setString(2, buildMirrorName(staff));
                    insert.setString(3, email);
                    insert.setString(4, null);
                    insert.executeUpdate();
                }
                identityStatement.execute("SET IDENTITY_INSERT dbo.Student OFF");
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
            return null;
        });

        return studentRepository.findById(staff.getStaffId())
                .orElseThrow(() -> new IllegalStateException("Không thể tạo hồ sơ sinh viên cho tài khoản hiện tại."));
    }

    private String resolveMirrorEmail(Staff staff) {
        String normalizedEmail = normalizeNullable(staff.getEmail());
        if (normalizedEmail == null) {
            return buildPlaceholderEmail(staff);
        }

        Optional<Student> existing = studentRepository.findByEmail(normalizedEmail);
        if (existing.isEmpty() || existing.get().getStudentId().equals(staff.getStaffId())) {
            return normalizedEmail;
        }

        return buildPlaceholderEmail(staff);
    }

    private String buildMirrorName(Staff staff) {
        if (StringUtils.hasText(staff.getStaffName())) {
            return staff.getStaffName().trim();
        }
        if (StringUtils.hasText(staff.getUsername())) {
            return staff.getUsername().trim();
        }
        return "Student #" + staff.getStaffId();
    }

    private String buildPlaceholderEmail(Staff staff) {
        String username = Optional.ofNullable(staff.getUsername())
                .map(value -> value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", ""))
                .filter(StringUtils::hasText)
                .orElse("student" + staff.getStaffId());
        return username + "." + staff.getStaffId() + "@student.local";
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
