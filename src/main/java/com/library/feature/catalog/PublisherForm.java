package com.library.feature.catalog;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublisherForm {

    @NotBlank(message = "TÃƒÂªn nhÃƒÂ  xuÃ¡ÂºÂ¥t bÃ¡ÂºÂ£n khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String publisherName;
}
