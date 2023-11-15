import {
  BedrockRuntimeClient,
  InvokeModelCommand,
} from '@aws-sdk/client-bedrock-runtime';

const HUMAN_PROMPT = "\n\nHuman:";
const AI_PROMPT = "\n\nAssistant:";
const bedrockClient = new BedrockRuntimeClient();


export const handler = async (event: any = {}): Promise<any> => {
  console.log(event);
  const docContent = JSON.parse(event.body)['doc_content'];

  const prompt = `${HUMAN_PROMPT}
I'm going to give you the contents of a document and then I'm going to ask you to give me a summarization of the document.

<document>
${docContent}
</document>

Please give me a short summary of the document.
${AI_PROMPT}`;
  console.log(prompt);

  const modelRequest = {
    prompt,
    max_tokens_to_sample: 500,
    temperature: 1,
  };

  const modelResponse = await bedrockClient.send(
    new InvokeModelCommand({
      modelId: "anthropic.claude-instant-v1",
      contentType: "application/json",
      accept: "*/*",
      body: JSON.stringify(modelRequest),
    }),
  );

  const modelResponseBody = JSON.parse(modelResponse.body.transformToString()).completion;
  const response = { content_summary: modelResponseBody };

  return {
    statusCode: 200,
    body: JSON.stringify(response),
    headers: {
        'Content-Type': 'application/json'
    }
  };
};
