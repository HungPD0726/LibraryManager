package com.library.feature.catalog;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorForm {

    @NotBlank(message = "Tên tác giả không được để trống.")
    private String authorName;
}
