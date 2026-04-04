package com.library.feature.student;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordForm {

    @NotBlank(message = "Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i.")
    private String currentPassword;

    @NotBlank(message = "Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi.")
    private String newPassword;

    @NotBlank(message = "Vui lÃƒÂ²ng xÃƒÂ¡c nhÃ¡ÂºÂ­n mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi.")
    private String confirmPassword;
}
