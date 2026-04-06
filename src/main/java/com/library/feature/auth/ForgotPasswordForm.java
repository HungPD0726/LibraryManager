package com.library.feature.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordForm {

    @NotBlank(message = "Vui lòng nhập email hoặc tên đăng nhập.")
    private String identity;
}
