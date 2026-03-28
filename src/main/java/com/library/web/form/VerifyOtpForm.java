package com.library.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpForm {

    @NotBlank(message = "Vui lòng nhập mã OTP.")
    private String otp;
}
