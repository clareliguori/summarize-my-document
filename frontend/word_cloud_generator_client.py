from aws_requests_auth.aws_auth import AWSRequestsAuth
import boto3
import os
import requests
import urllib.parse


# Client for the word cloud generator backend API
class WordCloudGeneratorAPI:
    # Set up request signing for calls to the word cloud generator API
    _aws_credentials = boto3.Session().get_credentials()
    _aws_region = os.getenv("AWS_REGION")

    _api_endpoint = f"{os.getenv('BACKEND_URL')}/word-cloud-generator"

    _api_auth = AWSRequestsAuth(
        aws_access_key=_aws_credentials.access_key,
        aws_secret_access_key=_aws_credentials.secret_key,
        aws_token=_aws_credentials.token,
        aws_host=urllib.parse.urlparse(_api_endpoint).netloc,
        aws_region=_aws_region,
        aws_service="execute-api",
    )

    # Post a document's contents to the word cloud generator backend API
    # and return the image from the API response
    def get_word_cloud_image(self, document: str) -> str:
        r = requests.post(
            url=self._api_endpoint,
            auth=self._api_auth,
            json={"doc_content": document},
        )

        if r.status_code == 200:
            # Example response:
            # { "word_cloud_url": "<S3 pre-signed link>" }
            return r.json()["word_cloud_url"]
        else:
            raise Exception(
                f"Word cloud generator API returned failure status code: {r.status_code}"
            )
