package notifier.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URI;
import java.net.URISyntaxException;

@ApplicationScoped
public class S3Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Service.class);

    @Inject
    S3Client s3Client;

    public byte[] downloadFile(String s3Url) {
        try {
            URI uri = new URI(s3Url);
            String host = uri.getHost();
            String path = uri.getPath();
            String bucketName = host.substring(0, host.indexOf(".s3."));
            String key = path != null && path.startsWith("/") ? path.substring(1) : path;

            LOGGER.info("Downloading file from bucket: {}, key: {}", bucketName, key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();

        } catch (URISyntaxException e) {
            LOGGER.error("Invalid S3 URL: {}", s3Url, e);
            throw new RuntimeException("Invalid S3 URL", e);
        } catch (Exception e) {
            LOGGER.error("Error downloading file from S3: {}", s3Url, e);
            throw new RuntimeException("Error downloading file from S3", e);
        }
    }
}
