package notifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ReportNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportNotificationService.class);

    @Inject
    EmailTemplateRegistry templateRegistry;

    @Inject
    ReportEmailContentBuilder contentBuilder;

    @Inject
    SnsSubscriptionService snsSubscriptionService;

    @Inject
    SesEmailService sesEmailService;

    @Inject
    S3Service s3Service;

    @ConfigProperty(name = "sns.report.subscription.topic.arn")
    String reportSubscriptionTopicArn;

    public void notifyReport(ReportMessage reportMessage) {
        byte[] pdfContent;
        try {
            pdfContent = s3Service.downloadFile(reportMessage.s3Url());
        } catch (Exception exception) {
            LOGGER.error("Não foi possível baixar o anexo do S3: {}", reportMessage.s3Url(), exception);
            throw exception;
        }

        EmailTemplate template = templateRegistry.getTemplate(EmailType.REPORT);
        Map<String, String> placeholders = contentBuilder.buildHtmlMessagePlaceholders(reportMessage);
        String htmlMessage = template.render(placeholders);

        List<String> subscribers = snsSubscriptionService.getSubscribers(reportSubscriptionTopicArn);

        if (subscribers.isEmpty()) {
            LOGGER.warn("Nenhum inscrito encontrado para o tópico SNS: {}", reportSubscriptionTopicArn);
            return;
        }

        LOGGER.info("Enviando notificação para {} inscritos...", subscribers.size());

        for (String email : subscribers) {
            sesEmailService.sendEmailWithAttachment(
                    email,
                    reportMessage.subject(),
                    htmlMessage,
                    pdfContent,
                    "relatorio.pdf"
            );
        }
    }
}
