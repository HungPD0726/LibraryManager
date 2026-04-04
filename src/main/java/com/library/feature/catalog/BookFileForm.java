package com.library.feature.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class BookFileForm {

    @NotNull(message = "Vui lÃƒÂ²ng chÃ¡Â»Ân sÃƒÂ¡ch.")
    private Integer bookId;

    @NotBlank(message = "TÃƒÂªn file khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String fileName;

    @NotBlank(message = "URL file khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String fileUrl;

    private String fileType;

    @PositiveOrZero(message = "KÃƒÂ­ch thÃ†Â°Ã¡Â»â€ºc file khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.")
    private Long fileSize;

    private Boolean active = Boolean.TRUE;
}
