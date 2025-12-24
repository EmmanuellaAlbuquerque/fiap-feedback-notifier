package notifier.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AdminSubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSubscriptionService.class);

    @Inject
    DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "subscribed.admin.table")
    String adminTableName;

    public List<String> getSubscribedAdminEmails() {
        List<String> adminEmails = new ArrayList<>();

        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(adminTableName)
                    .projectionExpression("email")
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                AttributeValue emailAttributeValue = item.get("email");
                adminEmails.add(emailAttributeValue.s());
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching subscribers from DynamoDB", e);
        }

        return adminEmails;
    }
}
