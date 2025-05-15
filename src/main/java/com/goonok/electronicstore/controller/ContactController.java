package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.ContactMessageDto;
import com.goonok.electronicstore.service.interfaces.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        if (!model.containsAttribute("contactMessage")) {
            model.addAttribute("contactMessage", new ContactMessageDto());
        }
        model.addAttribute("pageTitle", "Contact Us");
        return "contact";
    }

    @GetMapping("/about")
    public String showAboutPage(Model model) {
        System.out.println("About page controller called at: " + System.currentTimeMillis());
        model.addAttribute("pageTitle", "About Us");
        // we can load it from database if required
        model.addAttribute("companyInfo", getCompanyInfo());
        model.addAttribute("teamMembers", getTeamMembers());
        return "about-us";
    }

    @PostMapping("/send-message")
    public String processContactForm(
            @Valid @ModelAttribute("contactMessage") ContactMessageDto contactMessageDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {


        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in contact form: {}", bindingResult.getAllErrors());
            // Return to the same form with errors instead of redirecting
            model.addAttribute("errorMessage", "Please correct the errors in the form.");
            return "contact";
        }

        log.info("Received form data: {}", contactMessageDto);

        try {
            contactService.saveContactMessage(contactMessageDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your message! We'll get back to you soon.");
            return "redirect:/contact";
        } catch (Exception e) {
            log.error("Error saving contact message", e);
            model.addAttribute("errorMessage", "There was an error sending your message. Please try again later.");
            return "contact";
        }
    }

    // Helper methods to provide company and team info
    private Map<String, String> getCompanyInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Electronic Store");
        info.put("established", "2025");
        info.put("mission", "To provide high-quality electronics at affordable prices.");
        info.put("vision", "To become the leading electronics retailer in the region.");
        info.put("address", "4 Embank Road, Uttara 10, Dhaka-1203, Bangladesh");
        info.put("phone", "+88 01822 679672");
        info.put("email", "info@electronicstore.com");
        return info;
    }

    // Helper method to provide dummy team members

    private List<Map<String, String>> getTeamMembers() {
        List<Map<String, String>> team = new ArrayList<>();
        Map<String, String> member1 = new HashMap<>();
        member1.put("name", "Md. Alamin");
        member1.put("position", "CEO & Founder");
        member1.put("bio", "Alamin has over 2 years of experience in the electronics industry.");
        member1.put("image", "/images/team/alamin.jpg");
        member1.put("linkedin", "https://Linkedin.com/in/alamin5g");
        member1.put("twitter", "https://X.com/alamin5g");
        member1.put("email", "alaminvai5g@gmail.com");

        Map<String, String> member2 = new HashMap<>();
        member2.put("name", "Abu Hanif Riad");
        member2.put("position", "CTO");
        member2.put("bio", "Riad leads our technical team with her expertise in the latest electronic innovations.");
        member2.put("image", "/images/team/riad.jpg");
        member2.put("linkedin", "https://x.com/Elhan_Rid");
        member2.put("twitter", "www.linkedin.com/in/abrar-hanif-riad");
        member2.put("email", "ahriad.cse@gmail.com");




        team.add(member1);
        team.add(member2);

        return team;
    }
}