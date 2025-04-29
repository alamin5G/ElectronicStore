package com.goonok.electronicstore.service.interfaces;

import com.goonok.electronicstore.dto.ContactMessageDto;
import com.goonok.electronicstore.enums.ContactMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContactService {

    ContactMessageDto saveContactMessage(ContactMessageDto messageDto);

    Page<ContactMessageDto> getAllMessages(Pageable pageable);

    ContactMessageDto getMessageById(Long id);

    ContactMessageDto getMessageByIdAndEmail(Long id, String email);

    ContactMessageDto markAsRead(Long id);

    void deleteMessage(Long id);

    long countUnreadMessages();

    Page<ContactMessageDto> getFilteredMessages(ContactMessageStatus status, boolean unreadOnly, Pageable pageable);

    ContactMessageDto updateMessageStatus(Long id, ContactMessageStatus status, String adminNotes);

    ContactMessageDto assignToAdmin(Long messageId, Long adminId);

    // Statistics methods
    long countMessagesByStatus(ContactMessageStatus status);

    // For dashboard - add these new methods
    Long countTotalMessages();

    Long countNewMessagesToday();

    List<ContactMessageDto> getRecentMessages(int limit);

    Page<ContactMessageDto> getUnreadMessages(Pageable pageable);

    void sendStatusNotification(Long messageId, ContactMessageStatus status);
    void sendReplyNotification(Long messageId, String replyMessage);

   }


