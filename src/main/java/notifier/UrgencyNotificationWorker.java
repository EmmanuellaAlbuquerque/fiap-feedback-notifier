package notifier;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Named("urgencyNotificationWorker")
public class UrgencyNotificationWorker implements RequestHandler<SQSEvent, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrgencyNotificationWorker.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    UrgencyNotificationService urgencyNotificationService;

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        LOGGER.info("Processing {} records", event.getRecords().size());

        event.getRecords().forEach(record -> {
            try {
                FeedbackMessage feedback = objectMapper.readValue(record.getBody(), FeedbackMessage.class);
                LOGGER.info("Processing feedback: {}", feedback.toString());
                urgencyNotificationService.notifyUrgency(feedback);
            } catch (Exception exception) {
                LOGGER.error("Erro ao processar mensagem SQS: {}", record.getBody(), exception);
                throw new RuntimeException("Erro ao processar mensagem SQS", exception);
            }
        });

        return null;
    }
}