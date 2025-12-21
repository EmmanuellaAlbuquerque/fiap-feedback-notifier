package notifier.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.Subscription;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SnsSubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnsSubscriptionService.class);

    @Inject
    SnsClient snsClient;

    public List<String> getSubscribers(String topicArn) {
        ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest
                .builder()
                .topicArn(topicArn)
                .build();

        return snsClient.listSubscriptionsByTopic(request).subscriptions().stream()
                .filter(sub -> "email".equalsIgnoreCase(sub.protocol()) && !"PendingConfirmation".equals(sub.subscriptionArn()))
                .map(Subscription::endpoint)
                .collect(Collectors.toList());
    }
}
