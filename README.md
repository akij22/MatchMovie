# MatchMovie

MatchMovie is an Android application that helps users discover, save, rate, and revisit movies. The app combines movie data from The Movie Database (TMDB), a local Room database for each user's saved list, Supabase-backed authentication, and an AI chat assistant powered through a Flask backend.

## Demo

Below is a short demo of the MatchMovie app in action.

<video controls width="360">
  <source src="https://raw.githubusercontent.com/akij22/MatchMovie/main/MatchMovie_demo.mp4" type="video/mp4">
</video>

## Features

- User registration and login through the Flask backend and Supabase.
- Home screen with popular movies, upcoming movies, search, and trailer links.
- Movie detail pages with overview, rating, cast/crew information, recommendations, and save actions.
- Swipe-style explore flow for discovering recommended movies.
- Personal movie list stored locally with Room.
- User ratings and mood classification for saved movies.
- AI chat assistant that recommends movies using the user's saved movie context.
- Profile and "Wrapped" style statistics based on saved movies, ratings, and moods.

## Tech Stack

- Android, Kotlin, Jetpack Compose, Material 3
- Room for local persistence
- Retrofit and Moshi for HTTP and JSON handling
- Coil for image loading
- Flask backend
- Supabase for remote user storage
- TMDB API for movie data
- OpenRouter for AI chat completions

## Project Structure

```text
.
├── app/                  # Android application
├── server/               # Flask backend
├── gradle/               # Gradle wrapper and version catalog
├── build.gradle.kts      # Root Gradle build file
└── settings.gradle.kts   # Gradle project settings
```

## Prerequisites

Install the following tools before running the project locally:

- Android Studio with an Android SDK installed
- JDK 17 or newer
- Python 3.10 or newer
- A TMDB API bearer token
- An OpenRouter API key
- A Supabase project with a `users` table

## Backend Setup

1. Create the Supabase table by running [server/migration.sql](server/migration.sql) in the Supabase SQL editor.

2. Create a Python virtual environment and install the backend dependencies:

```bash
cd server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

3. Create a `server/.env` file:

```env
TMDB_TOKEN=your_tmdb_bearer_token
OPENROUTER_API_KEY=your_openrouter_api_key
OPENROUTER_MODEL=openrouter/owl-alpha
SUPABASE_URL=your_supabase_project_url
SUPABASE_SECRET_KEY=your_supabase_secret_or_service_role_key
```

4. Start the Flask server:

```bash
flask --app app run --host 0.0.0.0 --port 5001
```

The Android emulator can reach the local backend at `http://10.0.2.2:5001/`, which is the base URL currently configured in [RetrofitInstance.kt](app/src/main/java/com/example/matchmovie/network/RetrofitInstance.kt).

## Android App Setup

1. From the repository root, make sure the Gradle wrapper is executable:

```bash
chmod +x ./gradlew
```

2. Build the debug APK:

```bash
./gradlew assembleDebug
```

3. Open the project in Android Studio.

4. Start the Flask backend before launching the app.

5. Run the `app` configuration on an Android emulator or a physical device.

If you run the app on a physical Android device, replace the Retrofit base URL with the local network address of the machine running Flask, for example `http://192.168.1.10:5001/`.

## Useful Commands

Run unit tests:

```bash
./gradlew test
```

Run Android instrumented tests:

```bash
./gradlew connectedAndroidTest
```

Check backend health:

```bash
curl http://localhost:5001/health
```

## Backend Endpoints

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me`
- `PUT /auth/me`
- `GET /movies/search?query=<title>`
- `GET /movies/popular`
- `GET /movies/upcoming`
- `GET /movies/<movie_id>/credits`
- `GET /movies/<movie_id>/recommendations`
- `GET /movies/<movie_id>/videos`
- `GET /genres`
- `POST /chat`
- `GET /health`

Most movie and chat endpoints require the JWT returned by the authentication endpoints.

## Notes

- Saved movies are stored locally in the Android Room database.
- Remote authentication data is stored in Supabase.
- TMDB and OpenRouter calls are handled by the Flask backend so API keys are not embedded in the Android app.
- The repository also contains a backend-specific README in [server/README.md](server/README.md).
