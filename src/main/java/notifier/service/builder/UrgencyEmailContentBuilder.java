package notifier.service.builder;

import jakarta.enterprise.context.ApplicationScoped;
import notifier.domain.urgency.FeedbackMessage;
import notifier.domain.urgency.UrgencyLevel;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class UrgencyEmailContentBuilder implements EmailContentBuilder<FeedbackMessage> {

    @Override
    public Map<String, String> buildHtmlMessagePlaceholders(FeedbackMessage feedback) {
        UrgencyLevel urgency = UrgencyLevel.fromScore(feedback.nota());
        String sendDate = feedback.dataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        Map<String, String> map = new HashMap<>();
        map.put("descricao", feedback.descricao());
        map.put("urgencyClass", urgency.getCssClass());
        map.put("urgency", urgency.getLabel());
        map.put("nota", String.valueOf(feedback.nota()));
        map.put("sendDate", sendDate);

        return map;
    }
}
