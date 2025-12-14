package notifier;

import java.time.LocalDateTime;

public record FeedbackMessage(
        String id,
        String descricao,
        Integer nota,
        LocalDateTime dataCriacao
) {
}
