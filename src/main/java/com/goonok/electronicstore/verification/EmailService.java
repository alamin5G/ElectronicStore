package com.goonok.electronicstore.verification;

import com.goonok.electronicstore.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${mail.reply-to}")
    private String reply_to;

    public void sendVerificationEmail(User user, String token) {
        String subject = "Account Verification : " + user.getUsername() + ", please verify your account";
        //String confirmationUrl = "http://localhost:8081/verify?token=" + token;
        String content = "Hello dear " + user.getFullName() + ", \nYour account is created successfully! To use your account (" + user.getUsername()
                + ") Please verify your email between 24 Hours by clicking the link below:\n"
                + "<a href='http://localhost:8081/verify?token=" + token + "'>Verify Email</a>";;


        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(mailFrom);
        email.setReplyTo(reply_to);
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(content);

        log.info("Sending email to: {}", user.getEmail());
        log.info("Email content: {}", email.getText());

        try{
            mailSender.send(email);
            log.info("Email sent successfully");
        } catch (MailException e) {
            log.info("Runtime exception from the mail sender");
            throw new RuntimeException(e);

        }

    }
}
