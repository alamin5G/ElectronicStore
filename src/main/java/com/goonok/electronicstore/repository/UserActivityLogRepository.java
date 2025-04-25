package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    List<UserActivityLog> findByUser_UserIdOrderByTimestampDesc(Long userId);
    List<UserActivityLog> findByUser_UserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<UserActivityLog> findByUser_UserIdAndActivityType(Long userId, String activityType);
    List<UserActivityLog> findByActivityTypeAndTimestampBetween(String activityType, LocalDateTime start, LocalDateTime end);
}