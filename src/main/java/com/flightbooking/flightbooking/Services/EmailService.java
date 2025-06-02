package com.flightbooking.flightbooking.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("vanarasi.sainath07@gmail.com");
            message.setTo(toEmail);
            message.setSubject("SaiAirways - OTP Verification");
            message.setText("Dear User,\n\n" +
                    "Your OTP for verification is: " + otp + "\n\n" +
                    "This OTP will expire in 10 minutes.\n\n" +
                    "Please do not share this OTP with anyone.\n\n" +
                    "Thank you,\n" +
                    "SaiAirways Team");

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    public void sendWelcomeEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("vanarasi.sainath07@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Welcome to SaiAirways!");
            message.setText("Dear Valued Customer,\n\n" +
                    "Welcome to SaiAirways!\n\n" +
                    "Thank you for registering with us. Your account has been successfully created and verified.\n\n" +
                    "You can now:\n" +
                    "• Search and book flights\n" +
                    "• Manage your bookings\n" +
                    "• Access exclusive deals and offers\n\n" +
                    "We look forward to serving you and making your travel experience memorable.\n\n" +
                    "Happy Flying!\n\n" +
                    "Best regards,\n" +
                    "SaiAirways Team");

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending welcome email to: {}", toEmail, e);
        }
    }

    public void sendLoginOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("vanarasi.sainath07@gmail.com");
            message.setTo(toEmail);
            message.setSubject("SaiAirways - Login Verification");
            message.setText("Dear User,\n\n" +
                    "Your login verification OTP is: " + otp + "\n\n" +
                    "This OTP will expire in 10 minutes.\n\n" +
                    "If you did not request this login, please ignore this email.\n\n" +
                    "Thank you,\n" +
                    "SaiAirways Team");

            mailSender.send(message);
            log.info("Login OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending login OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send login OTP email");
        }
    }
}
