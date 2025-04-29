package com.goonok.electronicstore.service.impl;

import com.goonok.electronicstore.dto.ContactMessageDto;
import com.goonok.electronicstore.enums.ContactMessageStatus;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.model.Admin;
import com.goonok.electronicstore.model.ContactMessage;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.AdminRepository;
import com.goonok.electronicstore.repository.ContactMessageRepository;
import com.goonok.electronicstore.service.interfaces.ContactService;
import com.goonok.electronicstore.verification.EmailService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public ContactMessageDto saveContactMessage(ContactMessageDto messageDto) {
        ContactMessage message = new ContactMessage();

        // Make sure we're setting all required fields
        message.setName(messageDto.getName());
        message.setEmail(messageDto.getEmail());
        message.setSubject(messageDto.getSubject()); // This must be set properly
        message.setMessage(messageDto.getMessage());
        message.setStatus(ContactMessageStatus.NEW);
        message.setRead(false);

        ContactMessage savedMessage = contactMessageRepository.save(message);
        return mapToDto(savedMessage);
    }




    @Override
    public void deleteMessage(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contact message not found with id: " + id);
        }
        contactMessageRepository.deleteById(id);
    }

    @Override
    public long countUnreadMessages() {
        return contactMessageRepository.countUnreadMessages();
    }


    @Override
    @Transactional
    public Page<ContactMessageDto> getFilteredMessages(ContactMessageStatus status, boolean unreadOnly, Pageable pageable) {
        Page<ContactMessage> messages;

        if (status != null && unreadOnly) {
            // Filter by status and unread
            messages = contactMessageRepository.findByStatusAndReadFalse(status, pageable);
        } else if (status != null) {
            // Filter by status only
            messages = contactMessageRepository.findByStatus(status, pageable);
        } else if (unreadOnly) {
            // Filter by unread only
            messages = contactMessageRepository.findByReadFalse(pageable);
        } else {
            // No filters, get all messages
            messages = contactMessageRepository.findAll(pageable);
        }

        // Convert messages to DTOs using your custom mapToDto method
        return messages.map(this::mapToDto);
    }


    @Override
    public void sendStatusNotification(Long messageId, ContactMessageStatus status) {
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + messageId));

        // Use message ID as reference if subject is not available
        String subject = "Update on your inquiry #" + messageId + " - Electronic Store";

        String content = "Hello " + message.getName() + ",\n\n" +
                "We're writing to inform you that the status of your inquiry has been updated.\n\n" +
                "Inquiry Reference: #" + messageId + "\n" +
                "New Status: " + status.getDisplayName() + "\n\n";

        if (status == ContactMessageStatus.RESOLVED) {
            content += "Your issue has been resolved. If you have any further questions, " +
                    "please feel free to reply to this email or submit a new inquiry.\n\n";
        } else if (status == ContactMessageStatus.CLOSED) {
            content += "Your ticket has been closed. If you still need assistance, " +
                    "please submit a new inquiry referencing this message ID: " + messageId + ".\n\n";
        }

        content += "Thank you for contacting us.\n\n" +
                "Best regards,\n" +
                "Electronic Store Customer Support Team";

        // Create a simple user object to use with EmailService
        User user = new User();
        user.setEmail(message.getEmail());
        user.setName(message.getName());

        emailService.composeAndSendEmail(user, subject, content);
    }

    @Override
    public void sendReplyNotification(Long messageId, String replyMessage) {
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + messageId));

        String subject = "Response to your inquiry #" + messageId + " - Electronic Store";

        String content = "Hello " + message.getName() + ",\n\n" +
                "Thank you for contacting Electronic Store. Here is our response to your inquiry:\n\n" +
                "\"" + replyMessage + "\"\n\n" +
                "Your original message: \"" + message.getMessage() + "\"\n\n" +
                "If you have any further questions, please feel free to reply to this email " +
                "or reference your ticket ID (" + messageId + ") in a new message.\n\n" +
                "Best regards,\n" +
                "Electronic Store Customer Support Team";

        // Create a simple user object to use with EmailService
        User user = new User();
        user.setEmail(message.getEmail());
        user.setName(message.getName());

        emailService.composeAndSendEmail(user, subject, content);

        // Update message status to WAITING_FOR_RESPONSE if it's currently NEW or IN_PROGRESS
        if (message.getStatus() == ContactMessageStatus.NEW ||
                message.getStatus() == ContactMessageStatus.IN_PROGRESS) {

            message.setStatus(ContactMessageStatus.WAITING_FOR_RESPONSE);
            message.setLastUpdatedAt(LocalDateTime.now());
            contactMessageRepository.save(message);
        }
    }




    @Override
    @Transactional
    public ContactMessageDto assignToAdmin(Long messageId, Long adminId) {
        // Find the message
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        // Find the admin
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        // Associate the message with the admin
        message.setAssignedAdmin(admin);
        message.setLastUpdatedAt(LocalDateTime.now());

        // If message is NEW, change status to IN_PROGRESS
        if (message.getStatus() == ContactMessageStatus.NEW) {
            message.setStatus(ContactMessageStatus.IN_PROGRESS);
        }

        // Save the changes
        ContactMessage updatedMessage = contactMessageRepository.save(message);

        // Use your custom mapToDto method that handles lazy loading
        ContactMessageDto dto = mapToDto(updatedMessage);

        return dto;
    }

    @Override
    public ContactMessageDto getMessageById(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
        return mapToDto(message); // Use mapToDto instead of modelMapper
    }

// Change other methods similarly to use mapToDto instead of modelMapper

    @Override
    public long countMessagesByStatus(ContactMessageStatus status) {
        return contactMessageRepository.countByStatus(status);
    }



    @Override
    public Long countTotalMessages() {
        return (long) contactMessageRepository.count();
    }

    @Override
    public Long countNewMessagesToday() {
        return contactMessageRepository.countNewMessagesToday();
    }



    @Override
    @Transactional
    public Page<ContactMessageDto> getAllMessages(Pageable pageable) {
        Page<ContactMessage> messages = contactMessageRepository.findAll(pageable);
        return messages.map(this::mapToDto);
    }

    @Override
    @Transactional
    public ContactMessageDto getMessageByIdAndEmail(Long id, String email) {
        ContactMessage message = contactMessageRepository.findByIdAndEmail(id, email)
                .orElse(null);

        if (message == null) {
            return null;
        }

        return mapToDto(message);
    }

    @Override
    @Transactional
    public ContactMessageDto markAsRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));

        message.setRead(true);
        message.setReadAt(LocalDateTime.now());
        message = contactMessageRepository.save(message);

        return mapToDto(message);
    }

    @Override
    @Transactional
    public ContactMessageDto updateMessageStatus(Long id, ContactMessageStatus status, String adminNotes) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));

        // Store previous status to check if it changed
        ContactMessageStatus previousStatus = message.getStatus();

        message.setStatus(status);
        if (adminNotes != null && !adminNotes.trim().isEmpty()) {
            message.setAdminNotes(adminNotes);
        }

        // Update timestamp based on status
        message.setLastUpdatedAt(LocalDateTime.now());

        // If message is being marked as resolved, set resolved timestamp
        if (status == ContactMessageStatus.RESOLVED && message.getResolvedAt() == null) {
            message.setResolvedAt(LocalDateTime.now());
        }

        // Save and return updated message
        message = contactMessageRepository.save(message);

        // Send notification email if status changed to RESOLVED or CLOSED
        if ((status == ContactMessageStatus.RESOLVED || status == ContactMessageStatus.CLOSED)
                && previousStatus != status) {
            sendStatusNotification(id, status);
        }

        return mapToDto(message);
    }

    @Override
    @Transactional
    public Page<ContactMessageDto> getUnreadMessages(Pageable pageable) {
        Page<ContactMessage> messages = contactMessageRepository.findByReadFalse(pageable);
        return messages.map(this::mapToDto);
    }

    @Override
    @Transactional
    public List<ContactMessageDto> getRecentMessages(int limit) {
        // Get most recent messages ordered by creation date
        List<ContactMessage> recentMessages = contactMessageRepository.findAllByOrderByCreatedAtDesc();

        // Limit the result to the requested number of items
        if (recentMessages.size() > limit) {
            recentMessages = recentMessages.subList(0, limit);
        }

        // Convert to DTOs using mapToDto
        return recentMessages.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    // helper method
    private ContactMessageDto mapToDto(ContactMessage message) {
        ContactMessageDto dto = new ContactMessageDto();
        dto.setId(message.getId());
        dto.setName(message.getName());
        dto.setEmail(message.getEmail());
        dto.setSubject(message.getSubject());
        dto.setMessage(message.getMessage());
        dto.setRead(message.isRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setReadAt(message.getReadAt());
        dto.setStatus(message.getStatus());
        dto.setAdminNotes(message.getAdminNotes());
        dto.setLastUpdatedAt(message.getLastUpdatedAt());
        dto.setResolvedAt(message.getResolvedAt());
        
        // Handle the admin assignment fields properly
        if (message.getAssignedAdmin() != null) {
            Admin admin = message.getAssignedAdmin();
            dto.setAssignedAdminId(admin.getAdminId());
            
            // For the name, we need to access the User entity, which might be lazily loaded
            if (admin.getUser() != null) {
                dto.setAssignedAdminName(admin.getUser().getName());
            }
        } else {
            // Make sure these are explicitly set to null if no admin is assigned
            dto.setAssignedAdminId(null);
            dto.setAssignedAdminName(null);
        }
        
        return dto;
    }

}