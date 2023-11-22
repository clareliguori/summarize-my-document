package wordcloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WordCloudGeneratorUnitTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void generateValidImage() {
        try {
            WordCloudGenerator generator = new WordCloudGenerator();
            WordCloudRequest requestBody = WordCloudRequest.builder().docContent("Hello\nworld").build();
            APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(mapper.writeValueAsString(requestBody));
            Context context = mock(Context.class);
            APIGatewayProxyResponseEvent response = generator.handleRequest(request, context);

            // Validate that the headers and status code are correct for a successful request
            assertEquals(2, response.getHeaders().size());
            assertEquals("application/json", response.getHeaders().get("Content-Type"));
            assertEquals("application/json", response.getHeaders().get("X-Custom-Header"));
            assertEquals(200, response.getStatusCode());

            // Validate that the response contains valid image bytes
            WordCloudResponse responseBody = mapper.readValue(response.getBody(), WordCloudResponse.class);
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(responseBody.getWordCloudImage());
            final ImageInputStream imageInputStream = ImageIO.createImageInputStream(byteArrayInputStream);
            final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
            assertTrue(imageReaders.hasNext());
            final ImageReader imageReader = imageReaders.next();
            assertEquals("png", imageReader.getFormatName());
        } catch(IOException e) {
            fail(e);
        }
    }

    @Test
    public void invalidInputReturnsFailureStatusCode() {
        WordCloudGenerator generator = new WordCloudGenerator();
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
