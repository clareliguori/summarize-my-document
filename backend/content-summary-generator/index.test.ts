import {
  BedrockRuntimeClient,
  InvokeModelCommand,
} from '@aws-sdk/client-bedrock-runtime';
import { Uint8ArrayBlobAdapter } from "@smithy/util-stream";
import {mockClient} from 'aws-sdk-client-mock';
import 'aws-sdk-client-mock-jest';

import { handler } from "./index";

it('call Bedrock to summarize content', async () => {
  const bedrockMock = mockClient(BedrockRuntimeClient);
  const mockModelResponse = {
    completion: "  This document is... ",
  };
  bedrockMock.on(InvokeModelCommand).resolves({
      body: Uint8ArrayBlobAdapter.fromString(JSON.stringify(mockModelResponse)),
  });

  const testEvent = {
    body: JSON.stringify({doc_content: "Hello\nworld"}),
  };

  const handlerResponse = await handler(testEvent);

  expect(bedrockMock).toHaveReceivedCommandTimes(InvokeModelCommand, 1);

  const expectedPrompt = `

Human:
I'm going to give you the contents of a document and then I'm going to ask you to give me a summarization of the document.

<document>
Hello
world
</document>

Please give me a short summary of the document.


Assistant:`;

  const expectedModelParams = {
    prompt: expectedPrompt,
    max_tokens_to_sample: 500,
    temperature: 1,
  };

  expect(bedrockMock).toHaveReceivedCommandWith(InvokeModelCommand, {
    modelId: "anthropic.claude-instant-v1",
    contentType: "application/json",
    accept: "*/*",
    body: JSON.stringify(expectedModelParams),
  });

  expect(handlerResponse).toEqual({
    statusCode: 200,
    body: '{"content_summary":"This document is..."}',
    headers: {
        'Content-Type': 'application/json'
    }
  });
});
