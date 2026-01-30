package com.healthapp.notification.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailProvider implements EmailProvider {
    
    private final JavaMailSender mailSender;
    
    @Override
    public Mono<Boolean> sendEmail(String to, String subject, String htmlBody, Map<String, Object> metadata) {
        return Mono.fromCallable(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                
                // Add custom headers from metadata
                if (metadata != null) {
                    if (metadata.containsKey("from")) {
                        helper.setFrom((String) metadata.get("from"));
                    }
                    if (metadata.containsKey("replyTo")) {
                        helper.setReplyTo((String) metadata.get("replyTo"));
                    }
                }
                
                mailSender.send(message);
                log.info("Email sent successfully to: {}", to);
                return true;
            } catch (MessagingException e) {
                log.error("Failed to send email to: {}", to, e);
                throw new RuntimeException("Failed to send email", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorReturn(false);
    }
    
    @Override
    public Mono<Boolean> sendEmailWithTemplate(String to, String templateId, Map<String, Object> variables) {
        // For SMTP, we don't use provider-side templates
        // Templates are processed locally using Thymeleaf
        return Mono.just(false);
    }
    
    @Override
    public String getProviderName() {
        return "SMTP";
    }
}
