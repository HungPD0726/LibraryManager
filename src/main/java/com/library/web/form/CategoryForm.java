package com.library.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryForm {

    @NotBlank(message = "Tên danh mục không được để trống.")
    private String categoryName;
}
