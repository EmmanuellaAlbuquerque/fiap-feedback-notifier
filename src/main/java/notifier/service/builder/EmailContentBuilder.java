package notifier.service.builder;

import java.util.Map;

public interface EmailContentBuilder<T> {
    Map<String, String> buildHtmlMessagePlaceholders(T data);
}
