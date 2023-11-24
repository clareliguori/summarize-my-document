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
    results: [{
      outputText: "  This document is... ",
    }],
  };
  bedrockMock.on(InvokeModelCommand).resolves({
      body: Uint8ArrayBlobAdapter.fromString(JSON.stringify(mockModelResponse)),
  });

  const testEvent = {
    body: JSON.stringify({doc_content: "Hello\nworld"}),
  };

  const handlerResponse = await handler(testEvent);

  expect(bedrockMock).toHaveReceivedCommandTimes(InvokeModelCommand, 1);

  const expectedPrompt = `Document:
Hello
world
--------------------------------
From the document contents above, give me a short summary of the key points in the document.
`;

  const expectedModelParams = {
    inputText: expectedPrompt,
    textGenerationConfig: {
      maxTokenCount: 500,
      temperature: 1,
    },
  };

  expect(bedrockMock).toHaveReceivedCommandWith(InvokeModelCommand, {
    modelId: "amazon.titan-text-express-v1",
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
