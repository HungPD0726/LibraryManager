package com.library.feature.student;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentForm {

    @NotBlank(message = "TÃƒÂªn sinh viÃƒÂªn khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String studentName;

    private String email;

    private String phone;
}
