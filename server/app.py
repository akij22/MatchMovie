import functools
import os
import re
import uuid
from datetime import datetime, timedelta, timezone

import jwt
import requests
from dotenv import load_dotenv

load_dotenv()

from flask import Flask, g, jsonify, request
from flask_cors import CORS
from werkzeug.security import check_password_hash, generate_password_hash

from db import supabase

app = Flask(__name__)
CORS(app)

TMDB_BASE_URL = "https://api.themoviedb.org/3"
OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1"
TMDB_TOKEN = os.environ.get("TMDB_TOKEN")
OPENROUTER_API_KEY = os.environ.get("OPENROUTER_API_KEY")

OPENROUTER_MODEL = os.environ.get("OPENROUTER_MODEL", "openrouter/owl-alpha")
EMAIL_PATTERN = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")

JWT_SECRET = os.environ.get("SUPABASE_SECRET_KEY", "dev-secret")
JWT_ALGORITHM = "HS256"
JWT_EXPIRATION_HOURS = 720


def validate_auth_payload(data):
    name = (data.get("name") or "").strip()
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""
    confirm_password = data.get("confirmPassword") or ""

    if not email or not EMAIL_PATTERN.match(email):
        return None, jsonify({"error": "A valid email is required"}), 400

    if len(password) < 6:
        return None, jsonify({"error": "Password must be at least 6 characters"}), 400

    if password != confirm_password:
        return None, jsonify({"error": "Passwords do not match"}), 400

    return {"name": name, "email": email, "password": password}, None, None


def create_jwt(user_id, email):
    payload = {
        "user_id": user_id,
        "email": email,
        "jti": uuid.uuid4().hex,
        "exp": datetime.now(timezone.utc) + timedelta(hours=JWT_EXPIRATION_HOURS),
        "iat": datetime.now(timezone.utc),
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


def require_auth(f):
    @functools.wraps(f)
    def decorated(*args, **kwargs):
        auth_header = request.headers.get("Authorization", "")
        token = auth_header.removeprefix("Bearer ").strip()

        if not token:
            return jsonify({"error": "Missing authorization token"}), 401

        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
            g.current_user_id = payload["user_id"]
            g.current_user_email = payload["email"]
        except jwt.ExpiredSignatureError:
            return jsonify({"error": "Token has expired"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"error": "Invalid token"}), 401

        return f(*args, **kwargs)

    return decorated


def tmdb_get_method(path, params=None):
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


# ---------------------------------------------------------------------------
# MOVIE ENDPOINTS (protetti)
# ---------------------------------------------------------------------------


@app.get("/movies/search")
@require_auth
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

    return tmdb_get_method("/search/movie", {"query": query})


@app.get("/movies/popular")
@require_auth
def popular_movies():
    return tmdb_get_method("/movie/popular")


@app.get("/movies/upcoming")
@require_auth
def upcoming_movies():
    return tmdb_get_method("/movie/upcoming")


@app.get("/movies/<int:movie_id>/credits")
@require_auth
def movie_credits(movie_id):
    return tmdb_get_method(f"/movie/{movie_id}/credits")


@app.get("/movies/<int:movie_id>/recommendations")
@require_auth
def movie_recommendations(movie_id):
    return tmdb_get_method(f"/movie/{movie_id}/recommendations")


@app.get("/movies/<int:movie_id>/videos")
@require_auth
def movie_videos(movie_id):
    response, status_code = tmdb_get_method(f"/movie/{movie_id}/videos")

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


@app.post("/chat")
@require_auth
def chat():
    data = request.get_json(silent=True) or {}
    prompt = data.get("messagePrompt", "").strip()

    if not prompt:
        return jsonify({"error": "messagePrompt is required"}), 400

    return openrouter_chat(prompt)


@app.get("/genres")
@require_auth
def genres():
    return tmdb_get_method("/genre/movie/list")


# ---------------------------------------------------------------------------
# AUTH ENDPOINTS
# ---------------------------------------------------------------------------


@app.post("/auth/register")
def register():
    data = request.get_json(silent=True) or {}
    payload, error_response, status_code = validate_auth_payload(data)

    if error_response:
        return error_response, status_code

    if payload is None:
        return jsonify({"error": "Invalid payload"}), 400

    email = payload["email"]

    existing = supabase.table("users").select("id").eq("email", email).execute()
    if existing.data:
        return jsonify({"error": "Email already registered"}), 409

    password_hash = generate_password_hash(payload["password"])

    result = (
        supabase.table("users")
        .insert(
            {
                "name": payload["name"],
                "email": email,
                "password_hash": password_hash,
            }
        )
        .execute()
    )

    if not result.data:
        return jsonify({"error": "Registration failed"}), 500

    user = result.data[0]
    token = create_jwt(user["id"], email)

    return jsonify(
        {
            "token": token,
            "user": {
                "id": user["id"],
                "name": user["name"],
                "email": user["email"],
                "profileImage": user.get("profile_image"),
                "bio": user.get("bio"),
            },
        }
    )


@app.post("/auth/login")
def login():
    data = request.get_json(silent=True) or {}
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""

    if not email or not password:
        return jsonify({"error": "Email and password are required"}), 400

    result = supabase.table("users").select("*").eq("email", email).execute()

    if not result.data:
        return jsonify({"authenticated": False, "error": "Invalid credentials"}), 401

    user = result.data[0]

    if not check_password_hash(user["password_hash"], password):
        return jsonify({"authenticated": False, "error": "Invalid credentials"}), 401

    token = create_jwt(user["id"], email)

    return jsonify(
        {
            "authenticated": True,
            "token": token,
            "user": {
                "id": user["id"],
                "name": user["name"],
                "email": user["email"],
                "profileImage": user.get("profile_image"),
                "bio": user.get("bio"),
            },
        }
    )


@app.get("/auth/me")
@require_auth
def get_me():
    result = (
        supabase.table("users")
        .select("id, name, email, profile_image, bio, created_at")
        .eq("id", g.current_user_id)
        .single()
        .execute()
    )

    if not result.data:
        return jsonify({"error": "User not found"}), 404

    user = result.data
    return jsonify(
        {
            "id": user["id"],
            "name": user["name"],
            "email": user["email"],
            "profileImage": user.get("profile_image"),
            "bio": user.get("bio"),
        }
    )


@app.put("/auth/me")
@require_auth
def update_me():
    data = request.get_json(silent=True) or {}
    update_fields = {}

    if "name" in data:
        update_fields["name"] = data["name"].strip()
    if "bio" in data:
        update_fields["bio"] = data["bio"].strip()
    if "profileImage" in data:
        update_fields["profile_image"] = data["profileImage"]

    if not update_fields:
        return jsonify({"error": "No fields to update"}), 400

    result = (
        supabase.table("users")
        .update(update_fields)
        .eq("id", g.current_user_id)
        .execute()
    )

    if not result.data:
        return jsonify({"error": "Update failed"}), 500

    user = result.data[0]
    return jsonify(
        {
            "id": user["id"],
            "name": user["name"],
            "email": user["email"],
            "profileImage": user.get("profile_image"),
            "bio": user.get("bio"),
        }
    )


# ---------------------------------------------------------------------------
# HEALTH
# ---------------------------------------------------------------------------


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=True)
