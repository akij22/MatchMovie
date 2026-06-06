import os

import requests
from flask import Flask, jsonify, request


app = Flask(__name__)

TMDB_BASE_URL = "https://api.themoviedb.org/3"
TMDB_TOKEN = os.environ.get("TMDB_TOKEN")


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


@app.get("/movies/search")
def search_movies():
    query = request.args.get("query", "").strip()
    if not query:
        return jsonify({
            "page": 1,
            "results": [],
            "total_pages": 0,
            "total_results": 0,
        })

    return tmdb_get("/search/movie", {"query": query})


@app.get("/movies/popular")
def popular_movies():
    return tmdb_get("/movie/popular")


@app.get("/movies/<int:movie_id>/credits")
def movie_credits(movie_id):
    return tmdb_get(f"/movie/{movie_id}/credits")


@app.get("/health")
def health():
    return jsonify({"status": "ok"})
