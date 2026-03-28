package com.library.web.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BorrowRequestForm {

    @NotNull(message = "Vui lòng chọn sinh viên.")
    private Integer studentId;

    @NotEmpty(message = "Vui lòng chọn ít nhất một sách.")
    private List<Integer> bookIds = new ArrayList<>();

    private List<Integer> quantities = new ArrayList<>();

    @NotNull(message = "Vui lòng chọn hạn trả.")
    private LocalDate dueDate;
}
