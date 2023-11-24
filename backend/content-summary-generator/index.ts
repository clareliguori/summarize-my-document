import {
  BedrockRuntimeClient,
  InvokeModelCommand,
} from '@aws-sdk/client-bedrock-runtime';

const bedrockClient = new BedrockRuntimeClient({maxAttempts: 6});

export const handler = async (event: any = {}): Promise<any> => {
  const docContent = JSON.parse(event.body)['doc_content'];

  const prompt = `Document:
${docContent}
--------------------------------
From the document contents above, give me a short summary of the key points in the document.
`;

  const modelRequest = {
    inputText: prompt,
    textGenerationConfig: {
      maxTokenCount: 500,
      temperature: 1,
    },
  };

  const modelResponse = await bedrockClient.send(
    new InvokeModelCommand({
      modelId: "amazon.titan-text-express-v1",
      contentType: "application/json",
      accept: "*/*",
      body: JSON.stringify(modelRequest),
    }),
  );

  const modelResponseBody = JSON.parse(modelResponse.body.transformToString()).results[0].outputText;
  const response = { content_summary: modelResponseBody.trim() };

  return {
    statusCode: 200,
    body: JSON.stringify(response),
    headers: {
        'Content-Type': 'application/json'
    }
  };
};
