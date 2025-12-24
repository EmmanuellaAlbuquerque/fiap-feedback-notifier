package notifier.domain.urgency;

public enum UrgencyLevel {
    HIGH("ALTA", "alta"),
    MEDIUM("MÉDIA", "media"),
    LOW("BAIXA", "baixa");

    private final String label;
    private final String cssClass;

    UrgencyLevel(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public static UrgencyLevel fromScore(int score) {
        if (score <= 2) return HIGH;
        if (score < 4) return MEDIUM;
        return LOW;
    }
}
