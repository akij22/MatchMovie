import os

import requests
from flask import Flask, jsonify, request

app = Flask(__name__)

TMDB_BASE_URL = "https://api.themoviedb.org/3"
OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1"
TMDB_TOKEN = os.environ.get("TMDB_TOKEN")
OPENROUTER_API_KEY = os.environ.get("OPENROUTER_API_KEY")

# Definizione del modello da utilizzare per rispondere alle richieste
OPENROUTER_MODEL = os.environ.get("OPENROUTER_MODEL", "openrouter/owl-alpha")


def tmdb_get(path, params=None):
    if not TMDB_TOKEN:
        return jsonify({"error": "TMDB_TOKEN environment variable is not set"}), 500

    response = requests.get(
        f"{TMDB_BASE_URL}{path}",
        headers={
            "Authorization": f"Bearer {TMDB_TOKEN}",
            "accept": "application/json",
        },
        params=params,
        timeout=10,
    )

    return jsonify(response.json()), response.status_code


# Funzione per inviare una richiesta di chat a OpenRouter (ad un modello specifico)
def openrouter_chat(prompt):
    if not OPENROUTER_API_KEY:
        return jsonify(
            {"error": "OPENROUTER_API_KEY environment variable is not set"}
        ), 500

    try:
        response = requests.post(
            f"{OPENROUTER_BASE_URL}/chat/completions",
            headers={
                "Authorization": f"Bearer {OPENROUTER_API_KEY}",
                "Content-Type": "application/json",
            },
            json={
                "model": OPENROUTER_MODEL,
                "messages": [
                    {
                        "role": "system",
                        # Mantengo la descrizione di ogni film corta, per non andare oltre il contenuto del messaggio consentito lato UI
                        "content": (
                            "You are MatchMovie's assistant. Answer in Italian. "
                            "Help the user find movies, explain recommendations clearly, "
                            "and keep replies concise. The description of each movie cannot exceed 100 characters."
                        ),
                    },
                    {
                        "role": "user",
                        "content": prompt,
                    },
                ],
                "max_tokens": 500,
            },
            # Timeout personalizzato di 60 secondi per aspettare la risposta da Openrouter
            timeout=60,
        )
    except requests.Timeout:
        return jsonify({"error": "OpenRouter request timed out"}), 504

    response_body = response.json()
    if not response.ok:
        return jsonify(
            {
                "error": "OpenRouter request failed",
                "details": response_body,
            }
        ), response.status_code

    message_reply = (
        response_body.get("choices", [{}])[0].get("message", {}).get("content", "")
    )

    return jsonify({"messageReply": message_reply})


@app.get("/movies/search")
def search_movies():
    query = request.args.get("query", "").strip()
    if not query:
        return jsonify(
            {
                "page": 1,
                "results": [],
                "total_pages": 0,
                "total_results": 0,
            }
        )

    return tmdb_get("/search/movie", {"query": query})


@app.get("/movies/popular")
def popular_movies():
    return tmdb_get("/movie/popular")


@app.get("/movies/<int:movie_id>/credits")
def movie_credits(movie_id):
    return tmdb_get(f"/movie/{movie_id}/credits")


@app.post("/chat")
def chat():
    data = request.get_json(silent=True) or {}
    prompt = data.get("messagePrompt", "").strip()

    if not prompt:
        return jsonify({"error": "messagePrompt is required"}), 400

    return openrouter_chat(prompt)


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


# Endpoint per il recupero di tutti i generi possibili per un film
@app.get("/genres")
def genres():
    return tmdb_get("/genre/movie/list")
