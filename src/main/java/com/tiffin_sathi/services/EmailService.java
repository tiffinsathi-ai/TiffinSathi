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
                "Temporary Password: " + tempPassword + "\n\n" +
                "Status: PENDING\n\n" +
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
}
