package com.tiffin_sathi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVendorRegistrationEmail(String vendorEmail, String businessName, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(vendorEmail);
        message.setSubject("Vendor Registration Received - Tiffin Sathi");
        message.setText("Dear Vendor,\n\n" +
                "Thank you for registering your business '" + businessName + "' with Tiffin Sathi!\n\n" +
                "Your registration has been received and is currently under review.\n\n" +
                "Temporary Login Credentials (for admin approval process):\n" +
                "Email: " + vendorEmail + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Please note: Your account status is currently PENDING. " +
                "You will receive another email once your account is approved by our admin team.\n\n" +
                "You will be required to change your password after first login.\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }

    public void sendVendorApprovalEmail(String vendorEmail, String businessName, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(vendorEmail);
        message.setSubject("Vendor Account Approved - Tiffin Sathi");
        message.setText("Dear Vendor,\n\n" +
                "Congratulations! Your vendor account for '" + businessName + "' has been approved!\n\n" +
                "You can now login using the following credentials:\n" +
                "Email: " + vendorEmail + "\n" +
                "Password: " + tempPassword + "\n\n" +
                "Please change your password after first login for security reasons.\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }

    public void sendVendorRejectionEmail(String vendorEmail, String businessName, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(vendorEmail);
        message.setSubject("Vendor Account Status - Tiffin Sathi");
        message.setText("Dear Vendor,\n\n" +
                "We regret to inform you that your vendor account for '" + businessName + "' has been rejected.\n\n" +
                "Reason: " + (reason != null ? reason : "Business requirements not met") + "\n\n" +
                "You can contact support for more information or reapply with corrected information.\n\n" +
                "Best regards,\nTiffin Sathi Team");
        mailSender.send(message);
    }
}
