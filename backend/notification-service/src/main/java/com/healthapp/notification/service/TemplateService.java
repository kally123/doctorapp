package com.healthapp.notification.service;

import com.healthapp.notification.domain.NotificationChannel;
import com.healthapp.notification.domain.NotificationTemplate;
import com.healthapp.notification.domain.NotificationType;
import com.healthapp.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateService {
    
    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    
    private static final String DEFAULT_LOCALE = "en";
    
    public Mono<NotificationTemplate> getTemplate(NotificationType type, NotificationChannel channel, String locale) {
        String effectiveLocale = locale != null ? locale : DEFAULT_LOCALE;
        
        return templateRepository.findByNotificationTypeAndChannelAndLocaleAndIsActiveTrue(type, channel, effectiveLocale)
                .switchIfEmpty(
                    // Fallback to default locale if not found
                    templateRepository.findByNotificationTypeAndChannelAndLocaleAndIsActiveTrue(type, channel, DEFAULT_LOCALE)
                )
                .switchIfEmpty(
                    // Fallback to any active template for this type and channel
                    templateRepository.findByNotificationTypeAndChannelAndIsActiveTrue(type, channel)
                );
    }
    
    public Mono<ProcessedTemplate> processTemplate(NotificationType type, NotificationChannel channel, 
                                                    String locale, Map<String, Object> variables) {
        return getTemplate(type, channel, locale)
                .map(template -> {
                    Context context = new Context();
                    if (variables != null) {
                        variables.forEach(context::setVariable);
                    }
                    
                    String processedSubject = template.getSubject() != null 
                            ? processInlineTemplate(template.getSubject(), context)
                            : null;
                    
                    String processedBody = processInlineTemplate(template.getBodyTemplate(), context);
                    
                    return ProcessedTemplate.builder()
                            .subject(processedSubject)
                            .body(processedBody)
                            .templateId(template.getId())
                            .build();
                })
                .doOnError(e -> log.error("Error processing template for type: {}, channel: {}", type, channel, e));
    }
    
    private String processInlineTemplate(String template, Context context) {
        try {
            return templateEngine.process(template, context);
        } catch (Exception e) {
            log.warn("Failed to process template: {}", e.getMessage());
            // Return original template if processing fails
            return template;
        }
    }
    
    @lombok.Builder
    @lombok.Data
    public static class ProcessedTemplate {
        private String subject;
        private String body;
        private java.util.UUID templateId;
    }
}
