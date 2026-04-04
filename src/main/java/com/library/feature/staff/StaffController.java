package com.library.feature.staff;

import com.library.domain.model.Staff;
import com.library.feature.staff.StaffService;
import com.library.feature.staff.StaffForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Staff> staffPage = staffService.findAll(page, 10);
        model.addAttribute("staffList", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("roles", staffService.findAllRoles());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new StaffForm());
        }
        return "admin/staff/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") StaffForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "required", "MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (bindingResult.hasErrors()) {
            Page<Staff> staffPage = staffService.findAll(0, 10);
            model.addAttribute("staffList", staffPage.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", staffPage.getTotalPages());
            model.addAttribute("roles", staffService.findAllRoles());
            return "admin/staff/list";
        }

        Staff staff = new Staff();
        staff.setStaffName(form.getStaffName());
        staff.setUsername(form.getUsername());
        staff.setEmail(form.getEmail());
        staffService.createStaff(staff, form.getPassword(), form.getRoleIds());
        redirectAttributes.addFlashAttribute("msg", "ThÃƒÂªm nhÃƒÂ¢n viÃƒÂªn thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/staff";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("form") StaffForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Page<Staff> staffPage = staffService.findAll(0, 10);
            model.addAttribute("staffList", staffPage.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", staffPage.getTotalPages());
            model.addAttribute("roles", staffService.findAllRoles());
            return "admin/staff/list";
        }

        Staff staff = staffService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y nhÃƒÂ¢n viÃƒÂªn cÃ¡ÂºÂ§n cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t."));
        staff.setStaffName(form.getStaffName());
        staff.setUsername(form.getUsername());
        staff.setEmail(form.getEmail());
        staffService.updateStaff(staff, form.getPassword(), form.getRoleIds());
        redirectAttributes.addFlashAttribute("msg", "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t nhÃƒÂ¢n viÃƒÂªn thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/staff";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        staffService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "XÃƒÂ³a nhÃƒÂ¢n viÃƒÂªn thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/staff";
    }
}
