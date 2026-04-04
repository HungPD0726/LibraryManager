package com.library.feature.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordForm {

    @NotBlank(message = "Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi.")
    private String password;

    @NotBlank(message = "Vui lÃƒÂ²ng xÃƒÂ¡c nhÃ¡ÂºÂ­n mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi.")
    private String confirmPassword;
}
