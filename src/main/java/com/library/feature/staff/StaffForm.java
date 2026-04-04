package com.library.feature.staff;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StaffForm {

    @NotBlank(message = "TÃƒÂªn nhÃƒÂ¢n viÃƒÂªn khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String staffName;

    @NotBlank(message = "TÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String username;

    private String email;

    private String password;

    private List<Integer> roleIds = new ArrayList<>();
}
