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
    @Transactional
    // Inside UserService.java
    public void registerUser(User user) {
        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setVerified(false); // User needs to verify their email
        user.setEnabled(false);  // if email is verified than enabled
        user.setCreatedAt(LocalDateTime.now()); // Set the current date and time
        user.setUpdatedAt(LocalDateTime.now()); // Set the current date and time

        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User Role not found"));


        // Add the role to the user's set of roles
        user.getRoles().add(userRole);


        // Save the user (this will save the association in the users_roles table)
        User savedUser = userRepository.save(user);
        log.info("User created successfully : " + user);



        // Create and save the address for the user (if provided)
        if (savedUser.getAddresses() != null && !savedUser.getAddresses().isEmpty()) {
            for (Address address : savedUser.getAddresses()) {
                address.setUser(savedUser); // Set the user_id here
                addressRepository.save(address); // Save each address after the user is saved
            }
        }


        // Generate and create a verification token for the new user
        VerificationToken verificationToken = verificationService.createVerificationToken(savedUser);

        // Send a verification email with the token
        emailService.sendVerificationEmail(user, verificationToken.getToken());
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
