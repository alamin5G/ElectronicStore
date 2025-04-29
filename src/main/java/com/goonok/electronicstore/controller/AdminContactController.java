package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.ContactMessageDto;
import com.goonok.electronicstore.dto.TicketTrackingForm;
import com.goonok.electronicstore.enums.ContactMessageStatus;
import com.goonok.electronicstore.model.Admin;
import com.goonok.electronicstore.repository.AdminRepository;
import com.goonok.electronicstore.service.interfaces.ContactService;
import com.goonok.electronicstore.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/messages")
@Slf4j
public class AdminContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping
    public String showAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Model model) {

        // Create pageable with sort by createdAt DESC
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Convert status string to enum if needed
        ContactMessageStatus statusEnum = "ALL".equals(status) ? null :
                ContactMessageStatus.valueOf(status);

        // Get filtered messages
        Page<ContactMessageDto> messages = contactService.getFilteredMessages(statusEnum, unreadOnly, pageable);

        // Add counters to model
        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", page);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("unreadOnly", unreadOnly);

        // Add status counts to model
        model.addAttribute("newCount", contactService.countMessagesByStatus(ContactMessageStatus.NEW));
        model.addAttribute("inProgressCount", contactService.countMessagesByStatus(ContactMessageStatus.IN_PROGRESS));
        model.addAttribute("waitingCount", contactService.countMessagesByStatus(ContactMessageStatus.WAITING_FOR_RESPONSE));
        model.addAttribute("resolvedCount", contactService.countMessagesByStatus(ContactMessageStatus.RESOLVED));
        model.addAttribute("closedCount", contactService.countMessagesByStatus(ContactMessageStatus.CLOSED));
        model.addAttribute("unreadCount", contactService.countUnreadMessages());

        return "admin/messages/list";
    }

    @GetMapping("/{id}")
    public String viewMessage(@PathVariable Long id, Model model) {
        ContactMessageDto message = contactService.getMessageById(id);

        // Ensure message status is never null
        if (message.getStatus() == null) {
            message.setStatus(ContactMessageStatus.NEW);
        }

        // Mark message as read if it's unread
        if (!message.isRead()) {
            message = contactService.markAsRead(id);
        }

        // Get all admin users from repository
        List<Admin> admins = adminRepository.findAll();

        model.addAttribute("message", message);
        model.addAttribute("statuses", ContactMessageStatus.values());
        model.addAttribute("admins", admins);

        return "admin/messages/view";
    }

    @PostMapping("/{id}/update-status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam ContactMessageStatus status,
            @RequestParam(required = false) String adminNotes,
            RedirectAttributes redirectAttributes) {

        try {
            contactService.updateMessageStatus(id, status, adminNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update status: " + e.getMessage());
        }

        return "redirect:/admin/messages/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignToAdmin(
            @PathVariable Long id,
            @RequestParam Long adminId,
            RedirectAttributes redirectAttributes) {

    log.debug("Assigning message ID {} to admin ID {}", id, adminId);
    
    try {
        ContactMessageDto updatedMessage = contactService.assignToAdmin(id, adminId);
        log.debug("Assignment result - AdminId: {}, AdminName: {}", 
                updatedMessage.getAssignedAdminId(), 
                updatedMessage.getAssignedAdminName());
        
        if (updatedMessage.getAssignedAdminId() != null) {
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Message assigned to " + updatedMessage.getAssignedAdminName() + " successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Assignment was processed but admin information was not correctly updated.");
        }
    } catch (Exception e) {
        log.error("Error assigning message: ", e);
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign message: " + e.getMessage());
    }

    return "redirect:/admin/messages/" + id;
}


    @PostMapping("/{id}/reply")
    public String replyToMessage(
            @PathVariable Long id,
            @RequestParam String replyMessage,
            RedirectAttributes redirectAttributes) {

        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reply message cannot be empty.");
            return "redirect:/admin/messages/" + id;
        }

        try {
            // Send email notification with reply
            contactService.sendReplyNotification(id, replyMessage);

            // Update status and save a record of this reply
            // (You could extend this to save replies in a separate table)
            redirectAttributes.addFlashAttribute("successMessage", "Reply sent successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send reply: " + e.getMessage());
        }

        return "redirect:/admin/messages/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteMessage(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            contactService.deleteMessage(id);
            redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully.");
            return "redirect:/admin/messages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete message: " + e.getMessage());
            return "redirect:/admin/messages/" + id;
        }
    }

    @GetMapping("/track-ticket")
    public String showTicketTrackingForm(Model model) {
        if (!model.containsAttribute("trackingForm")) {
            model.addAttribute("trackingForm", new TicketTrackingForm());
        }
        return "track-ticket";
    }

    @PostMapping("/track-ticket")
    public String findTicket(
            @Valid @ModelAttribute("trackingForm") TicketTrackingForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

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
            model.addAttribute("errorMessage", "Error retrieving ticket information.");
            return "track-ticket";
        }
    }

}