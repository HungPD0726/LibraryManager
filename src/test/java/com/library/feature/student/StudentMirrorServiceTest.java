package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentMirrorServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Test
    void ensureStudentMirror_shouldReturnExistingStudentWithoutCreatingNewOne() {
        Student existing = new Student();
        existing.setStudentId(7);

        Staff staff = new Staff();
        staff.setStaffId(7);
        staff.setUsername("student01");

        when(studentRepository.findById(7)).thenReturn(Optional.of(existing));

        JdbcTemplate jdbcTemplate = new CallbackJdbcTemplate(null);
        StudentMirrorService studentMirrorService = new StudentMirrorService(studentRepository, jdbcTemplate);

        Student mirrored = studentMirrorService.ensureStudentMirror(staff);

        assertThat(mirrored).isSameAs(existing);
    }

    @Test
    void ensureStudentMirror_shouldCreatePlaceholderEmailWhenOriginalEmailIsAlreadyUsed() throws Exception {
        Staff staff = new Staff();
        staff.setStaffId(9);
        staff.setStaffName("Nguyen Van A");
        staff.setUsername("Student 01");
        staff.setEmail("existing@example.com");

        Student existingEmailOwner = new Student();
        existingEmailOwner.setStudentId(99);

        Student created = new Student();
        created.setStudentId(9);

        SqlRecorder sqlRecorder = new SqlRecorder();

        when(studentRepository.findById(9)).thenReturn(Optional.empty(), Optional.of(created));
        when(studentRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingEmailOwner));
        JdbcTemplate jdbcTemplate = new CallbackJdbcTemplate(sqlRecorder.connection());
        StudentMirrorService studentMirrorService = new StudentMirrorService(studentRepository, jdbcTemplate);

        Student mirrored = studentMirrorService.ensureStudentMirror(staff);

        assertThat(mirrored).isSameAs(created);
        assertThat(sqlRecorder.executedSql).containsExactly(
                "SET IDENTITY_INSERT dbo.Student ON",
                "SET IDENTITY_INSERT dbo.Student OFF"
        );
        assertThat(sqlRecorder.preparedSql).isEqualTo(
                "INSERT INTO Student(StudentID, StudentName, Email, Phone) VALUES(?, ?, ?, ?)"
        );
        assertThat(sqlRecorder.parameters).containsEntry(1, 9)
                .containsEntry(2, "Nguyen Van A")
                .containsEntry(3, "student01.9@student.local")
                .containsEntry(4, null);
        assertThat(sqlRecorder.commitCount).isEqualTo(1);
    }

    @Test
    void ensureStudentMirror_shouldRejectStaffWithoutIdentity() {
        JdbcTemplate jdbcTemplate = new CallbackJdbcTemplate(null);
        StudentMirrorService studentMirrorService = new StudentMirrorService(studentRepository, jdbcTemplate);

        assertThatThrownBy(() -> studentMirrorService.ensureStudentMirror(new Staff()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class CallbackJdbcTemplate extends JdbcTemplate {

        private final Connection connection;

        private CallbackJdbcTemplate(Connection connection) {
            this.connection = connection;
        }

        @Override
        public <T> T execute(ConnectionCallback<T> action) {
            try {
                return action.doInConnection(connection);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static final class SqlRecorder {

        private final List<String> executedSql = new ArrayList<>();
        private final Map<Integer, Object> parameters = new HashMap<>();
        private String preparedSql;
        private boolean autoCommit = true;
        private int commitCount;
        private int rollbackCount;

        private Connection connection() {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> {
                        return switch (method.getName()) {
                            case "getAutoCommit" -> autoCommit;
                            case "setAutoCommit" -> {
                                autoCommit = (Boolean) args[0];
                                yield null;
                            }
                            case "createStatement" -> statement();
                            case "prepareStatement" -> {
                                preparedSql = (String) args[0];
                                yield preparedStatement();
                            }
                            case "commit" -> {
                                commitCount++;
                                yield null;
                            }
                            case "rollback" -> {
                                rollbackCount++;
                                yield null;
                            }
                            case "close" -> null;
                            default -> defaultValue(method.getReturnType());
                        };
                    }
            );
        }

        private Statement statement() {
            return (Statement) Proxy.newProxyInstance(
                    Statement.class.getClassLoader(),
                    new Class[]{Statement.class},
                    (proxy, method, args) -> {
                        if ("execute".equals(method.getName())) {
                            executedSql.add((String) args[0]);
                            return true;
                        }
                        if ("close".equals(method.getName())) {
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    }
            );
        }

        private PreparedStatement preparedStatement() {
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class[]{PreparedStatement.class},
                    (proxy, method, args) -> {
                        return switch (method.getName()) {
                            case "setInt", "setString" -> {
                                parameters.put((Integer) args[0], args[1]);
                                yield null;
                            }
                            case "executeUpdate" -> 1;
                            case "close" -> null;
                            default -> defaultValue(method.getReturnType());
                        };
                    }
            );
        }

        private Object defaultValue(Class<?> returnType) {
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == double.class) {
                return 0D;
            }
            if (returnType == float.class) {
                return 0F;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return null;
        }
    }
}
