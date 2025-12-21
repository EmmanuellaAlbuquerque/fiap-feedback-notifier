package notifier.domain.report;

public record ReportMessage(
        String subject,
        String body,
        String s3Url
) {
}