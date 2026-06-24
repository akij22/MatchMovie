import os
import re

import requests
from dotenv import load_dotenv
from flask import Flask, jsonify, request
from werkzeug.security import check_password_hash, generate_password_hash

load_dotenv()

app = Flask(__name__)

TMDB_BASE_URL = "https://api.themoviedb.org/3"
OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1"
TMDB_TOKEN = os.environ.get("TMDB_TOKEN")
OPENROUTER_API_KEY = os.environ.get("OPENROUTER_API_KEY")

# Definizione del modello da utilizzare per rispondere alle richieste
OPENROUTER_MODEL = os.environ.get("OPENROUTER_MODEL", "openrouter/owl-alpha")
EMAIL_PATTERN = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


def validate_auth_payload(data):
    name = (data.get("name") or "").strip()
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""
    confirm_password = data.get("confirmPassword") or ""

    if not email or not EMAIL_PATTERN.match(email):
        return None, jsonify({"error": "A valid email is required"}), 400

    # Se la password non rispetta i criteri minimi (lunghezza almeno 6 caratteri), restituisco errore
    if len(password) < 6:
        return None, jsonify({"error": "Password must be at least 6 characters"}), 400

    # Controllo che la password e la conferma di essa corrispondano (anche lato backend, per evitare errori se chiamata al server è eseguita lato Postman / curl / ...)
    if password != confirm_password:
        return None, jsonify({"error": "Passwords do not match"}), 400

    return {"name": name, "email": email, "password": password}, None, None


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
                            "Your name is MatchMovie's assistant. Reply in the same language in which the question was asked."
                            "Help the user find movies, explain recommendations clearly, and keep replies concise. "
                            "The description of each movie cannot exceed 100 characters. "
                            "IMPORTANT: do not format your response in markdown style (so don't use '**<text>**' for bold, use plain text instead)."
                            "IMPORTANT: if user asks about TV series, reply that they are not currently supported and suggest movies instead."
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


@app.get("/movies/upcoming")
def upcoming_movies():
    return tmdb_get("/movie/upcoming")


@app.get("/movies/<int:movie_id>/credits")
def movie_credits(movie_id):
    return tmdb_get(f"/movie/{movie_id}/credits")


@app.get("/movies/<int:movie_id>/recommendations")
def movie_recommendations(movie_id):
    return tmdb_get(f"/movie/{movie_id}/recommendations")


@app.post("/chat")
def chat():
    data = request.get_json(silent=True) or {}
    prompt = data.get("messagePrompt", "").strip()

    if not prompt:
        return jsonify({"error": "messagePrompt is required"}), 400

    return openrouter_chat(prompt)


@app.post("/auth/register")
def register():
    data = request.get_json(silent=True) or {}
    payload, error_response, status_code = validate_auth_payload(data)

    if error_response:
        return error_response, status_code

    if payload is None:
        return jsonify({"error": "Invalid payload; payload is required"}), 400

    return jsonify(
        {
            "name": payload["name"],
            "email": payload["email"],
            # Genero l'hash della password, che verrà successivamente salvato nel campo 'password' di User entity
            "passwordHash": generate_password_hash(payload["password"]),
        }
    )


@app.post("/auth/login")
def login():
    data = request.get_json(silent=True) or {}
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""
    password_hash = data.get("passwordHash") or ""

    # Controllo la presenza dei campi necessari
    if not email or not password or not password_hash:
        return jsonify({"error": "email, password and passwordHash are required"}), 400

    # Verifico la corrispondenza tra il hash e la password (con funzione apposita)
    if not check_password_hash(password_hash, password):
        return jsonify({"authenticated": False, "error": "Invalid credentials"}), 401

    # Se corrispondono, restituisco l'autenticazione riuscita
    return jsonify({"authenticated": True, "email": email})


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


# Endpoint per il recupero di tutti i generi possibili per un film
@app.get("/genres")
def genres():
    return tmdb_get("/genre/movie/list")


@app.get("/movies/<int:movie_id>/videos")
def movie_videos(movie_id):
    response, status_code = tmdb_get(f"/movie/{movie_id}/videos")

    if status_code != 200:
        return response, status_code

    data = response.get_json()
    videos = data.get("results", [])
    trailer = next(
        (
            video
            for video in videos
            if video.get("site") == "YouTube"
            and video.get("type") == "Trailer"
            and video.get("official")
        ),
        None,
    ) or next(
        (
            video
            for video in videos
            if video.get("site") == "YouTube" and video.get("type") == "Trailer"
        ),
        None,
    )

    if not trailer:
        return jsonify({"key": None})

    return jsonify({"key": trailer.get("key")})
