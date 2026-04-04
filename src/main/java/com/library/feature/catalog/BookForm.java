package com.library.feature.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BookForm {

    @NotBlank(message = "TÃƒÂªn sÃƒÂ¡ch khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    private String bookName;

    @NotNull(message = "SÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    @Min(value = 0, message = "SÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.")
    private Integer quantity;

    @NotNull(message = "SÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng cÃƒÂ³ sÃ¡ÂºÂµn khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    @Min(value = 0, message = "SÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng cÃƒÂ³ sÃ¡ÂºÂµn khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.")
    private Integer available;

    @NotNull(message = "Vui lÃƒÂ²ng chÃ¡Â»Ân danh mÃ¡Â»Â¥c.")
    private Integer categoryId;

    @NotNull(message = "Vui lÃƒÂ²ng chÃ¡Â»Ân nhÃƒÂ  xuÃ¡ÂºÂ¥t bÃ¡ÂºÂ£n.")
    private Integer publisherId;

    private List<Integer> authorIds = new ArrayList<>();

    private String description;

    private String shelfLocation;

    private String imageUrl;

    @NotNull(message = "GiÃƒÂ¡ bÃƒÂ¡n khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.")
    @Positive(message = "GiÃƒÂ¡ bÃƒÂ¡n phÃ¡ÂºÂ£i lÃ¡Â»â€ºn hÃ†Â¡n 0.")
    private BigDecimal priceAmount;

    private String currency = "VND";

    private String priceNote;
}
