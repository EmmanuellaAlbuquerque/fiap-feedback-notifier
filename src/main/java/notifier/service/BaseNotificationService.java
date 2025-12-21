package notifier.service;

import jakarta.inject.Inject;
import notifier.domain.EmailTemplate;
import notifier.domain.EmailType;
import notifier.service.builder.EmailContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class BaseNotificationService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseNotificationService.class);

    @Inject
    EmailTemplateRegistry templateRegistry;

    @Inject
    SnsSubscriptionService snsSubscriptionService;

    @Inject
    protected SesEmailService sesEmailService;

    protected abstract EmailContentBuilder<T> getContentBuilder();

    @FunctionalInterface
    protected interface NotificationSender {
        void send(String email, String subject, String htmlBody);
    }

    protected void processNotification(T data, EmailType emailType, String topicArn, String subject, NotificationSender notificationSender) {
        EmailTemplate template = templateRegistry.getTemplate(emailType);
        Map<String, String> placeholders = getContentBuilder().buildHtmlMessagePlaceholders(data);
        String htmlMessage = template.render(placeholders);

        List<String> subscribers = snsSubscriptionService.getSubscribers(topicArn);

        if (subscribers.isEmpty()) {
            LOGGER.warn("Nenhum inscrito encontrado para o tópico SNS: {}", topicArn);
            return;
        }

        LOGGER.info("Enviando notificação para {} inscritos...", subscribers.size());

        for (String email : subscribers) {
            notificationSender.send(email, subject, htmlMessage);
        }
    }
}
