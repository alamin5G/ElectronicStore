package com.goonok.electronicstore.service.impl;

import com.goonok.electronicstore.dto.*;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.model.*;
import com.goonok.electronicstore.repository.*;
import com.goonok.electronicstore.service.interfaces.UserService;
import com.goonok.electronicstore.verification.EmailService;
import com.goonok.electronicstore.verification.VerificationService;
import com.goonok.electronicstore.verification.VerificationToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Service
@Transactional
@Slf4j  // Add this annotation for logging
public class UserServiceImpl implements UserService {
    // Existing repositories
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ShoppingCartItemRepository cartItemRepository;
    @Autowired
    private RoleRepository roleRepository;
    
    // Add these new dependencies
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ModelMapper modelMapper;
    
    @PersistenceContext
    private EntityManager entityManager;


    // --- Registration & Verification (Keep as is) ---
    @Transactional
    @Override
    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);
        user.setEnabled(false);
        Role userRole = roleRepository.findByRoleName("ROLE_USER") // Assuming findByName exists
                .orElseThrow(() -> new RuntimeException("User Role not found"));
        user.getRoles().clear();
        user.getRoles().add(userRole);

        // Detach addresses before saving user
        List<Address> addressesToSave = user.getAddresses() != null ? new ArrayList<>(user.getAddresses()) : new ArrayList<>();
        if(user.getAddresses() != null) user.getAddresses().clear();

        User savedUser = userRepository.save(user);
        log.info("User created successfully : UserID={}", savedUser.getUserId());

        // Save addresses after user is saved
        if (!addressesToSave.isEmpty()) {
            log.info("Saving address(es) for user ID: {}", savedUser.getUserId());
            boolean firstAddress = true;
            for (Address address : addressesToSave) {
                address.setUser(savedUser);
                // Set first address as default
                address.setDefaultShipping(firstAddress);
                address.setDefaultBilling(firstAddress);
                firstAddress = false;
                addressRepository.save(address);
                log.info("Saved address ID: {}", address.getAddressId());
            }
        }
        VerificationToken verificationToken = verificationService.createVerificationToken(savedUser);
        emailService.sendVerificationEmail(savedUser, verificationToken.getToken());
    }

    @Override
    public void resendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        if (!user.isVerified()) {
            verificationService.resendVerificationToken(user);
        }
    }

    @Override
    public Page<AdminUserViewDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> {
            AdminUserViewDto dto = modelMapper.map(user, AdminUserViewDto.class);

            // Calculate analytics for each user
            dto.setTotalOrders(orderRepository.countByUser(user));
            dto.setTotalSpent(orderRepository.calculateTotalSpentByUser(user));
            dto.setLastOrderDate(orderRepository.findTopByUserOrderByCreatedAtDesc(user)
                    .map(Order::getCreatedAt)
                    .orElse(null));

            // Set customer tier based on total spent
            if (dto.getTotalSpent() != null) {
                if (dto.getTotalSpent().compareTo(new BigDecimal("100000")) > 0) {
                    dto.setCustomerTier("GOLD");
                } else if (dto.getTotalSpent().compareTo(new BigDecimal("50000")) > 0) {
                    dto.setCustomerTier("SILVER");
                } else {
                    dto.setCustomerTier("BRONZE");
                }
            }

            return dto;
        });
    }

    // --- Profile Management ---
    @Transactional(readOnly = true)
    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    @Override
    public UserProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return modelMapper.map(user, UserProfileDto.class);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(String currentEmail, UserProfileUpdateDto updateDto) {
        log.info("Attempting to update profile for email: {}", currentEmail);
        User existingUser = userRepository.findByEmailIgnoreCase(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));



        // Check if phone number is being changed and if it already exists
        if (updateDto.getPhone() != null && !updateDto.getPhone().equals(existingUser.getPhoneNumber())) {
            userRepository.findByPhoneNumber(updateDto.getPhone()).ifPresent(u -> {
                throw new IllegalArgumentException("Phone number '" + updateDto.getPhone() + "' is already in use.");
            });
            existingUser.setPhoneNumber(updateDto.getPhone());
        } else if (updateDto.getPhone() == null) {
            existingUser.setPhoneNumber(null); // Allow removing phone number
        }


        existingUser.setName(updateDto.getName());
        // Note: updatedAt timestamp is handled by @UpdateTimestamp annotation

        User updatedUser = userRepository.save(existingUser);
        log.info("User profile updated successfully for ID: {}", updatedUser.getUserId());

        // Create a new verification token and send email
        //VerificationToken verificationToken = verificationService.createVerificationToken(existingUser);
        //emailService.sendVerificationEmail(existingUser, verificationToken.getToken());

        return modelMapper.map(updatedUser, UserProfileDto.class);
    }



    // --- Password Management (Keep as is) ---
    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }


    @Override
    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated successfully for user ID: {}", user.getUserId());
    }

    @Override
    // --- Account Status (Keep as is) ---
    @Transactional
    public void enableUserAccount(User user) {
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User account enabled for ID: {}", user.getUserId());
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.findByEmailIgnoreCase(email).isPresent();
    }




    // --- Address Management ---
    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getAddressesForUser(String email) {
        log.debug("Fetching addresses for user email: {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultShippingDescIsDefaultBillingDesc(user); // Fetch user's addresses, defaults first
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddressByIdAndUser(Long addressId, String email) {
        log.debug("Fetching address ID {} for user email: {}", addressId, email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "ID", addressId + " for user " + email));
        return modelMapper.map(address, AddressDto.class);
    }

    @Override
    @Transactional
    public AddressDto addAddressToUser(String email, AddressDto addressDto) {
        log.info("Adding new address for user email: {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Create new address and set all required fields explicitly
        Address address = new Address();
        address.setUser(user);

        // Explicitly set all required fields
        address.setRecipientName(addressDto.getRecipientName());
        address.setRecipientPhone(addressDto.getRecipientPhone());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setCountry(addressDto.getCountry() != null ? addressDto.getCountry() : "Bangladesh");
        address.setPostalCode(addressDto.getPostalCode());

        // Set default flags
        List<Address> existingAddresses = addressRepository.findByUser(user);
        if (existingAddresses.isEmpty()) {
            // If this is the first address, make it default for both
            address.setDefaultShipping(true);
            address.setDefaultBilling(true);
            log.info("Setting first address as default for user ID: {}", user.getUserId());
        } else {
            // If new address is marked as default, unset others
            if (address.isDefaultShipping()) {
                unsetDefaultForOthers(existingAddresses, address.getAddressId(), "shipping");
                address.setDefaultShipping(true);
            }
            if (address.isDefaultBilling()) {
                unsetDefaultForOthers(existingAddresses, address.getAddressId(), "billing");
                address.setDefaultBilling(true);
            }
        }

        // Validate the address before saving
        validateAddress(address);

        Address savedAddress = addressRepository.save(address);
        log.info("Address added successfully with ID: {} for user ID: {}", savedAddress.getAddressId(), user.getUserId());
        return modelMapper.map(savedAddress, AddressDto.class);
    }


    @Override
    @Transactional
    public AddressDto updateAddressForUser(Long addressId, String email, AddressDto addressDto) {
        log.info("Updating address ID {} for user email: {}", addressId, email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Address existingAddress = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "ID", addressId + " for user " + email));

        // Update fields from DTO
        existingAddress.setStreet(addressDto.getStreet());
        existingAddress.setCity(addressDto.getCity());
        existingAddress.setPostalCode(addressDto.getPostalCode());
        existingAddress.setRecipientName(addressDto.getRecipientName());
        existingAddress.setRecipientPhone(addressDto.getRecipientPhone());
        // State and Country should be mapped automatically if names match
        existingAddress.setState(addressDto.getState());
        existingAddress.setCountry(addressDto.getCountry());

        // Handle default flags update
        List<Address> allUserAddresses = addressRepository.findByUser(user);
        if (addressDto.isDefaultShipping() && !existingAddress.isDefaultShipping()) {
            unsetDefaultForOthers(allUserAddresses, addressId, "shipping");
            existingAddress.setDefaultShipping(true);
        } else {
            existingAddress.setDefaultShipping(addressDto.isDefaultShipping());
        }

        if (addressDto.isDefaultBilling() && !existingAddress.isDefaultBilling()) {
            unsetDefaultForOthers(allUserAddresses, addressId, "billing");
            existingAddress.setDefaultBilling(true);
        } else {
            existingAddress.setDefaultBilling(addressDto.isDefaultBilling());
        }

        // Ensure at least one address remains default if this was the only default
        ensureAtLeastOneDefault(allUserAddresses, "shipping");
        ensureAtLeastOneDefault(allUserAddresses, "billing");


        Address updatedAddress = addressRepository.save(existingAddress);
        log.info("Address updated successfully for ID: {}", updatedAddress.getAddressId());
        return modelMapper.map(updatedAddress, AddressDto.class);
    }


    @Override
    @Transactional
    public void deleteUserAddress(Long addressId, String email) {
        log.warn("Attempting to delete address ID {} for user email: {}", addressId, email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Address addressToDelete = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "ID", addressId + " for user " + email));

        // Prevent deleting the last address? Or handle default logic carefully.
        List<Address> remainingAddresses = addressRepository.findByUser(user).stream()
                .filter(a -> !a.getAddressId().equals(addressId))
                .toList();
        if (remainingAddresses.isEmpty()) {
            throw new IllegalStateException("Cannot delete the last remaining address.");
        }

        addressRepository.delete(addressToDelete);
        log.warn("Address deleted successfully: ID={}", addressId);

        // After deletion, ensure there's still a default address if needed
        ensureAtLeastOneDefault(remainingAddresses, "shipping");
        ensureAtLeastOneDefault(remainingAddresses, "billing");
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long addressId, String email, String type) {
        log.info("Setting default {} address to ID {} for user email: {}", type, addressId, email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Fetch all addresses in a single query to minimize database hits
        List<Address> allUserAddresses = addressRepository.findByUser(user);

        // Find the target address
        Address newDefaultAddress = allUserAddresses.stream()
                .filter(addr -> addr.getAddressId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", "ID", addressId + " for user " + email));

        // Update default flags without triggering validation
        if ("shipping".equalsIgnoreCase(type)) {
            // First, unset any existing default shipping address
            allUserAddresses.stream()
                    .filter(addr -> !addr.getAddressId().equals(addressId) && addr.isDefaultShipping())
                    .forEach(addr -> {
                        addr.setDefaultShipping(false);
                        // Use native query or JPQL to update only the default flag
                        entityManager.createQuery(
                                        "UPDATE Address a SET a.isDefaultShipping = false WHERE a.addressId = :addressId")
                                .setParameter("addressId", addr.getAddressId())
                                .executeUpdate();
                    });

            // Set new default shipping address
            newDefaultAddress.setDefaultShipping(true);
            entityManager.createQuery(
                            "UPDATE Address a SET a.isDefaultShipping = true WHERE a.addressId = :addressId")
                    .setParameter("addressId", addressId)
                    .executeUpdate();
        } else if ("billing".equalsIgnoreCase(type)) {
            // First, unset any existing default billing address
            allUserAddresses.stream()
                    .filter(addr -> !addr.getAddressId().equals(addressId) && addr.isDefaultBilling())
                    .forEach(addr -> {
                        addr.setDefaultBilling(false);
                        // Use native query or JPQL to update only the default flag
                        entityManager.createQuery(
                                        "UPDATE Address a SET a.isDefaultBilling = false WHERE a.addressId = :addressId")
                                .setParameter("addressId", addr.getAddressId())
                                .executeUpdate();
                    });

            // Set new default billing address
            newDefaultAddress.setDefaultBilling(true);
            entityManager.createQuery(
                            "UPDATE Address a SET a.isDefaultBilling = true WHERE a.addressId = :addressId")
                    .setParameter("addressId", addressId)
                    .executeUpdate();
        } else {
            throw new IllegalArgumentException("Invalid default address type specified: " + type);
        }

        // Flush to ensure all updates are synchronized
        entityManager.flush();
        log.info("Default {} address updated successfully for user ID {}", type, user.getUserId());
    }

    // Find the default billing address for a user
    @Transactional(readOnly = true)
    public AddressDto getDefaultBillingAddress(String userEmail) {

        List<AddressDto> userAddresses = getAddressesForUser(userEmail);

        // Find default billing address
        AddressDto billingAddress = userAddresses.stream()
                .filter(AddressDto::isDefaultBilling)
                .findFirst()
                .orElse(null);

        // If no default billing address is found, try to find default shipping address
        if (billingAddress == null) {
            billingAddress = userAddresses.stream()
                    .filter(AddressDto::isDefaultShipping)
                    .findFirst()
                    .orElse(null);
        }

        return billingAddress;
    }

    // Find a user by their ID
    public User getUserById(long userId) {
        return userRepository.findByUserId(userId);
    }



    @Override
    @Transactional(readOnly = true)
    public long countTotalUsers() {
        // Use count() instead of findAll().size() for better performance
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public int countNewUsersToday() {
        return userRepository.getUsersCreatedToday();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserViewDto> getRecentUsers(int limit) {
        // Get recent users with pagination
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> recentUsers = userRepository.findAllByOrderByCreatedAtDesc(pageable);

        // Map to DTOs and include basic stats
        return recentUsers.stream()
                .map(user -> {
                    AdminUserViewDto dto = modelMapper.map(user, AdminUserViewDto.class);

                    // Add basic user statistics
                    dto.setTotalOrders(orderRepository.countByUser(user));
                    dto.setTotalSpent(orderRepository.calculateTotalSpentByUser(user));
                    dto.setLastOrderDate(orderRepository.findTopByUserOrderByCreatedAtDesc(user)
                            .map(Order::getCreatedAt)
                            .orElse(null));

                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public AdminUserViewDto getUserByIdForAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        AdminUserViewDto dto = modelMapper.map(user, AdminUserViewDto.class);

        // Set roles
        dto.setRoleNames(user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet()));

        // Set address information
        List<Address> addresses = addressRepository.findByUser(user);
        dto.setTotalAddresses(addresses.size());

        // Find default addresses
        addresses.stream()
                .filter(Address::isDefaultShipping)
                .findFirst()
                .ifPresent(a -> dto.setDefaultShippingAddress(formatAddress(a)));

        addresses.stream()
                .filter(Address::isDefaultBilling)
                .findFirst()
                .ifPresent(a -> dto.setDefaultBillingAddress(formatAddress(a)));

        // Set order statistics
        dto.setTotalOrders(orderRepository.countByUser(user));
        dto.setTotalSpent(orderRepository.calculateTotalSpentByUser(user));
        dto.setLastOrderDate(orderRepository.findTopByUserOrderByCreatedAtDesc(user)
                .map(Order::getCreatedAt)
                .orElse(null));

        return dto;
    }

    private String formatAddress(Address address) {
        return String.format("%s, %s, %s - %s",
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry());
    }


    @Override
    public AdminUserViewDto updateUserRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Clear existing roles
        user.getRoles().clear();

        // Add new roles
        if (roleIds != null && !roleIds.isEmpty()) {
            roleIds.forEach(roleId -> {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
                user.getRoles().add(role);
            });
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, AdminUserViewDto.class);
    }

    @Override
    public AdminUserViewDto toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Toggle the enabled status
        user.setEnabled(!user.isEnabled());

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, AdminUserViewDto.class);
    }

    @Override
    public AdminUserViewDto getUserAnalytics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AdminUserViewDto dto = modelMapper.map(user, AdminUserViewDto.class);

        // Add order analytics (existing code)
        List<Order> orders = orderRepository.findByUser(user);
        dto.setTotalOrders(orders.size());
        dto.setTotalSpent(calculateTotalSpent(orders));
        dto.setTotalProductsPurchased(calculateTotalProducts(orders));

        // Set login/account activity information
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setLoginCount(user.getLoginCount() != null ? user.getLoginCount() : 0);
        dto.setLastLoginIP(user.getLastLoginIp());

        // Set address information
        List<Address> addresses = addressRepository.findByUser(user);
        dto.setTotalAddresses(addresses.size());

        // Find and set default addresses
        addresses.stream()
                .filter(Address::isDefaultShipping)
                .findFirst()
                .ifPresent(a -> dto.setDefaultShippingAddress(formatAddress(a)));

        addresses.stream()
                .filter(Address::isDefaultBilling)
                .findFirst()
                .ifPresent(a -> dto.setDefaultBillingAddress(formatAddress(a)));

        if (orders != null && !orders.isEmpty()) {
            Order lastOrder = orders.get(0);
            dto.setLastOrderDate(lastOrder.getOrderDate());
            dto.setLastOrderStatus(lastOrder.getStatus().toString());
        }

        // Add cart information
        List<ShoppingCartItem> cartItems = cartItemRepository.findByUser(user);
        dto.setCurrentCartItems(cartItems.size());
        dto.setCurrentCartValue(cartItems.stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Set customer tier
        if (dto.getTotalSpent() != null) {
            dto.setCustomerTier(calculateCustomerTier(dto.getTotalSpent()));
        }

        return dto;
    }

    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        return Map.of();
    }

    @Override
    public Page<OrderDto> getUserOrderHistory(Long userId, Pageable pageable) {
        return null;
    }

    @Override
    public List<UserActivityLog> getUserActivityLogs(Long userId) {
        return List.of();
    }

    @Override
    public void bulkEnableUsers(Set<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(user -> user.setEnabled(true));
        userRepository.saveAll(users);
    }

    @Override
    public void bulkDisableUsers(Set<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(user -> user.setEnabled(false));
        userRepository.saveAll(users);
    }

    @Override
    public void bulkAssignRoles(Set<Long> userIds, Set<Long> roleIds) {
        List<User> users = userRepository.findAllById(userIds);
        List<Role> roles = roleRepository.findAllById(roleIds);

        users.forEach(user -> {
            user.getRoles().clear();
            user.getRoles().addAll(roles);
        });

        userRepository.saveAll(users);
    }

    @Override
    public byte[] exportUserDataToExcel(Set<Long> userIds) {
        return new byte[0];
    }

    @Override
    public byte[] exportUserOrdersToCSV(Long userId) {
        return new byte[0];
    }

    @Override
    public void updateCustomerTier(Long userId) {

    }

    @Override
    public List<AdminUserViewDto> getUsersByTier(String tier, Pageable pageable) {
        return List.of();
    }

    @Override
    public Page<AdminUserViewDto> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        // Create specification based on search criteria
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getNameContains() != null) {
                predicates.add(cb.like(cb.lower(root.get("fullName")),
                    "%" + criteria.getNameContains().toLowerCase() + "%"));
            }

            if (criteria.getEmailContains() != null) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                    "%" + criteria.getEmailContains().toLowerCase() + "%"));
            }

            if (criteria.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), criteria.getEnabled()));
            }

            if (criteria.getVerified() != null) {
                predicates.add(cb.equal(root.get("verified"), criteria.getVerified()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(user -> modelMapper.map(user, AdminUserViewDto.class));
    }

    private BigDecimal calculateTotalSpent(List<Order> orders) {
        return orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer calculateTotalProducts(List<Order> orders) {
        return orders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }

    private String calculateCustomerTier(BigDecimal totalSpent) {
        if (totalSpent == null) return "BRONZE";
        if (totalSpent.compareTo(new BigDecimal("5000")) > 0) return "GOLD";
        if (totalSpent.compareTo(new BigDecimal("1000")) > 0) return "SILVER";
        return "BRONZE";
    }

    // Add this helper method for validation
    private void validateAddress(Address address) {
        if (address.getRecipientName() == null || address.getRecipientName().trim().length() < 3) {
            throw new IllegalArgumentException("Recipient name must be at least 3 characters long");
        }
        if (address.getRecipientPhone() == null || !address.getRecipientPhone().matches("^(01[3-9]\\d{8})$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (address.getState() == null || address.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        // Add other validations as needed
    }


    // --- Helper Methods ---
    private void unsetDefaultForOthers(List<Address> addresses, Long currentAddressId, String type) {
        for (Address addr : addresses) {
            // Skip the address that is being set as default
            if (addr.getAddressId().equals(currentAddressId)) continue;

            if ("shipping".equalsIgnoreCase(type) && addr.isDefaultShipping()) {
                addr.setDefaultShipping(false);
                addressRepository.save(addr); // Save the change
            } else if ("billing".equalsIgnoreCase(type) && addr.isDefaultBilling()) {
                addr.setDefaultBilling(false);
                addressRepository.save(addr); // Save the change
            }
        }
    }


    private void ensureAtLeastOneDefault(List<Address> addresses, String type) {
        if (addresses == null || addresses.isEmpty()) return;

        boolean defaultExists = false;
        if ("shipping".equalsIgnoreCase(type)) {
            defaultExists = addresses.stream().anyMatch(Address::isDefaultShipping);
        } else if ("billing".equalsIgnoreCase(type)) {
            defaultExists = addresses.stream().anyMatch(Address::isDefaultBilling);
        }

        if (!defaultExists) {
            // If no default exists (e.g., after deleting the only default), make the first one default
            Address firstAddress = addresses.get(0);
            if ("shipping".equalsIgnoreCase(type)) {
                firstAddress.setDefaultShipping(true);
            } else if ("billing".equalsIgnoreCase(type)) {
                firstAddress.setDefaultBilling(true);
            }
            addressRepository.save(firstAddress);
            log.warn("No default {} address found after operation, setting address ID {} as default.", type, firstAddress.getAddressId());
        }


    }


}