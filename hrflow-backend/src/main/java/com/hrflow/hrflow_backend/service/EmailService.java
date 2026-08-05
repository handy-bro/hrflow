package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.config.AppProperties;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.entity.LeaveRequest;
import com.hrflow.hrflow_backend.enums.LeaveStatus;
import com.hrflow.hrflow_backend.exceptionHandler.mails.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    private static final int ACTIVATION_EXPIRY_HOURS = 24;
    private static final int VERIFICATION_EXPIRY_HOURS = 24;
    private static final int RESET_EXPIRY_HOURS = 1;

    // ==================================================================
    // Auth
    // ==================================================================

    public void sendAccountActivationEmail(String to, String token) {
        Context context = new Context();
        context.setVariable("link", appProperties.frontendUrl() + "/activate-account?token=" + token);
        context.setVariable("expiryHours", ACTIVATION_EXPIRY_HOURS);

        sendHtml(to, "HRFlow - Activate your account", "email/activation", context);
    }

    public void sendVerificationEmail(String to, String token) {
        Context context = new Context();
        context.setVariable("link", appProperties.frontendUrl() + "/verify-email?token=" + token);
        context.setVariable("expiryHours", VERIFICATION_EXPIRY_HOURS);

        sendHtml(to, "HRFlow - Verify your mail", "email/verification", context);
    }

    public void sendResetPasswordEmail(String to, String token) {
        Context context = new Context();
        context.setVariable("link", appProperties.frontendUrl() + "/reset-password?token=" + token);
        context.setVariable("expiryHours", RESET_EXPIRY_HOURS);

        sendHtml(to, "HRFlow - Reset your password", "email/reset-password", context);
    }

    // ==================================================================
    // Leaves
    // ==================================================================

    public void sendLeaveRequestSubmittedEmail(String managerEmail, Employee employee, LeaveRequest request) {
        Context context = new Context();
        context.setVariable("employeeName", employee.getFirstName() + " " + employee.getLastName());
        context.setVariable("leaveType", request.getLeaveType());
        context.setVariable("startDate", request.getStartDate());
        context.setVariable("endDate", request.getEndDate());
        context.setVariable("requestedDays", request.getRequestedDays());
        context.setVariable("link", appProperties.frontendUrl() + "/leaves/" + request.getId());

        sendHtml(managerEmail,
                "HRFlow - New leave request for " + employee.getFirstName() + " " + employee.getLastName(),
                "email/leave-submitted", context);
    }

    public void sendLeaveRequestReviewedEmail(String employeeEmail, LeaveRequest request) {
        boolean approved = request.getStatus() == LeaveStatus.APPROVED;

        Context context = new Context();
        context.setVariable("approved", approved);
        context.setVariable("startDate", request.getStartDate());
        context.setVariable("endDate", request.getEndDate());
        context.setVariable("comment", request.getManagerComment());
        context.setVariable("link", appProperties.frontendUrl() + "/leaves/" + request.getId());

        sendHtml(employeeEmail,
                "HRFlow - Your leave request has been " + (approved ? "approved" : "rejected"),
                "email/leave-reviewed", context);
    }

    // ==================================================================
    // Common template
    // ==================================================================

    private void sendHtml(String to, String subject, String templateName, Context context) {
        try {
            String htmlBody = templateEngine.process(templateName, context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} using template {}", to, templateName, e);
            throw new EmailSendingException("Failed to send email, caused by" + e);
        }
    }
}