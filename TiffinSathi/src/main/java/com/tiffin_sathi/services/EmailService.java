package com.tiffin_sathi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Generic email sender
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendVendorRegistrationEmail(String vendorEmail, String businessName, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(vendorEmail);
        message.setSubject("Vendor Registration Received - Tiffin Sathi");
        message.setText("Dear Vendor,\n\n" +
                "Thank you for registering your business '" + businessName + "' with Tiffin Sathi!\n\n" +
                "Temporary Login Credentials:\n" +
                "Email: " + vendorEmail + "\n" +
                "Temporary Password: " + tempPassword + "This will only work after approval\n\n" +
                "Status: PENDING\n\n" +
                "Your request to join our platform has been submitted."+ "\n" +
                "We will review your application and notify you soon.\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }

    public void sendVendorApprovalEmail(String vendorEmail, String businessName, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(vendorEmail);
        message.setSubject("Vendor Account Approved - Tiffin Sathi");
        message.setText("Dear Vendor,\n\n" +
                "Congratulations! Your vendor account for '" + businessName + "' has been approved!\n\n" +
                "Login:\n" +
                "Email: " + vendorEmail + "\n" +
                "Password: " + tempPassword + "\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }

    public void sendVendorRejectionEmail(String vendorEmail, String businessName, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(vendorEmail);
        message.setSubject("Vendor Account Status - Tiffin Sathi");
        message.setText("Dear Vendor,\n\n" +
                "Your vendor account for '" + businessName + "' has been rejected.\n\n" +
                "Reason: " + (reason != null ? reason : "Requirements not met") + "\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }

    // Method for sending delivery partner credentials
    public void sendDeliveryPartnerCredentials(String deliveryEmail, String name, String tempPassword, String vendorName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(deliveryEmail);
        message.setSubject("Welcome to Tiffin Sathi - Delivery Partner Account");
        message.setText("Dear " + name + ",\n\n" +
                "Welcome to Tiffin Sathi! Your delivery partner account has been created successfully.\n\n" +
                "Your Login Credentials:\n" +
                "Email: " + deliveryEmail + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Assigned Vendor: " + vendorName + "\n\n" +
                "Please login and change your password immediately for security reasons.\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }

    // Method for sending password reset confirmation
    public void sendPasswordResetConfirmation(String email, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Successful - Tiffin Sathi");
        message.setText("Dear " + name + ",\n\n" +
                "Your password has been reset successfully.\n\n" +
                "If you did not request this change, please contact support immediately.\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }
}
