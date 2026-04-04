package com.library.feature.borrow;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BorrowRequestForm {

    @NotNull(message = "Vui lÃƒÂ²ng chÃ¡Â»Ân sinh viÃƒÂªn.")
    private Integer studentId;

    @NotEmpty(message = "Vui lÃƒÂ²ng chÃ¡Â»Ân ÃƒÂ­t nhÃ¡ÂºÂ¥t mÃ¡Â»â„¢t sÃƒÂ¡ch.")
    private List<Integer> bookIds = new ArrayList<>();

    private List<Integer> quantities = new ArrayList<>();

    @NotNull(message = "Vui lÃƒÂ²ng chÃ¡Â»Ân hÃ¡ÂºÂ¡n trÃ¡ÂºÂ£.")
    private LocalDate dueDate;
}
