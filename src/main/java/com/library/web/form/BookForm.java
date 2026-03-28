package com.library.web.form;

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

    @NotBlank(message = "Tên sách không được để trống.")
    private String bookName;

    @NotNull(message = "Số lượng không được để trống.")
    @Min(value = 0, message = "Số lượng không hợp lệ.")
    private Integer quantity;

    @NotNull(message = "Số lượng có sẵn không được để trống.")
    @Min(value = 0, message = "Số lượng có sẵn không hợp lệ.")
    private Integer available;

    @NotNull(message = "Vui lòng chọn danh mục.")
    private Integer categoryId;

    @NotNull(message = "Vui lòng chọn nhà xuất bản.")
    private Integer publisherId;

    private List<Integer> authorIds = new ArrayList<>();

    private String description;

    private String shelfLocation;

    private String imageUrl;

    @NotNull(message = "Giá bán không được để trống.")
    @Positive(message = "Giá bán phải lớn hơn 0.")
    private BigDecimal priceAmount;

    private String currency = "VND";

    private String priceNote;
}
