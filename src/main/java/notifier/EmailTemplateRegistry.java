package notifier;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.EnumMap;
import java.util.Map;

@ApplicationScoped
public class EmailTemplateRegistry {

    private final Map<EmailType, EmailTemplate> templates = new EnumMap<>(EmailType.class);

    @PostConstruct
    void init() {
        templates.put(EmailType.URGENCY, new EmailTemplate(
                "templates/urgency/urgency-email-template.html",
                "templates/urgency/urgency-email-styles.css"
        ));

        templates.put(EmailType.REPORT, new EmailTemplate(
                "templates/report/report-email-template.html",
                "templates/report/report-email-styles.css"
        ));
    }

    public EmailTemplate getTemplate(EmailType type) {
        EmailTemplate template = templates.get(type);

        if (template == null) {
            throw new IllegalArgumentException("Template não configurado para tipo: " + type);
        }

        return template;
    }
}