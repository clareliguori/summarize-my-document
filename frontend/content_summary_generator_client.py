from aws_requests_auth.aws_auth import AWSRequestsAuth
import boto3
import os
import requests
import urllib.parse


# Client for the content summary generator backend API
class ContentSummaryGeneratorAPI:
    # Set up request signing for calls to the content summary generator API
    _aws_credentials = boto3.Session().get_credentials()
    _aws_region = os.getenv("AWS_REGION")

    _api_endpoint = f"{os.getenv('BACKEND_URL')}/content-summary-generator"

    _api_auth = AWSRequestsAuth(
        aws_access_key=_aws_credentials.access_key,
        aws_secret_access_key=_aws_credentials.secret_key,
        aws_token=_aws_credentials.token,
        aws_host=urllib.parse.urlparse(_api_endpoint).netloc,
        aws_region=_aws_region,
        aws_service="execute-api",
    )

    # Post a document's contents to the content summary generator backend API
    # and return the generated summary from the API response
    def get_content_summary(self, document: str) -> str:
        r = requests.post(
            url=self._api_endpoint,
            auth=self._api_auth,
            json={"doc_content": document},
        )

        if r.status_code == 200:
            # Example response:
            # { "content_summary": "Here is a short summary of the document..." }
            return r.json()["content_summary"]
        else:
            raise Exception(
                f"Content summary generator API returned failure status code: {r.status_code}"
            )
