package com.goonok.electronicstore.service;

import com.goonok.electronicstore.dto.AddressDto; // Import AddressDto
import com.goonok.electronicstore.dto.UserProfileDto; // Import UserProfileDto
import com.goonok.electronicstore.dto.UserProfileUpdateDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Import custom exception
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
import org.modelmapper.ModelMapper; // Import ModelMapper
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Import Collectors

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
    @Autowired
    private ModelMapper modelMapper; // Inject ModelMapper

    // --- Registration & Verification (Keep as is) ---
    @Transactional
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

    public void resendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        if (!user.isVerified()) {
            verificationService.resendVerificationToken(user);
        }
    }

    // --- Profile Management ---

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return modelMapper.map(user, UserProfileDto.class);
    }

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
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated successfully for user ID: {}", user.getUserId());
    }

    // --- Account Status (Keep as is) ---
    @Transactional
    public void enableUserAccount(User user) {
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User account enabled for ID: {}", user.getUserId());
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmailIgnoreCase(email).isPresent();
    }


    // --- Address Management ---

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

    @Transactional(readOnly = true)
    public AddressDto getAddressByIdAndUser(Long addressId, String email) {
        log.debug("Fetching address ID {} for user email: {}", addressId, email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "ID", addressId + " for user " + email));
        return modelMapper.map(address, AddressDto.class);
    }


    @Transactional
    public AddressDto addAddressToUser(String email, AddressDto addressDto) {
        log.info("Adding new address for user email: {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Address address = modelMapper.map(addressDto, Address.class);
        address.setUser(user); // Set the user relationship

        // Logic to handle default flags when adding a new address
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
            }
            if (address.isDefaultBilling()) {
                unsetDefaultForOthers(existingAddresses, address.getAddressId(), "billing");
            }
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address added successfully with ID: {} for user ID: {}", savedAddress.getAddressId(), user.getUserId());
        return modelMapper.map(savedAddress, AddressDto.class);
    }

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
        // Update other fields like state/country if they exist

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

    @Transactional
    public void setDefaultAddress(Long addressId, String email, String type) {
        log.info("Setting default {} address to ID {} for user email: {}", type, addressId, email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Address newDefaultAddress = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "ID", addressId + " for user " + email));

        List<Address> allUserAddresses = addressRepository.findByUser(user);

        if ("shipping".equalsIgnoreCase(type)) {
            unsetDefaultForOthers(allUserAddresses, addressId, "shipping");
            newDefaultAddress.setDefaultShipping(true);
        } else if ("billing".equalsIgnoreCase(type)) {
            unsetDefaultForOthers(allUserAddresses, addressId, "billing");
            newDefaultAddress.setDefaultBilling(true);
        } else {
            throw new IllegalArgumentException("Invalid default address type specified: " + type);
        }

        addressRepository.save(newDefaultAddress); // Save the new default
        log.info("Default {} address updated successfully for user ID {}", type, user.getUserId());
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

    // Find a user by their ID
    public User getUserById(long userId) {
        return userRepository.findByUserId(userId);
    }




}
