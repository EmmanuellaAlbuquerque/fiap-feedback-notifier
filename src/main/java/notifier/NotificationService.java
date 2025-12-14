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

    @ConfigProperty(name = "sns.urgency.topic.arn")
    String urgencyTopicArn;

    public void notifyUrgency(FeedbackMessage feedback) {

        String message = """
            Feedback Crítico Recebido

            Nota: %d
            Descrição: %s
            Data: %s
            
            """.formatted(
                feedback.nota(),
                feedback.descricao(),
                feedback.dataCriacao()
        );

        snsClient.publish(
                PublishRequest.builder()
                        .topicArn(urgencyTopicArn)
                        .message(message)
                        .build()
        );
    }
}
