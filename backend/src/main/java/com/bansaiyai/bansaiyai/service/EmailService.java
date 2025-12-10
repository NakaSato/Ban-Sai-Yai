package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Email notification service for sending templated emails.
 * Uses async processing to avoid blocking main threads.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username:noreply@bansaiyai.com}")
  private String fromEmail;

  @Value("${app.email.enabled:false}")
  private boolean emailEnabled;

  @Value("${app.name:Ban Sai Yai Savings Group}")
  private String appName;

  private static final Locale THAI_LOCALE = new Locale("th", "TH");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", THAI_LOCALE);
  private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(THAI_LOCALE);

  /**
   * Send password reset email.
   */
  @Async("emailExecutor")
  public void sendPasswordResetEmail(String toEmail, String resetToken, String resetUrl) {
    if (!emailEnabled) {
      log.info("Email disabled - would send password reset to: {}", toEmail);
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("resetUrl", resetUrl + "?token=" + resetToken);
    context.setVariable("expiryHours", 24);

    sendTemplatedEmail(toEmail, "รีเซ็ตรหัสผ่าน - " + appName, "email/password-reset", context);
  }

  /**
   * Send loan submission notification to approvers (Secretary/President).
   */
  @Async("emailExecutor")
  public void sendLoanSubmissionNotification(User approver, Loan loan) {
    if (!emailEnabled) {
      log.info("Email disabled - would notify {} about loan submission", approver.getUsername());
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("approverName", approver.getUsername());
    context.setVariable("memberName", loan.getMember().getName());
    context.setVariable("loanAmount", formatCurrency(loan.getPrincipalAmount()));
    context.setVariable("loanType", loan.getLoanType().name());
    context.setVariable("applicationDate", formatDate(loan.getCreatedAt()));

    sendTemplatedEmail(approver.getEmail(), "คำขอกู้ใหม่รอการอนุมัติ - " + appName,
        "email/loan-submission-notification", context);
  }

  /**
   * Send loan submission confirmation to member.
   */
  @Async("emailExecutor")
  public void sendLoanSubmissionConfirmationToMember(Member member, Loan loan) {
    if (!emailEnabled || member.getEmail() == null) {
      log.info("Email disabled or no email - would confirm loan submission to member: {}", member.getMemberId());
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("memberName", member.getName());
    context.setVariable("loanAmount", formatCurrency(loan.getPrincipalAmount()));
    context.setVariable("loanType", loan.getLoanType().name());
    context.setVariable("applicationDate", formatDate(loan.getCreatedAt()));
    context.setVariable("loanNumber", loan.getLoanNumber());

    sendTemplatedEmail(member.getEmail(), "ยืนยันการยื่นคำขอกู้ - " + appName,
        "email/loan-submission-confirmation", context);
  }

  /**
   * Send loan approval notification to member.
   */
  @Async("emailExecutor")
  public void sendLoanApprovalNotification(Member member, Loan loan) {
    if (!emailEnabled || member.getEmail() == null) {
      log.info("Email disabled or no email - would notify loan approval to member: {}", member.getMemberId());
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("memberName", member.getName());
    context.setVariable("loanAmount", formatCurrency(loan.getApprovedAmount()));
    context.setVariable("loanNumber", loan.getLoanNumber());
    context.setVariable("interestRate", loan.getInterestRate() + "%");
    context.setVariable("termMonths", loan.getTermMonths());

    sendTemplatedEmail(member.getEmail(), "คำขอกู้ได้รับการอนุมัติแล้ว - " + appName,
        "email/loan-approval-notification", context);
  }

  /**
   * Send loan rejection notification to member.
   */
  @Async("emailExecutor")
  public void sendLoanRejectionNotification(Member member, Loan loan, String rejectionReason) {
    if (!emailEnabled || member.getEmail() == null) {
      log.info("Email disabled or no email - would notify loan rejection to member: {}", member.getMemberId());
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("memberName", member.getName());
    context.setVariable("loanNumber", loan.getLoanNumber());
    context.setVariable("loanAmount", formatCurrency(loan.getPrincipalAmount()));
    context.setVariable("rejectionReason", rejectionReason);

    sendTemplatedEmail(member.getEmail(), "คำขอกู้ไม่ได้รับการอนุมัติ - " + appName,
        "email/loan-rejection-notification", context);
  }

  /**
   * Send payment reminder email.
   */
  @Async("emailExecutor")
  public void sendPaymentReminder(Member member, Loan loan, BigDecimal amountDue, LocalDate dueDate) {
    if (!emailEnabled || member.getEmail() == null) {
      log.info("Email disabled or no email - would send payment reminder to member: {}", member.getMemberId());
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("memberName", member.getName());
    context.setVariable("loanNumber", loan.getLoanNumber());
    context.setVariable("amountDue", formatCurrency(amountDue));
    context.setVariable("dueDate", formatDate(dueDate));

    sendTemplatedEmail(member.getEmail(), "แจ้งเตือนกำหนดชำระ - " + appName,
        "email/payment-reminder", context);
  }

  /**
   * Send welcome email to new member.
   */
  @Async("emailExecutor")
  public void sendWelcomeEmail(Member member) {
    if (!emailEnabled || member.getEmail() == null) {
      log.info("Email disabled or no email - would send welcome email to: {}", member.getMemberId());
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("memberName", member.getName());
    context.setVariable("memberId", member.getMemberId());

    sendTemplatedEmail(member.getEmail(), "ยินดีต้อนรับสู่ " + appName,
        "email/welcome", context);
  }

  /**
   * Send generic notification email.
   */
  @Async("emailExecutor")
  public void sendNotification(String toEmail, String subject, String message) {
    if (!emailEnabled) {
      log.info("Email disabled - would send notification to: {}", toEmail);
      return;
    }

    Context context = new Context();
    context.setVariable("appName", appName);
    context.setVariable("message", message);

    sendTemplatedEmail(toEmail, subject, "email/notification", context);
  }

  private void sendTemplatedEmail(String toEmail, String subject, String templateName, Context context) {
    try {
      String htmlContent = templateEngine.process(templateName, context);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Email sent successfully to: {}", toEmail);

    } catch (MessagingException e) {
      log.error("Failed to send email to: {} - {}", toEmail, e.getMessage());
    }
  }

  private String formatCurrency(BigDecimal amount) {
    if (amount == null)
      return "฿0.00";
    return CURRENCY_FORMAT.format(amount);
  }

  private String formatDate(LocalDate date) {
    if (date == null)
      return "";
    return date.format(DATE_FORMATTER);
  }

  private String formatDate(LocalDateTime dateTime) {
    if (dateTime == null)
      return "";
    return dateTime.toLocalDate().format(DATE_FORMATTER);
  }
}
