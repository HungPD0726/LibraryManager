package com.library.feature.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordForm {

    @NotBlank(message = "Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p email hoÃ¡ÂºÂ·c tÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p.")
    private String identity;
}
