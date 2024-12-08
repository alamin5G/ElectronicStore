package com.goonok.electronicstore.verification;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class VerificationService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private final VerificationToken verificationToken = new VerificationToken();


    public VerificationToken createVerificationToken(User user) {

        String token = UUID.randomUUID().toString();

        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDate.now().plusDays(1));
        verificationToken.setVerified(false);
        log.info("Verification token created: " + token);
        return verificationTokenRepository.save(verificationToken);
    }

    public Optional<VerificationToken> getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    public boolean verifyToken(String token) {
        Optional<VerificationToken> optionalToken = getVerificationToken(token);
        if (optionalToken.isEmpty()) {
            return false; // Token not found
        }

        VerificationToken verificationToken = optionalToken.get();
        if (verificationToken.getExpiryDate().isBefore(ChronoLocalDate.from(LocalDateTime.now()))) {
            return false; // Token expired
        }

        // Token is valid, enable the user
        User user = verificationToken.getUser();

        user.setVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        //reset the expiry time to 0
        verificationToken.setVerified(true);
        verificationToken.setExpiryDate(LocalDate.now().minusDays(1));
        verificationTokenRepository.save(verificationToken);
        return true;
    }


    public void resendVerificationToken(User user) {
        Optional<VerificationToken> verificationToken = updateToken(user);

        // Logic to resend the email (use your EmailService to send the new token)
        // e.g., emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
        if (verificationToken.isPresent()) {
            if (!verificationToken.get().isVerified()){
                emailService.sendVerificationEmail(user, verificationToken.get().getToken());
                log.info("re-sent:  Verification email re-send is success");
            }else {
                log.info("re-sent:  Verification email re-send is already verified");
            }

        } else {
            log.info("Failed to resend verification email: Token not found or not updated.");
        }
    }

    // Additional methods (if required):
    // - deleteVerificationToken(String token): For cleanup or to remove expired tokens.

    public void deleteVerificationToken(String token) {
        verificationTokenRepository.deleteByToken(token);
    }

    // - findUserByVerificationToken(String token): Retrieve user associated with a given token.

    public User findUserByVerificationToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        return verificationToken.getUser();
    }

    public Optional<VerificationToken> updateToken(User user) {

        Optional<VerificationToken> optionalToken = verificationTokenRepository.findUserById(user.getUserId());

        if (optionalToken.isEmpty()) {
            log.info("Token not found");
            return optionalToken;
        }

        String updatedNewToken = UUID.randomUUID().toString();

        VerificationToken verificationToken = optionalToken.get();

        verificationToken.setToken(updatedNewToken);
        verificationToken.setExpiryDate(LocalDate.now().plusDays(1));
        verificationTokenRepository.save(verificationToken);
        return Optional.of(verificationToken);
    }


}



