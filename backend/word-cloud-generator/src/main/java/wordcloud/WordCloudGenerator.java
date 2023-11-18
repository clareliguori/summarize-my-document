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

import sun.misc.BASE64Encoder;

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

            WordCloudResponse imgResponse = WordCloudResponse.builder().wordCloudImage(imgData).build();
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
}
