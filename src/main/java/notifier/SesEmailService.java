package notifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@ApplicationScoped
public class SesEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SesEmailService.class);

    @Inject
    SesClient sesClient;

    @ConfigProperty(name = "ses.sender.email")
    String senderEmail;

    public void sendEmail(String recipientEmail, String subject, String htmlBody) {
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
