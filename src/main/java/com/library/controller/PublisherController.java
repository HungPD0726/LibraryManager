package com.library.controller;

import com.library.entity.Publisher;
import com.library.service.PublisherService;
import com.library.web.form.PublisherForm;
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
@RequestMapping("/admin/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("publishers", publisherService.findAll());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PublisherForm());
        }
        return "admin/publisher/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") PublisherForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("publishers", publisherService.findAll());
            return "admin/publisher/list";
        }

        Publisher publisher = new Publisher();
        publisher.setPublisherName(form.getPublisherName().trim());
        publisherService.save(publisher);
        redirectAttributes.addFlashAttribute("msg", "Thêm nhà xuất bản thành công.");
        return "redirect:/admin/publishers";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("form") PublisherForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("publishers", publisherService.findAll());
            return "admin/publisher/list";
        }

        Publisher publisher = publisherService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà xuất bản cần cập nhật."));
        publisher.setPublisherName(form.getPublisherName().trim());
        publisherService.save(publisher);
        redirectAttributes.addFlashAttribute("msg", "Cập nhật nhà xuất bản thành công.");
        return "redirect:/admin/publishers";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        publisherService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "Xóa nhà xuất bản thành công.");
        return "redirect:/admin/publishers";
    }
}
