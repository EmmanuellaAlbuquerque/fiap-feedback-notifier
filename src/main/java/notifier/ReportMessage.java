package notifier;

public record ReportMessage(
        String subject,
        String body,
        String s3Url
) {
}