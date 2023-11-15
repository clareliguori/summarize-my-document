import streamlit as st

from aws_requests_auth.aws_auth import AWSRequestsAuth
import boto3
import json
import os
import requests
import urllib.parse

st.set_page_config(initial_sidebar_state="collapsed")

st.title("Test the Content Summary Generator API")

aws_credentials = boto3.Session().get_credentials()
aws_region = os.getenv("AWS_REGION")

api_endpoint = f"{os.getenv('BACKEND_URL')}/content-summary-generator"

api_auth = AWSRequestsAuth(
    aws_access_key=aws_credentials.access_key,
    aws_secret_access_key=aws_credentials.secret_key,
    aws_token=aws_credentials.token,
    aws_host=urllib.parse.urlparse(api_endpoint).netloc,
    aws_region=aws_region,
    aws_service="execute-api",
)

with st.form("test_content_summary_generator_api"):
    content_input = st.text_area(
        "Text to analyze",
        "It was the best of times, it was the worst of times, it was the age of "
        "wisdom, it was the age of foolishness, it was the epoch of belief, it "
        "was the epoch of incredulity, it was the season of Light, it was the "
        "season of Darkness, it was the spring of hope, it was the winter of "
        "despair, (...)",
    )
    submitted = st.form_submit_button("Test")
    if submitted:
        r = requests.post(
            url=api_endpoint,
            auth=api_auth,
            json={"doc_content": content_input},
        )
        api_response_json = r.json()

        # Example response:
        # { "content_summary": "Here is a short summary of the document..." }
        st.subheader("API Response")
        st.write(f"Status Code: {r.status_code}")
        st.code(json.dumps(api_response_json, indent=2), language="json")
