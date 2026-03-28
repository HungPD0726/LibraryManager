package com.library.controller;

import com.library.entity.Author;
import com.library.service.AuthorService;
import com.library.web.form.AuthorForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("authors", authorService.findAll());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AuthorForm());
        }
        return "admin/author/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") AuthorForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            return "admin/author/list";
        }

        Author author = new Author();
        author.setAuthorName(form.getAuthorName().trim());
        authorService.save(author);
        redirectAttributes.addFlashAttribute("msg", "Thêm tác giả thành công.");
        return "redirect:/admin/authors";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("form") AuthorForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            return "admin/author/list";
        }

        Author author = authorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tác giả cần cập nhật."));
        author.setAuthorName(form.getAuthorName().trim());
        authorService.save(author);
        redirectAttributes.addFlashAttribute("msg", "Cập nhật tác giả thành công.");
        return "redirect:/admin/authors";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        authorService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "Xóa tác giả thành công.");
        return "redirect:/admin/authors";
    }
}
