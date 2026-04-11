package com.library.feature.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class BookFileForm {

    @NotNull(message = "Vui lòng chọn sách.")
    private Integer bookId;

    @NotBlank(message = "Tên file không được để trống.")
    private String fileName;

    @NotBlank(message = "URL file không được để trống.")
    private String fileUrl;

    private String fileType;

    @PositiveOrZero(message = "Kích thước file không hợp lệ.")
    private Long fileSize;

    private Boolean active = Boolean.TRUE;
}
