package notifier.service.emailbuilder;

import jakarta.enterprise.context.ApplicationScoped;
import notifier.domain.report.ReportMessage;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ReportEmailContentBuilder implements EmailContentBuilder<ReportMessage> {
    @Override
    public Map<String, String> buildHtmlMessagePlaceholders(ReportMessage reportMessage) {
        Map<String, String> map = new HashMap<>();
        map.put("titulo", reportMessage.subject());
        map.put("descricao", reportMessage.body());

        return map;
    }
}
