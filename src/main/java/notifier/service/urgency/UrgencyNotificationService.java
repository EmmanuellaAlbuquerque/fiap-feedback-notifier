package notifier.service.urgency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import notifier.domain.EmailType;
import notifier.domain.urgency.FeedbackMessage;
import notifier.domain.urgency.UrgencyLevel;
import notifier.service.BaseNotificationService;
import notifier.service.builder.EmailContentBuilder;
import notifier.service.builder.UrgencyEmailContentBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UrgencyNotificationService extends BaseNotificationService<FeedbackMessage> {

    @Inject
    UrgencyEmailContentBuilder contentBuilder;

    @ConfigProperty(name = "sns.urgency.topic.arn")
    String urgencyTopicArn;

    @Override
    protected EmailContentBuilder<FeedbackMessage> getContentBuilder() {
        return contentBuilder;
    }

    public void notifyUrgency(FeedbackMessage feedback) {
        UrgencyLevel urgency = UrgencyLevel.fromScore(feedback.nota());
        String subject = "Feedback Crítico - Urgência: " + urgency.getLabel();

        processNotification(
            feedback, 
            EmailType.URGENCY,
            urgencyTopicArn, 
            subject,
            (email, subj, body) -> sesEmailService.sendEmail(email, subj, body)
        );
    }
}
