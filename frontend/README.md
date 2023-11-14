# Frontend App

## Run locally

```sh
docker compose up --build
```

## Test
```sh
python3 -m venv .venv
source .venv/bin/activate

pip install -r requirements.txt
pip install -r requirements-dev.txt

pytest
```
