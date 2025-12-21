package notifier.service.report;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import notifier.config.S3Service;
import notifier.domain.EmailType;
import notifier.domain.report.ReportMessage;
import notifier.service.BaseNotificationService;
import notifier.service.builder.EmailContentBuilder;
import notifier.service.builder.ReportEmailContentBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ReportNotificationService extends BaseNotificationService<ReportMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportNotificationService.class);

    @Inject
    ReportEmailContentBuilder contentBuilder;

    @Inject
    S3Service s3Service;

    @ConfigProperty(name = "sns.report.subscription.topic.arn")
    String reportSubscriptionTopicArn;

    @Override
    protected EmailContentBuilder<ReportMessage> getContentBuilder() {
        return contentBuilder;
    }

    public void notifyReport(ReportMessage reportMessage) {
        byte[] pdfContent;
        String attachmentName = "relatorio.pdf";

        try {
            pdfContent = s3Service.downloadFile(reportMessage.s3Url());
        } catch (Exception exception) {
            LOGGER.error("Não foi possível baixar o anexo do S3: {}", reportMessage.s3Url(), exception);
            throw exception;
        }

        processNotification(
            reportMessage,
            EmailType.REPORT,
            reportSubscriptionTopicArn,
            reportMessage.subject(),
            (email, subj, body) -> sesEmailService.sendEmailWithAttachment(email, subj, body, pdfContent, attachmentName)
        );
    }
}
