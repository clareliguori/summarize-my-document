from unittest.mock import patch
from streamlit.testing.v1 import AppTest


# @patch("openai.ChatCompletion.create")
# def test_Chatbot(openai_create):
#     at = AppTest.from_file("Chatbot.py").run()
#     assert not at.exception
#     at.chat_input[0].set_value("Do you know any jokes?").run()
#     assert at.info[0].value == "Please add your OpenAI API key to continue."

#     JOKE = "Why did the chicken cross the road? To get to the other side."
#     openai_create.return_value = create_openai_object_sync(JOKE)
#     at.text_input(key="chatbot_api_key").set_value("sk-...")
#     at.chat_input[0].set_value("Do you know any jokes?").run()
#     print(at)
#     assert at.chat_message[1].markdown[0].value == "Do you know any jokes?"
#     assert at.chat_message[2].markdown[0].value == JOKE
#     assert at.chat_message[2].avatar == "assistant"
#     assert not at.exception
