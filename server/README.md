# MatchMovie Flask Server

Backend minimale per spostare le chiamate a The Movie Database fuori dall'app Android.

## Setup

```bash
cd server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
export TMDB_TOKEN="your_tmdb_bearer_token"
export OPENROUTER_API_KEY="your_openrouter_api_key"
export OPENROUTER_MODEL="openrouter/auto"
flask --app app run --host 0.0.0.0 --port 5000
```

L'emulatore Android raggiunge il server locale con `http://10.0.2.2:5000/`.

## Endpoint

- `GET /movies/search?query=<title>`
- `GET /movies/popular`
- `GET /movies/upcoming`
- `GET /movies/<movie_id>/credits`
- `POST /chat`
- `POST /auth/register`
- `POST /auth/login`
- `GET /health`
- `GET /genres`

`POST /chat` expects the same JSON shape used by the Android `ChatRequestDto`:

```json
{
  "messagePrompt": "Consigliami un film rilassante"
}
```

It returns the same JSON shape used by `ChatResponseDto`:

```json
{
  "messageReply": "..."
}
```

`POST /auth/register` validates the credentials and returns a password hash to store in the
Android local Room database:

```json
{
  "name": "Mario Rossi",
  "email": "mario@example.com",
  "password": "secret1"
}
```

`POST /auth/login` verifies a password against the hash already saved locally by the app:

```json
{
  "email": "mario@example.com",
  "password": "secret1",
  "passwordHash": "..."
}
```
