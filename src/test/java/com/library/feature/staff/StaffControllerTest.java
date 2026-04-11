package com.library.feature.staff;

import com.library.domain.model.Staff;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.staff.RoleOptionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private StaffController staffController;

    @BeforeEach
    void setUp() {
        staffController = new StaffController(new StaffService(staffRepository, roleRepository, passwordEncoder));
    }

    @Test
    void create_shouldRejectBlankPasswordWithLocalizedMessage() {
        StaffForm form = new StaffForm();
        form.setStaffName("Thủ thư A");
        form.setUsername("thu-thu-a");
        form.setEmail("a@example.com");
        form.setPassword(" ");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        ExtendedModelMap model = new ExtendedModelMap();

        when(staffRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));
        when(roleRepository.findAllOptions()).thenReturn(List.<RoleOptionView>of());

        String view = staffController.create(form, bindingResult, model, new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("admin/staff/list");
        assertThat(bindingResult.getFieldError("password")).isNotNull();
        assertThat(bindingResult.getFieldError("password").getDefaultMessage()).isEqualTo("Mật khẩu không được để trống.");
    }

    @Test
    void edit_shouldThrowLocalizedMessageWhenStaffIsMissing() {
        StaffForm form = new StaffForm();
        form.setStaffName("Thủ thư A");
        form.setUsername("thu-thu-a");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        when(staffRepository.findById(15)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffController.edit(15, form, bindingResult, new ExtendedModelMap(), new RedirectAttributesModelMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không tìm thấy nhân viên cần cập nhật.");
    }

    @Test
    void create_shouldSetLocalizedFlashMessage() {
        StaffForm form = new StaffForm();
        form.setStaffName("Thủ thư B");
        form.setUsername("thu-thu-b");
        form.setEmail("b@example.com");
        form.setPassword("Password1");
        form.setRoleIds(List.of());
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = staffController.create(form, bindingResult, new ExtendedModelMap(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/staff");
        assertThat(redirectAttributes.getFlashAttributes().get("msg")).isEqualTo("Thêm nhân viên thành công.");
        verify(staffRepository).save(any(Staff.class));
    }
}
