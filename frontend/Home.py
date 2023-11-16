import streamlit as st
import content_summary_generator_client as summary_client
import word_cloud_generator_client as word_cloud_client

st.set_page_config(initial_sidebar_state="collapsed")

st.title("📝 Summarize my document")

# Get a file from the user
uploaded_file = st.file_uploader(
    "Upload a document to summarize", type=("txt", "md"), key="document_upload"
)

if uploaded_file:
    document = uploaded_file.read().decode()

    # Call the content summary generator API and word cloud generator API with file contents
    with st.spinner("Generating..."):
        summary_api = summary_client.ContentSummaryGeneratorAPI()
        st.session_state.content_summary = summary_api.get_content_summary(document)

        word_cloud_api = word_cloud_client.WordCloudGeneratorAPI()
        st.session_state.word_cloud = word_cloud_api.get_word_cloud_image(document)

    # Display the summarization
    st.subheader("Summary")
    st.write(st.session_state.content_summary)

    st.subheader("Word cloud")
    st.image(st.session_state.word_cloud)
