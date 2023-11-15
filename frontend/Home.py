import streamlit as st

from aws_requests_auth.aws_auth import AWSRequestsAuth
import boto3
import json
import os
import requests
import urllib.parse

st.set_page_config(initial_sidebar_state="collapsed")

st.title("📝 Summarize my document")

aws_credentials = boto3.Session().get_credentials()
aws_region = os.getenv("AWS_REGION")

# Set up request signing for calls to the content summary generator API
content_summary_generator_api_endpoint = (
    f"{os.getenv('BACKEND_URL')}/content-summary-generator"
)

content_summary_generator_auth = AWSRequestsAuth(
    aws_access_key=aws_credentials.access_key,
    aws_secret_access_key=aws_credentials.secret_key,
    aws_token=aws_credentials.token,
    aws_host=urllib.parse.urlparse(content_summary_generator_api_endpoint).netloc,
    aws_region=aws_region,
    aws_service="execute-api",
)

# Get a file from the user
uploaded_file = st.file_uploader("Upload a document to summarize", type=("txt", "md"))

if uploaded_file:
    document = uploaded_file.read().decode()

    # Call the content summary generator API with file contents, then display the returned summarization
    r = requests.post(
        url=content_summary_generator_api_endpoint,
        auth=content_summary_generator_auth,
        json={"doc_content": document},
    )

    if r.status_code == 200:
        # Example response:
        # { "content_summary": "Here is a short summary of the document..." }
        st.subheader("Summary")
        st.write(r.json()["content_summary"])
    else:
        st.error("Failed to call the backend API")
