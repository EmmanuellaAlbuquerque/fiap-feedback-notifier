package notifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UrgencyNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrgencyNotificationService.class);

    @Inject
    EmailTemplateRegistry templateRegistry;

    @Inject
    UrgencyEmailContentBuilder contentBuilder;

    @Inject
    SnsSubscriptionService snsSubscriptionService;

    @Inject
    SesEmailService sesEmailService;

    @ConfigProperty(name = "sns.urgency.topic.arn")
    String urgencyTopicArn;

    public void notifyUrgency(FeedbackMessage feedback) {
        UrgencyLevel urgency = UrgencyLevel.fromScore(feedback.nota());
        String subject = "Feedback Crítico - Urgência: " + urgency.getLabel();

        EmailTemplate template = templateRegistry.getTemplate(EmailType.URGENCY);
        Map<String, String> placeholders = contentBuilder.buildHtmlMessagePlaceholders(feedback);
        String htmlMessage = template.render(placeholders);

        List<String> subscribers = snsSubscriptionService.getSubscribers(urgencyTopicArn);

        if (subscribers.isEmpty()) {
            LOGGER.warn("Nenhum inscrito encontrado para o tópico SNS: {}", urgencyTopicArn);
            return;
        }

        LOGGER.info("Enviando notificação para {} inscritos...", subscribers.size());

        for (String email : subscribers) {
            sesEmailService.sendEmail(email, subject, htmlMessage);
        }
    }
}
