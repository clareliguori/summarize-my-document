from aws_cdk import (
    BundlingOptions,
    BundlingOutput,
    CfnOutput,
    Duration,
    RemovalPolicy,
    Stack,
    aws_apigateway as apigw,
    aws_certificatemanager as acm,
    aws_iam as iam,
    aws_lambda as _lambda,
    aws_lambda_nodejs as nodejs,
    aws_logs as logs,
    aws_route53 as route53,
    aws_route53_targets as route53_targets,
    aws_s3 as s3,
)
from constructs import Construct
from pathlib import Path
from os import path


# Deploy an API Gateway that serves the content summary generator API and word cloud generator API
class BackendStack(Stack):
    def __init__(
        self, scope: Construct, construct_id: str, parent_domain: str, **kwargs
    ) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # Content summary generator Lambda function, written in Typescript
        function_code_dir = (
            Path(__file__).absolute().parent.parent.__str__() + "/backend/"
        )
        summary_lambda_function = nodejs.NodejsFunction(
            self,
            "ContentSummaryGenerator",
            entry=path.join(function_code_dir, "content-summary-generator/index.ts"),
            deps_lock_file_path=path.join(
                function_code_dir, "content-summary-generator/package-lock.json"
            ),
            handler="handler",
            timeout=Duration.seconds(30),
            log_retention=logs.RetentionDays.ONE_WEEK,
        )
        summary_lambda_function.add_to_role_policy(
            iam.PolicyStatement(
                effect=iam.Effect.ALLOW,
                actions=[
                    "bedrock:InvokeModel",
                ],
                resources=[
                    "arn:aws:bedrock:*::foundation-model/anthropic.claude-instant-v1",
                ],
            )
        )

        # Word cloud generator Lambda function, written in Java
        java_packaging_instructions = [
            "/bin/sh",
            "-c",
            "mvn clean install "
            + "&& cp /asset-input/target/word-cloud-generator-1.0.jar /asset-output/",
        ]

        java_bundling_options = BundlingOptions(
            command=java_packaging_instructions,
            image=_lambda.Runtime.JAVA_8_CORRETTO.bundling_image,
            user="root",
            output_type=BundlingOutput.ARCHIVED,
        )

        word_cloud_lambda_function = _lambda.Function(
            self,
            "WordCloudGenerator",
            runtime=_lambda.Runtime.JAVA_8_CORRETTO,
            code=_lambda.Code.from_asset(
                path.join(function_code_dir, "word-cloud-generator"),
                bundling=java_bundling_options,
            ),
            handler="wordcloud.WordCloudGenerator",
            memory_size=1024,
            timeout=Duration.seconds(30),
            log_retention=logs.RetentionDays.ONE_WEEK,
        )

        # API Gateway serving the two Lambda functions as APIs
        domain_name = f"summarize-my-document-backend.{parent_domain}"
        hosted_zone = route53.HostedZone.from_lookup(
            self, "Zone", domain_name=parent_domain
        )
        certificate = acm.Certificate(
            self,
            "Cert",
            domain_name=domain_name,
            validation=acm.CertificateValidation.from_dns(hosted_zone=hosted_zone),
        )

        backend_apis = apigw.RestApi(
            self,
            "SummarizeMyDocumentBackendAPIs",
            description="Backend APIs for Summarize My Document demo",
            domain_name=apigw.DomainNameOptions(
                domain_name=domain_name, certificate=certificate
            ),
            # disable_execute_api_endpoint=True,
        )

        summary_api = backend_apis.root.add_resource("content-summary-generator")
        post_to_summary_api = summary_api.add_method(
            "POST",
            apigw.LambdaIntegration(summary_lambda_function),
            authorization_type=apigw.AuthorizationType.IAM,
        )
        CfnOutput(
            self,
            "SummaryApiMethodArn",
            value=post_to_summary_api.method_arn,
            export_name="SummarizeMyDoc-ContentSummaryMethod",
        )

        word_cloud_api = backend_apis.root.add_resource("word-cloud-generator")
        post_to_word_cloud_api = word_cloud_api.add_method(
            "POST",
            apigw.LambdaIntegration(word_cloud_lambda_function),
            authorization_type=apigw.AuthorizationType.IAM,
        )
        CfnOutput(
            self,
            "WordCloudApiMethodArn",
            value=post_to_word_cloud_api.method_arn,
            export_name="SummarizeMyDoc-WordCloudMethod",
        )

        route53.ARecord(
            self,
            "BackendApiRecord",
            record_name="summarize-my-document-backend",
            zone=hosted_zone,
            target=route53.RecordTarget.from_alias(
                route53_targets.ApiGateway(backend_apis)
            ),
        )
