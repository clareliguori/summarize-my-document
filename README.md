# Summarize my document demo app

https://github.com/clareliguori/summarize-my-document/assets/484245/03b8ef1a-4d03-4c21-b7ab-f307246a7a1b

## Deploy with AWS CDK

Fork this repo to your own GitHub account.
Edit the file `cdk_stacks.py`. Search for `parent_domain` and fill in your own DNS domain, such as `my-domain.com`.
The demo application will be hosted at `https://summarize-my-document-demo.my-domain.com`.
Also edit the file `frontend/docker-compose.yml` and fill in your own DNS domain for the `BACKEND_URL` value.
Push these changes to your fork repository.

Install both nodejs and python on your computer.

Install CDK:
```sh
npm install -g aws-cdk
```

Set up a virtual env:
```sh
python3 -m venv .venv

source .venv/bin/activate

pip install -r requirements.txt
```
After this initial setup, you only need to run `source .venv/bin/activate` to use the virtual env for further development.

Synthesize the demo stacks:
```sh
cdk synth --app 'python3 cdk_stacks.py'
```

Deploy all the demo stacks:
```sh
cdk deploy --app 'python3 cdk_stacks.py' --all
```

The demo application will now be hosted at `https://summarize-my-document-demo.my-domain.com`,
behind Cognito-based user authentication.
To add users that can log into the demo application, select the `summarize-my-document-demo` user pool on the
[Cognito console](https://us-west-2.console.aws.amazon.com/cognito/v2/idp/user-pools?region=us-west-2)
and click "Create user".
