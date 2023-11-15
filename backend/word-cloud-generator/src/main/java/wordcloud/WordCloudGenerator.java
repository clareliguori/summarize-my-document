package wordcloud;

import java.awt.Dimension;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler for requests to Lambda function.
 */
public class WordCloudGenerator implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            logger.debug("Request: " + input.getBody());
            WordCloudRequest request = mapper.readValue(input.getBody(), WordCloudRequest.class);

            final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
            final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(Arrays.asList(request.getDocContent().split(System.lineSeparator())));
            final Dimension dimension = new Dimension(600, 600);
            final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
            wordCloud.setPadding(0);
            wordCloud.setBackground(new RectangleBackground(dimension));
            wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE));
            wordCloud.setFontScalar(new LinearFontScalar(10, 40));
            wordCloud.build(wordFrequencies);

            ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
            wordCloud.writeToStreamAsPNG(imgOutput);
            WordCloudResponse imgResponse = WordCloudResponse.builder().wordCloudImage(imgOutput.toByteArray()).build();
            String serializedImg = mapper.writeValueAsString(imgResponse);

            return response
                    .withStatusCode(200)
                    .withBody(serializedImg);
        } catch (Exception e) {
            logger.error(e);
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    // For testing locally
    public static void main(String[] args) {
        WordCloudGenerator generator = new WordCloudGenerator();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody("{\"doc_content\": \"Hello world\"}");
        Context context = new ContextStub();
        APIGatewayProxyResponseEvent response = generator.handleRequest(request, context);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Body:" + response.getBody());
    }
}
