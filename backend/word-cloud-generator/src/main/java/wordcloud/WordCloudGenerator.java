package wordcloud;

import java.awt.Dimension;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.misc.BASE64Encoder;

/**
 * Main logic for the word cloud API.
 */
public class WordCloudGenerator implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper();
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public WordCloudGenerator() {
        s3Client = S3Client.builder()
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();
        s3Presigner = S3Presigner.create();
    }

    public WordCloudGenerator(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Handler for API Gateway requests to the Lambda function that generates the word cloud image.
     */
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            // Parse the request
            logger.debug("Request: " + input.getBody());
            WordCloudRequest request = mapper.readValue(input.getBody(), WordCloudRequest.class);

            // Analyze the content provided in the request,
            // and generate the word cloud image
            final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
            final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(Arrays.asList(request.getDocContent().split(System.lineSeparator())));
            Integer side = new Integer(400);
            final Dimension dimension = new Dimension(side, side);
            final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
            wordCloud.setPadding(0);
            wordCloud.setBackground(new RectangleBackground(dimension));

            wordCloud.setBackgroundColor(Color.WHITE);
            wordCloud.setColorPalette(new ColorPalette(
                new Color(0x7ce8f4), // Cyan
                new Color(0x7c5aed), // Violet
                new Color(0xdf2a5d), // Cosmos
                new Color(0x330066), // Galaxy
                new Color(0x232f3e)  // Squid Ink
            ));

            wordCloud.setFontScalar(new LinearFontScalar(10, 80));
            wordCloud.build(wordFrequencies);

            ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
            wordCloud.writeToStreamAsPNG(imgOutput);
            byte[] imgData = imgOutput.toByteArray();

            logger.debug("Img data:");
            logger.debug(new BASE64Encoder().encode(imgData));

            // Upload the image to S3 and generate a pre-signed URL
            String bucketName = System.getenv("BUCKET_NAME");
            if (bucketName == null) {
                // just to make unit tests work for now, will be removed later
                logger.warn("BUCKET_NAME environment variable not set, using default bucket name");
                bucketName = "XXXXXXXXXXXXXXXX";
            }
            String key = uploadImageToS3(imgData, bucketName);
            String url = generatePresignedURL(bucketName, key);

            // Add the word cloud URL to the response
            WordCloudResponse imgResponse = WordCloudResponse.builder().wordCloudUrl(url).build();
            String serializedResponse = mapper.writeValueAsString(imgResponse);

            return response
                    .withStatusCode(200)
                    .withBody(serializedResponse);
        } catch (Exception e) {
            logger.error(e);
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    private String uploadImageToS3(byte[] image, String bucketName) {
        String key = "wordcloud" + UUID.randomUUID().toString() + ".png";
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(objectRequest, RequestBody.fromBytes(image));
        return key;
    }

    private String generatePresignedURL(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toString();
    }
}
