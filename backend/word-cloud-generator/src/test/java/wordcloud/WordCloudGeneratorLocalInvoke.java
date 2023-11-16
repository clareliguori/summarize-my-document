package wordcloud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * For local testing
 */
public class WordCloudGeneratorLocalInvoke {

    public static void main(String[] args) {
        WordCloudGenerator generator = new WordCloudGenerator();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody("{\"doc_content\": \"Hello world\"}");
        Context context = new ContextStub();
        APIGatewayProxyResponseEvent response = generator.handleRequest(request, context);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Body:" + response.getBody());
    }
}
