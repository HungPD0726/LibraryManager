package com.library.feature.catalog;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorForm {

    @NotBlank(message = "TÃƒÂªn tÃƒÂ¡c giÃ¡ÂºÂ£ khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String authorName;
}
