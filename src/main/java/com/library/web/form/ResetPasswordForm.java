package com.library.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordForm {

    @NotBlank(message = "Vui lòng nhập mật khẩu mới.")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới.")
    private String confirmPassword;
}
