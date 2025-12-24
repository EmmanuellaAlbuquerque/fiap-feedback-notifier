package notifier.domain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class EmailTemplate {

    private final String rawTemplate;
    private final String rawStyles;

    public EmailTemplate(String templatePath, String stylesPath) {
        this.rawTemplate = loadResourceFile(templatePath);
        this.rawStyles = loadResourceFile(stylesPath);
    }

    public String render(Map<String, String> placeholders) {
        String html = rawTemplate.replace("{{styles}}", rawStyles);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
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
