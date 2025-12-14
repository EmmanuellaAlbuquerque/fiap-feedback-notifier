package notifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.Subscription;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Inject
    SnsClient snsClient;

    @Inject
    SesClient sesClient;

    @Inject
    EmailTemplateService emailTemplateService;

    @ConfigProperty(name = "sns.urgency.topic.arn")
    String urgencyTopicArn;

    @ConfigProperty(name = "ses.sender.email")
    String senderEmail;

    public void notifyUrgency(FeedbackMessage feedback) {
        String htmlMessage = emailTemplateService.buildHtmlMessage(feedback);
        UrgencyLevel urgency = UrgencyLevel.fromScore(feedback.nota());
        String subject = "Feedback Crítico - Urgência: " + urgency.getLabel();

        List<String> subscribers = getSubscribers();

        if (subscribers.isEmpty()) {
            LOGGER.warn("Nenhum inscrito encontrado para o tópico SNS: {}", urgencyTopicArn);
            return;
        }

        LOGGER.info("Enviando notificação para {} inscritos...", subscribers.size());

        for (String email : subscribers) {
            sendEmail(email, subject, htmlMessage);
        }
    }

    private List<String> getSubscribers() {
        ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder()
                .topicArn(urgencyTopicArn)
                .build();

        return snsClient.listSubscriptionsByTopic(request).subscriptions().stream()
                .filter(sub -> "email".equalsIgnoreCase(sub.protocol()) && !"PendingConfirmation".equals(sub.subscriptionArn()))
                .map(Subscription::endpoint)
                .collect(Collectors.toList());
    }

    private void sendEmail(String recipientEmail, String subject, String htmlBody) {
        try {
            Destination destination = Destination.builder().toAddresses(recipientEmail).build();
            Content subjectContent = Content.builder().data(subject).charset("UTF-8").build();
            Content htmlContent = Content.builder().data(htmlBody).charset("UTF-8").build();
            Body body = Body.builder().html(htmlContent).build();
            Message message = Message.builder().subject(subjectContent).body(body).build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(destination)
                    .message(message)
                    .build();

            sesClient.sendEmail(request);
            LOGGER.info("E-mail enviado com sucesso para: {}", recipientEmail);

        } catch (SesException e) {
            LOGGER.error("Falha ao enviar e-mail para {}: {}", recipientEmail, e.awsErrorDetails().errorMessage());
        }
    }
}
