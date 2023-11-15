package wordcloud;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for requests to Lambda function.
 */
public class WordCloudGenerator implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            String output = "{ \"message\": \"hello world\" }";

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Exception e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    // For testing locally
    public static void main(String[] args) {
        WordCloudGenerator generator = new WordCloudGenerator();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody("{\"doc-content\": \"Hello world\"}");
        Context context = new ContextStub();
        APIGatewayProxyResponseEvent response = generator.handleRequest(request, context);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Body:" + response.getBody());
    }
}
