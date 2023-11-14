from aws_cdk import (
    App,
    Environment,
)
from stacks.frontend_stack import FrontendStack
import os


app = App()
env = Environment(account=os.environ["CDK_DEFAULT_ACCOUNT"], region="us-west-2")
FrontendStack(
    app,
    "SummarizeMyDoc-Frontend",
    env=env,
    parent_domain="liguori.people.aws.dev",
)
app.synth()
