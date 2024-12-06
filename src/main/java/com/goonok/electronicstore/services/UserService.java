package com.goonok.electronicstore.services;


import com.goonok.electronicstore.dto.EditUserByAdminDto;
import com.goonok.electronicstore.dto.UserProfileUpdateDto;
import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.RoleRepository;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.verification.EmailService;
import com.goonok.electronicstore.verification.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private EmailService emailService;



    public void createUser(User user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setVerified(false); // User needs to verify their email
        user.setEnabled(false);  // if email is verified than enabled

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User Role not found"));


        // Add the role to the user's set of roles
        user.getRoles().add(userRole);
        log.info("User Role is added into the user : " + userRole);


        // Save the user (this will save the association in the users_roles table)
        userRepository.save(user);
        log.info("User created successfully : " + user);

        // Add the user to the role
        userRole.getUsers().add(user);
        log.info("User is added into the userRole : " + userRole);
        userRepository.save(user);
        log.info("User created successfully again after inserting the role user: " + user);


        // Send the verification email
        var verificationToken = verificationService.createVerificationToken(user);
        log.info("Verification token created successfully : " + verificationToken);
        emailService.sendVerificationEmail(user, verificationToken.getToken());
    }



    // Create a new admin with the role 'ROLE_ADMIN'
    public void createAdmin(User admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        ///TODO - change default to false, admin also need to verify with their email
        admin.setVerified(true); // User needs to verify their email
        admin.setEnabled(true);  // if email is verified than enabled

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin Role not found"));

        admin.getRoles().add(adminRole);

        log.info("from UserService Assigning role {} to user {}", adminRole, admin.getUsername());

        userRepository.save(admin);
        log.info("admin created successfully : " + admin);

        adminRole.getUsers().add(admin);
        log.info("Admin is added into the adminRole : " + adminRole);
        userRepository.save(admin);
        log.info("Admin created successfully : " + admin);
    }

    // Approve a user after admin approval
    public void approveUser(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            u.setEnabled(true);
            userRepository.save(u);
        });
    }

    // Find a user by their username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    // Find a user by their email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Find a user by their phone number
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    // Method to assign a role to a user
    public void assignRole(User user, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        log.info("from UserService Assigning role {} to user {}", roleName, user.getId());
        user.getRoles().add(role);
        userRepository.save(user);
    }

    //get all users including admin
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //get enabled user
    public List<User> getEnabledUsers() {
        return userRepository.findByEnabled(true);
    }

    //get enabled user
    public List<User> getDisabledUsers() {
        return userRepository.findByEnabled(false);
    }

    //get user by user id
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    //update user status by user id and set the status in the parameter
    public void updateUserStatus(Integer userId, boolean enabled) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(enabled);
            userRepository.save(user);
        }
    }

    public void updateUserWithPassword(User user) {
        // Fetch the existing user
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the user details
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());

        // Update the password if it is provided and different from the existing one
        if (user.getPassword() != null && !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Save the updated user
        userRepository.save(existingUser);
    }


    public void updateUser(UserProfileUpdateDto user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the existing user's fields
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());

        // Save the updated user back to the repository
        userRepository.save(existingUser);
    }

    public void updateUserInformationByAdmin(EditUserByAdminDto user){
        User existingUser = getUserById(user.getId()).orElseThrow( ()-> new RuntimeException("User no found"));
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setEnabled(user.isEnabled()) ;
        userRepository.save(existingUser);
    }

    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
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

    //update user password by user id and set the password in the parameter
    public void updateUserPassword(Integer userId, String password) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }
    }

    public void resendVerificationEmail(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        verificationService.resendVerificationToken(user);
    }



    public List<User> findAllByStatus(String status) {
        if (status == null) {
            return userRepository.findAll();
        }
        switch (status) {
            case "enabled":
                return userRepository.findByEnabledTrue();
            case "disabled":
                return userRepository.findByEnabledFalse();
            case "non-verified":
                return userRepository.findByVerifiedFalse();
            case "admin":
                return getAdmins();
            case "user":
                return getUsers();
            default:
                return userRepository.findAll();
        }
    }

    //get all admin
    public List<User> findAllAdmins() {
        return userRepository.findByRoles_Name("ROLE_ADMIN");
    }


    public List<User> getAdmins() {
        Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");

        return userRepository.findByRolesContaining(adminRole.orElse(null));
    }

    public List<User> getUsers() {
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        return userRepository.findByRolesContaining(userRole.orElse(null));
    }

    //find user by id
    public User findById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    //update user with only user object
    public void updateUser(User user) {
        userRepository.save(user);
    }


}


