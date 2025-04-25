package com.goonok.electronicstore.service.interfaces;

import com.goonok.electronicstore.dto.*; // Import necessary DTOs
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.model.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; // Import Set

public interface UserService {

    // --- Existing User Methods ---
    void registerUser(User user);
    void resendVerificationEmail(Long userId);
    Optional<User> getUserByEmail(String email);
    UserProfileDto getUserProfileByEmail(String email);
    UserProfileDto updateUserProfile(String currentEmail, UserProfileUpdateDto updateDto);
    boolean checkIfValidOldPassword(User user, String oldPassword);
    void updatePassword(User user, String newPassword);
    void enableUserAccount(User user); // This might be admin action too
    boolean isEmailExists(String email);
    List<AddressDto> getAddressesForUser(String email);
    AddressDto getAddressByIdAndUser(Long addressId, String email);
    AddressDto addAddressToUser(String email, AddressDto addressDto);
    AddressDto updateAddressForUser(Long addressId, String email, AddressDto addressDto);
    void deleteUserAddress(Long addressId, String email);
    void setDefaultAddress(Long addressId, String email, String type);
    AddressDto getDefaultBillingAddress(String userEmail);


        // --- Admin User Management Methods ---

    /**
     * Retrieves all registered users with pagination for admin view.
     * @param pageable Pagination and sorting information.
     * @return A Page of AdminUserViewDto objects.
     */
    @Transactional(readOnly = true)
    Page<AdminUserViewDto> getAllUsers(Pageable pageable);

    /**
     * Retrieves detailed information for a specific user by ID for admin view.
     * @param userId The ID of the user.
     * @return The AdminUserViewDto.
     * @throws ResourceNotFoundException if user not found.
     */
    @Transactional(readOnly = true)
    AdminUserViewDto getUserByIdForAdmin(Long userId);

    /**
     * Updates the roles assigned to a specific user.
     * @param userId The ID of the user to update.
     * @param roleIds A Set of Role IDs to assign to the user.
     * @return The updated AdminUserViewDto.
     * @throws ResourceNotFoundException if user or any role ID not found.
     */
    @Transactional
    AdminUserViewDto updateUserRoles(Long userId, Set<Long> roleIds);

    /**
     * Toggles the enabled/disabled status of a user account.
     * @param userId The ID of the user account to toggle.
     * @return The updated AdminUserViewDto showing the new status.
     * @throws ResourceNotFoundException if user not found.
     */
    @Transactional
    AdminUserViewDto toggleUserStatus(Long userId);
    // New admin management methods
    AdminUserViewDto getUserAnalytics(Long userId);
    Map<String, Object> getUserStatistics(Long userId);
    Page<OrderDto> getUserOrderHistory(Long userId, Pageable pageable);
    List<UserActivityLog> getUserActivityLogs(Long userId);

    // Bulk operations
    void bulkEnableUsers(Set<Long> userIds);
    void bulkDisableUsers(Set<Long> userIds);
    void bulkAssignRoles(Set<Long> userIds, Set<Long> roleIds);

    // Export operations
    byte[] exportUserDataToExcel(Set<Long> userIds);
    byte[] exportUserOrdersToCSV(Long userId);

    // Customer segmentation
    void updateCustomerTier(Long userId);
    List<AdminUserViewDto> getUsersByTier(String tier, Pageable pageable);

    // Search and filter
    Page<AdminUserViewDto> searchUsers(UserSearchCriteria criteria, Pageable pageable);

    User getUserById(long userId);
}