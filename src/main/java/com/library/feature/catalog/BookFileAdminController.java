package com.library.feature.catalog;

import com.library.domain.model.BookFile;
import com.library.domain.model.Staff;
import com.library.feature.catalog.BookFileService;
import com.library.feature.catalog.BookService;
import com.library.feature.staff.StaffService;
import com.library.feature.catalog.BookFileForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bookfiles")
@RequiredArgsConstructor
public class BookFileAdminController {

    private final BookFileService bookFileService;
    private final BookService bookService;
    private final StaffService staffService;

    @GetMapping
    public String list(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new BookFileForm());
        }
        model.addAttribute("bookFiles", bookFileService.findAllViews());
        model.addAttribute("books", bookService.findAll());
        return "admin/bookfile/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") BookFileForm form,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookFiles", bookFileService.findAllViews());
            model.addAttribute("books", bookService.findAll());
            return "admin/bookfile/list";
        }

        Staff staff = staffService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y nhÃƒÂ¢n viÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p."));
        bookFileService.create(form, staff.getStaffId());
        redirectAttributes.addFlashAttribute("msg", "ThÃƒÂªm file sÃƒÂ¡ch thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/bookfiles";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("form") BookFileForm form,
                       BindingResult bindingResult,
                       Authentication authentication,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookFiles", bookFileService.findAllViews());
            model.addAttribute("books", bookService.findAll());
            return "admin/bookfile/list";
        }

        Staff staff = staffService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y nhÃƒÂ¢n viÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p."));
        bookFileService.update(id, form, staff.getStaffId());
        redirectAttributes.addFlashAttribute("msg", "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t file sÃƒÂ¡ch thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/bookfiles";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        bookFileService.delete(id);
        redirectAttributes.addFlashAttribute("msg", "XÃƒÂ³a file sÃƒÂ¡ch thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/bookfiles";
    }

    @ModelAttribute("editingBookFile")
    public BookFile editingBookFile() {
        return null;
    }
}
