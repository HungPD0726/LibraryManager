package com.library.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileForm {

    @NotBlank(message = "Họ và tên không được để trống.")
    private String studentName;

    private String email;

    private String phone;
}
