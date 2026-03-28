package com.library.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublisherForm {

    @NotBlank(message = "Tên nhà xuất bản không được để trống.")
    private String publisherName;
}
