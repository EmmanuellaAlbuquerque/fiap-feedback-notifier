package notifier.domain.urgency;

import java.time.LocalDateTime;

public record FeedbackMessage(
        String id,
        String descricao,
        Integer nota,
        LocalDateTime dataCriacao
) {
}
