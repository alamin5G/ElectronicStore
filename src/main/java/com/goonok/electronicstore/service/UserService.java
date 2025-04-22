package com.goonok.electronicstore.service;

import com.goonok.electronicstore.dto.UserProfileUpdateDto;
import com.goonok.electronicstore.model.Address;
import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.AddressRepository;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.repository.RoleRepository;
import com.goonok.electronicstore.verification.EmailService;
import com.goonok.electronicstore.verification.VerificationService;
import com.goonok.electronicstore.verification.VerificationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private EmailService emailService;

    // Register a new user, assign roles, and send a verification email
    @Transactional // Ensure atomicity
    public void registerUser(User user) { // Assume 'user' comes with populated address(es) from form
        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setVerified(false);
        user.setEnabled(false);
        // Timestamps are handled by annotations - no need to set manually

        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User Role not found"));

        user.getRoles().clear(); // Ensure roles set is clean before adding
        user.getRoles().add(userRole);

        // --- Modification Starts Here ---

        // 1. Prepare addresses BEFORE saving user (ensure bidirectional link is set)
        List<Address> addressesToSave = new ArrayList<>(user.getAddresses()); // Work with a copy
        user.getAddresses().clear(); // Detach addresses from user before saving user

        // 2. Save the user FIRST to generate userId
        User savedUser = userRepository.save(user);
        log.info("User created successfully : UserID={}", savedUser.getUserId());

        // 3. Save addresses AFTER user is saved, using the generated ID
        if (!addressesToSave.isEmpty()) {
            log.info("Saving address(es) for user ID: {}", savedUser.getUserId());
            for (Address address : addressesToSave) {
                // Explicitly set the saved user on each address
                address.setUser(savedUser);

                // Set default flags for the first address if applicable (based on previous discussion)
                // This logic might need refinement depending on how you handle multiple addresses
                // For simplicity, assuming only one address collected at registration:
                if (addressesToSave.indexOf(address) == 0) { // If it's the first one
                    address.setDefaultShipping(true);
                    address.setDefaultBilling(true);
                } else {
                    address.setDefaultShipping(false);
                    address.setDefaultBilling(false);
                }

                addressRepository.save(address);
                log.info("Saved address ID: {}", address.getAddressId());
                // Optionally add the saved address back to the managed user entity's collection
                // savedUser.getAddresses().add(savedAddress); // Not strictly necessary unless needed immediately
            }
        } else {
            log.info("No addresses provided during registration for user ID: {}", savedUser.getUserId());
        }


        // --- Modification Ends Here ---


        // Generate and create a verification token for the new user
        // Pass the potentially modified savedUser if needed by verification service
        VerificationToken verificationToken = verificationService.createVerificationToken(savedUser);

        // Send a verification email with the token
        // Use savedUser if email service needs the persisted state
        emailService.sendVerificationEmail(savedUser, verificationToken.getToken());
    }


    // Resend the verification email
    public void resendVerificationEmail(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Resend the verification token if user hasn't verified their account
        if (!user.isVerified()) {
            verificationService.resendVerificationToken(user);
        }
    }

    // Find a user by their ID
    public User getUserById(long userId) {
        return userRepository.findByUserId(userId);
    }

    // Find a user by their email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public void updateUser(UserProfileUpdateDto user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the existing user's fields
        existingUser.setName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhone());

        // Save the updated user back to the repository
        userRepository.save(existingUser);
    }

    // Check if the provided old password is correct
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    // Update the user's password
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Enable the user's account
    public void enableUserAccount(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    // check if there is a user with the provided email
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}
