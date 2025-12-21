package notifier;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ReportEmailContentBuilder {
    public Map<String, String> buildHtmlMessagePlaceholders(ReportMessage reportMessage) {
        Map<String, String> map = new HashMap<>();
        map.put("titulo", reportMessage.subject());
        map.put("descricao", reportMessage.body());

        return map;
    }
}
