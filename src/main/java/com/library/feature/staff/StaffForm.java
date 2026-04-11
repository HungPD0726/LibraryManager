package com.library.feature.staff;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StaffForm {

    @NotBlank(message = "Tên nhân viên không được để trống.")
    private String staffName;

    @NotBlank(message = "Tên đăng nhập không được để trống.")
    private String username;

    private String email;

    private String password;

    private List<Integer> roleIds = new ArrayList<>();
}
