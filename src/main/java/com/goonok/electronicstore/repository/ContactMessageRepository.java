package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.enums.ContactMessageStatus;
import com.goonok.electronicstore.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    // Existing methods...

    // Find a message by its ID and the sender's email
    Optional<ContactMessage> findByIdAndEmail(Long id, String email);

    // Count unread messages
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.read = false")
    long countUnreadMessages();

    // Find by status
    Page<ContactMessage> findByStatus(ContactMessageStatus status, Pageable pageable);

    // Find by unread status
    Page<ContactMessage> findByReadFalse(Pageable pageable);

    // Find by status and unread
    Page<ContactMessage> findByStatusAndReadFalse(ContactMessageStatus status, Pageable pageable);

    // Count by status
    long countByStatus(ContactMessageStatus status);

    // Find all ordered by creation date
    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    // Count new messages created today
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE DATE(c.createdAt) = CURRENT_DATE")
    Long countNewMessagesToday();
}