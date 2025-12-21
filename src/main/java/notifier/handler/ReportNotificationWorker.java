package notifier.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import notifier.domain.report.ReportMessage;
import notifier.service.report.ReportNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Named("reportNotificationWorker")
public class ReportNotificationWorker implements RequestHandler<SNSEvent, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportNotificationWorker.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ReportNotificationService reportNotificationService;

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        LOGGER.info("Processing {} SNS records (report)", event.getRecords().size());

        event.getRecords().forEach(record -> {
            try {
                String messageJson = record.getSNS().getMessage();
                ReportMessage report = objectMapper.readValue(messageJson, ReportMessage.class);

                LOGGER.info("Processing report: {}", report);
                reportNotificationService.notifyReport(report);

            } catch (Exception e) {
                LOGGER.error("Erro ao processar mensagem SNS (report): {}", record.getSNS().getMessage(), e);
                throw new RuntimeException("Erro ao processar mensagem SNS (report)", e);
            }
        });

        return null;
    }
}
