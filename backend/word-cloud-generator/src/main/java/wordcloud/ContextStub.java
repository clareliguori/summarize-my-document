package wordcloud;

import com.amazonaws.services.lambda.runtime.*;


/** For local testing only */
public class ContextStub implements Context {

    private String awsRequestId = "awsRequestId";
    private ClientContext clientContext;
    private String functionName = "functionName";
    private CognitoIdentity identity;
    private String logGroupName = "logGroupName";
    private String logStreamName = "logStreamName";
    private LambdaLogger logger = new StubLogger();
    private int memoryLimitInMB = 512;
    private int remainingTimeInMillis = 15000;
    private String functionVersion = "functionVersion";
    private String invokedFunctionArn = "invokedFunctionArn";

    @Override
    public String getAwsRequestId() {
        return awsRequestId;
    }

    public void setAwsRequestId(String value) {
        awsRequestId = value;
    }

    @Override
    public ClientContext getClientContext() {
        return clientContext;
    }

    public void setClientContext(ClientContext value) {
        clientContext = value;
    }

    @Override
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String value) {
        functionName = value;
    }

    @Override
    public CognitoIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(CognitoIdentity value) {
        identity = value;
    }

    @Override
    public String getLogGroupName() {
        return logGroupName;
    }

    public void setLogGroupName(String value) {
        logGroupName = value;
    }

    @Override
    public String getLogStreamName() {
        return logStreamName;
    }

    public void setLogStreamName(String value) {
        logStreamName = value;
    }

    @Override
    public LambdaLogger getLogger() {
        return logger;
    }

    public void setLogger(LambdaLogger value) {
        logger = value;
    }

    @Override
    public int getMemoryLimitInMB() {
        return memoryLimitInMB;
    }

    public void setMemoryLimitInMB(int value) {
        memoryLimitInMB = value;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return remainingTimeInMillis;
    }

    public void setRemainingTimeInMillis(int value) {
        remainingTimeInMillis = value;
    }

    @Override
    public String getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(String value) {
        functionVersion = value;
    }

    @Override
    public String getInvokedFunctionArn() {
        return invokedFunctionArn;
    }

    public void setInvokedFunctionArn(String value) {
        invokedFunctionArn = value;
    }

    private static class StubLogger implements LambdaLogger {

        @Override
        public void log(String message) {
            System.out.println(message);
        }

        @Override
        public void log(byte[] message) {
            System.out.println(message);
        }
    }
}