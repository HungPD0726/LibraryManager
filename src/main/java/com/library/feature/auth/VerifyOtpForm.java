package com.library.feature.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpForm {

    @NotBlank(message = "Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p mÃƒÂ£ OTP.")
    private String otp;
}
