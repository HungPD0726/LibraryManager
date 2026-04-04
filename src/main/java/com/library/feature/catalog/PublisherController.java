package com.library.feature.catalog;

import com.library.domain.model.Publisher;
import com.library.feature.catalog.PublisherService;
import com.library.feature.catalog.PublisherForm;
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
        redirectAttributes.addFlashAttribute("msg", "ThÃƒÂªm nhÃƒÂ  xuÃ¡ÂºÂ¥t bÃ¡ÂºÂ£n thÃƒÂ nh cÃƒÂ´ng.");
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
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y nhÃƒÂ  xuÃ¡ÂºÂ¥t bÃ¡ÂºÂ£n cÃ¡ÂºÂ§n cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t."));
        publisher.setPublisherName(form.getPublisherName().trim());
        publisherService.save(publisher);
        redirectAttributes.addFlashAttribute("msg", "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t nhÃƒÂ  xuÃ¡ÂºÂ¥t bÃ¡ÂºÂ£n thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/publishers";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        publisherService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "XÃƒÂ³a nhÃƒÂ  xuÃ¡ÂºÂ¥t bÃ¡ÂºÂ£n thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/publishers";
    }
}
