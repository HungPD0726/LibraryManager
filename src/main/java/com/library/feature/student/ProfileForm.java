package com.library.feature.student;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileForm {

    @NotBlank(message = "HÃ¡Â»Â vÃƒÂ  tÃƒÂªn khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String studentName;

    private String email;

    private String phone;

    private String avatarUrl;
}
