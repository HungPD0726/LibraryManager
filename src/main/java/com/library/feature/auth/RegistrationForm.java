package com.library.feature.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrationForm {

    @NotBlank(message = "HÃ¡Â»Â vÃƒÂ  tÃƒÂªn khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String staffName;

    @NotBlank(message = "TÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String username;

    @Email(message = "Email khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.")
    @NotBlank(message = "Email khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String email;

    @NotBlank(message = "MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String password;

    @NotBlank(message = "Vui lÃƒÂ²ng xÃƒÂ¡c nhÃ¡ÂºÂ­n mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u.")
    private String confirmPassword;
}
