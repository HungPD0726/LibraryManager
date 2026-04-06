package com.library.feature.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrationForm {

    @NotBlank(message = "Họ và tên không được để trống.")
    private String staffName;

    @NotBlank(message = "Tên đăng nhập không được để trống.")
    private String username;

    @Email(message = "Email không hợp lệ.")
    @NotBlank(message = "Email không được để trống.")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống.")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu.")
    private String confirmPassword;
}
