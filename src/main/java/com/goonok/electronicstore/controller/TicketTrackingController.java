package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.ContactMessageDto;
import com.goonok.electronicstore.dto.TicketTrackingForm;
import com.goonok.electronicstore.service.interfaces.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/track-ticket")
public class TicketTrackingController {

    @Autowired
    private ContactService contactService;

    @GetMapping
    public String showTicketTrackingForm(Model model) {
        if (!model.containsAttribute("trackingForm")) {
            model.addAttribute("trackingForm", new TicketTrackingForm());
        }
        return "track-ticket";
    }

    @PostMapping
    public String findTicket(
            @Valid @ModelAttribute("trackingForm") TicketTrackingForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "track-ticket";
        }

        try {
            // Try to find the ticket
            ContactMessageDto message = contactService.getMessageByIdAndEmail(
                    form.getTicketId(), form.getEmail());

            if (message != null) {
                model.addAttribute("message", message);
                return "ticket-details";
            } else {
                model.addAttribute("errorMessage",
                        "No ticket found with the provided ID and email address.");
                return "track-ticket";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving ticket information: " + e.getMessage());
            return "track-ticket";
        }
    }
}