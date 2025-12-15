package notifier;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmailTemplateService {

    private final String emailTemplate;
    private final String emailStyles;

    public EmailTemplateService() {
        this.emailTemplate = loadResourceFile("templates/email-template.html");
        this.emailStyles = loadResourceFile("templates/email-styles.css");
    }

    public String buildHtmlMessage(FeedbackMessage feedback) {
        UrgencyLevel urgency = UrgencyLevel.fromScore(feedback.nota());
        String sendDate = feedback.dataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return emailTemplate
                .replace("{{styles}}", emailStyles)
                .replace("{{descricao}}", feedback.descricao())
                .replace("{{urgencyClass}}", urgency.getCssClass())
                .replace("{{urgency}}", urgency.getLabel())
                .replace("{{nota}}", String.valueOf(feedback.nota()))
                .replace("{{sendDate}}", sendDate);
    }

    private String loadResourceFile(String path) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Não foi possível encontrar o arquivo de recurso: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar o arquivo de recurso: " + path, e);
        }
    }
}
