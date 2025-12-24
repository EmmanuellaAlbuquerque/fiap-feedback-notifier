package notifier.service.urgency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import notifier.domain.EmailType;
import notifier.domain.urgency.FeedbackMessage;
import notifier.domain.urgency.UrgencyLevel;
import notifier.service.BaseNotificationService;
import notifier.service.emailbuilder.EmailContentBuilder;
import notifier.service.emailbuilder.UrgencyEmailContentBuilder;

@ApplicationScoped
public class UrgencyNotificationService extends BaseNotificationService<FeedbackMessage> {

    @Inject
    UrgencyEmailContentBuilder contentBuilder;

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
            subject,
            (email, subj, body) -> sesEmailService.sendEmail(email, subj, body)
        );
    }
}
