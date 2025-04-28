package com.goonok.electronicstore.verification;

import com.goonok.electronicstore.model.Order;
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


        String subject = "Email Account Verification - Electronics Store: verify your account";
        //String confirmationUrl = "http://localhost:8081/verify?token=" + token;
        String content = "Hello dear " + user.getName() + ", \nYour account is created successfully! To use your account (" + user.getName()
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

    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Password Reset Request - Electronics Store";

        // Create reset password URL with token
        String resetUrl = "http://localhost:8081/reset-password?token=" + token;

        // Create email content with HTML formatting
        String content = "Hello " + user.getName() + ",\n\n" +
                "We received a request to reset your password for your Electronics Store account. " +
                "To reset your password, please click the link below:\n\n" +
                "<a href='" + resetUrl + "'>Reset Your Password</a>\n\n" +
                "This link will expire in 24 hours for security reasons.\n\n" +
                "If you didn't request a password reset, you can safely ignore this email. " +
                "Your password will remain unchanged.\n\n" +
                "If you have any questions or need assistance, please contact our customer support team.\n\n" +
                "Thank you,\n" +
                "The Electronics Store Team";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(mailFrom);
        email.setReplyTo(reply_to);
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(content);

        log.info("Sending password reset email to: {}", user.getEmail());

        try {
            mailSender.send(email);
            log.info("Password reset email sent successfully");
        } catch (MailException e) {
            log.error("Error sending password reset email", e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendOrderConfirmationEmail(User user, String subject, String body){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setReplyTo(reply_to);
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        try{
            mailSender.send(mailMessage);
            log.info("Email sent successfully");
        } catch (MailException e) {}

    }
}
