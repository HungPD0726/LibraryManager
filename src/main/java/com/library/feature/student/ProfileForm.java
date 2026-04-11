package com.library.feature.student;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileForm {

    @NotBlank(message = "Họ và tên không được để trống.")
    private String studentName;

    private String email;

    private String phone;

    private String avatarUrl;
}
