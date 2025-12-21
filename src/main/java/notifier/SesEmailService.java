package notifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import static jakarta.mail.Message.RecipientType.TO;

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

    public void sendEmailWithAttachment(String recipientEmail, String subject, String htmlBody, byte[] attachment, String attachmentName) {
        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setSubject(subject, "UTF-8");
            mimeMessage.setFrom(new InternetAddress(senderEmail));
            mimeMessage.setRecipients(TO, InternetAddress.parse(recipientEmail));

            MimeMultipart msgBody = new MimeMultipart("mixed");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
            msgBody.addBodyPart(htmlPart);

            if (attachment != null && attachment.length > 0) {
                MimeBodyPart attPart = new MimeBodyPart();
                attPart.setFileName(attachmentName);
                attPart.setContent(attachment, "application/pdf");
                msgBody.addBodyPart(attPart);
            }

            mimeMessage.setContent(msgBody);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mimeMessage.writeTo(outputStream);
            ByteBuffer buf = ByteBuffer.wrap(outputStream.toByteArray());

            RawMessage rawMessage = RawMessage.builder().data(SdkBytes.fromByteBuffer(buf)).build();

            LOGGER.info("RawMessage: {}", rawMessage);
            LOGGER.info("htmlBody: {}", htmlBody);

            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .source(senderEmail)
                    .destinations(recipientEmail)
                    .build();

            sesClient.sendRawEmail(rawEmailRequest);
            LOGGER.info("E-mail com anexo enviado com sucesso para: {}", recipientEmail);

        } catch (MessagingException | IOException | SesException e) {
            LOGGER.error("Falha ao enviar e-mail com anexo para {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail com anexo", e);
        }
    }
}
