package notifier.service;

import jakarta.inject.Inject;
import notifier.domain.EmailTemplate;
import notifier.domain.EmailType;
import notifier.service.emailbuilder.EmailContentBuilder;
import notifier.service.emailbuilder.EmailTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class BaseNotificationService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseNotificationService.class);

    @Inject
    EmailTemplateRegistry templateRegistry;

    @Inject
    AdminSubscriptionService adminSubscriptionService;

    @Inject
    protected SesEmailService sesEmailService;

    protected abstract EmailContentBuilder<T> getContentBuilder();

    @FunctionalInterface
    protected interface NotificationSender {
        void send(String email, String subject, String htmlBody);
    }

    protected void processNotification(T data, EmailType emailType, String subject, NotificationSender notificationSender) {
        EmailTemplate template = templateRegistry.getTemplate(emailType);
        Map<String, String> placeholders = getContentBuilder().buildHtmlMessagePlaceholders(data);
        String htmlMessage = template.render(placeholders);

        List<String> adminEmails = adminSubscriptionService.getSubscribedAdminEmails();;

        if (adminEmails.isEmpty()) {
            LOGGER.warn("Nenhum administrador inscrito para receber emails!");
            return;
        }

        LOGGER.info("Enviando emails para {} inscritos...", adminEmails.size());
        LOGGER.info("Emails cadastrados: {}", adminEmails);

        for (String email : adminEmails) {
            notificationSender.send(email, subject, htmlMessage);
        }
    }
}
