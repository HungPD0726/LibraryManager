package com.library.feature.catalog;

import com.library.domain.model.Category;
import com.library.feature.catalog.CategoryService;
import com.library.feature.catalog.CategoryForm;
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
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CategoryForm());
        }
        return "admin/category/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") CategoryForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/category/list";
        }

        Category category = new Category();
        category.setCategoryName(form.getCategoryName().trim());
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("msg", "ThÃƒÂªm danh mÃ¡Â»Â¥c thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("form") CategoryForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/category/list";
        }

        Category category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y danh mÃ¡Â»Â¥c cÃ¡ÂºÂ§n cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t."));
        category.setCategoryName(form.getCategoryName().trim());
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("msg", "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t danh mÃ¡Â»Â¥c thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        categoryService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "XÃƒÂ³a danh mÃ¡Â»Â¥c thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/categories";
    }
}
