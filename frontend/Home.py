import streamlit as st

st.title("📝 Summarize my document")
uploaded_file = st.file_uploader("Upload a document", type=("txt", "md"))

if uploaded_file:
    document = uploaded_file.read().decode()

    st.write("### Summary")
    st.write("Hello world")
