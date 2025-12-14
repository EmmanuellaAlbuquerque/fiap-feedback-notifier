package notifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ApplicationScoped
public class NotificationService {

    @Inject
    SnsClient snsClient;

    @Inject
    EmailTemplateService emailTemplateService;

    @ConfigProperty(name = "sns.urgency.topic.arn")
    String urgencyTopicArn;

    public void notifyUrgency(FeedbackMessage feedback) {
        String htmlMessage = emailTemplateService.buildHtmlMessage(feedback);
        UrgencyLevel urgency = UrgencyLevel.fromScore(feedback.nota());

        snsClient.publish(
                PublishRequest.builder()
                        .topicArn(urgencyTopicArn)
                        .subject("Feedback Crítico - Urgência: " + urgency.getLabel())
                        .message(htmlMessage)
                        .build()
        );
    }
}
