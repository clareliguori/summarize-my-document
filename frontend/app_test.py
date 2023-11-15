from streamlit.testing.v1 import AppTest


# This is currently just a sanity test - the AppTesting framework does not yet support testing file_uploader elements
def test_Home():
    at = AppTest.from_file("Home.py").run()

    # Page loads successfully
    assert not at.exception
