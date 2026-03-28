package com.library.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentForm {

    @NotBlank(message = "Tên sinh viên không được để trống.")
    private String studentName;

    private String email;

    private String phone;
}
