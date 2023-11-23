# Backend App

## Content summary generator

## Run content-summary-generator locally

```sh
npm install -g node-lambda

npm install

npm run build

node-lambda run --apiGateway
```

## Test content-summary-generator

```sh
npm test
```

## Word cloud generator

## Run word-cloud-generator locally

```sh
mvn compile test-compile

BUCKET_NAME=my-s3-bucket mvn exec:java -Dexec.mainClass="wordcloud.WordCloudGeneratorLocalInvoke" -Dexec.classpathScope=test
```

## Test word-cloud-generator

```sh
mvn clean compile test
```
