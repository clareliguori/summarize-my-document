package wordcloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class WordCloudGeneratorUnitTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void generateValidImage() {
        try {
            Context context = mock(Context.class);
            S3Client mockS3 = mock(S3Client.class);
            S3Presigner mockPresigner = mock(S3Presigner.class);
            PresignedGetObjectRequest presignedGetObjectRequest = mock(PresignedGetObjectRequest.class);
            when(mockS3.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
            when(mockPresigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGetObjectRequest);
            when(presignedGetObjectRequest.url()).thenReturn(new URL("https://example.com/image.png"));

            WordCloudGenerator generator = new WordCloudGenerator(mockS3, mockPresigner);
            WordCloudRequest requestBody = WordCloudRequest.builder().docContent("Hello\nworld").build();
            APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(mapper.writeValueAsString(requestBody));

            APIGatewayProxyResponseEvent response = generator.handleRequest(request, context);

            // Validate that the headers and status code are correct for a successful request
            assertEquals(2, response.getHeaders().size());
            assertEquals("application/json", response.getHeaders().get("Content-Type"));
            assertEquals("application/json", response.getHeaders().get("X-Custom-Header"));
            assertEquals(200, response.getStatusCode());

            // Validate that the image is uploaded to S3
            // and the response contains a pre-signed URL
            WordCloudResponse responseBody = mapper.readValue(response.getBody(), WordCloudResponse.class);
            assertEquals("https://example.com/image.png", responseBody.getWordCloudUrl());
        } catch(IOException e) {
            fail(e);
        }
    }

    @Test
    public void invalidInputReturnsFailureStatusCode() {
        S3Client mockS3 = mock(S3Client.class);
        S3Presigner mockPresigner = mock(S3Presigner.class);
        WordCloudGenerator generator = new WordCloudGenerator(mockS3, mockPresigner);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody("Hello world");
        Context context = mock(Context.class);
        APIGatewayProxyResponseEvent response = generator.handleRequest(request, context);

        // Validate that the headers and status code are correct for a failed request
        assertEquals(2, response.getHeaders().size());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertEquals("application/json", response.getHeaders().get("X-Custom-Header"));
        assertEquals(500, response.getStatusCode());

        // Validate that the response is an empty JSON object
        assertEquals("{}", response.getBody());
    }
}
