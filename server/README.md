# MatchMovie Flask Server

Backend minimale per spostare le chiamate a The Movie Database fuori dall'app Android.

## Setup

```bash
cd server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
export TMDB_TOKEN="your_tmdb_bearer_token"
flask --app app run --host 0.0.0.0 --port 5000
```

L'emulatore Android raggiunge il server locale con `http://10.0.2.2:5000/`.

## Endpoint

- `GET /movies/search?query=<title>`
- `GET /movies/popular`
- `GET /movies/<movie_id>/credits`
- `GET /health`
