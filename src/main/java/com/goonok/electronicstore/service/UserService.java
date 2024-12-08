package com.goonok.electronicstore.service;

import com.goonok.electronicstore.model.Address;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.AddressRepository;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.repository.RoleRepository;
import com.goonok.electronicstore.verification.EmailService;
import com.goonok.electronicstore.verification.VerificationService;
import com.goonok.electronicstore.verification.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Assign the USER role to the new user
        user.getRoles().add(roleRepository.findByRoleName("USER"));

        // Set user as not enabled (it will be enabled after email verification)
        user.setEnabled(false);
        user.setVerified(false);

        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Create and save the address for the user (if provided)
        if (savedUser.getAddresses() != null && !savedUser.getAddresses().isEmpty()) {
            for (Address address : savedUser.getAddresses()) {
                address.setUser(savedUser); // Set the user_id here
                addressRepository.save(address); // Save each address after the user is saved
            }
        }


        // Generate and create a verification token for the new user
        VerificationToken verificationToken = verificationService.createVerificationToken(user);

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
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
