-- Esegui questo script nell'SQL Editor di Supabase per creare le tabelle remote.
-- I film salvati dall'utente restano nel database locale Room dell'app Android.

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    profile_image TEXT,
    bio TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
