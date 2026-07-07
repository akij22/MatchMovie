package com.example.matchmovie.repository

import com.example.matchmovie.database.ApiCacheEntry
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.network.dto.GenreResponseDto
import com.example.matchmovie.network.dto.MovieResponseDto
import com.example.matchmovie.network.dto.TvSeriesResponseDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeCacheRepository(
    private val dao: FilmDAO
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun getMovieGenres(fetchFromNetwork: suspend () -> GenreResponseDto): GenreResponseDto {
        return getOrFetch(
            cacheKey = MOVIE_GENRES_KEY,
            ttlMillis = GENRE_TTL_MILLIS,
            type = GenreResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    suspend fun getTvSeriesGenres(fetchFromNetwork: suspend () -> GenreResponseDto): GenreResponseDto {
        return getOrFetch(
            cacheKey = TV_SERIES_GENRES_KEY,
            ttlMillis = GENRE_TTL_MILLIS,
            type = GenreResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    suspend fun getPopularMovies(fetchFromNetwork: suspend () -> MovieResponseDto): MovieResponseDto {
        return getOrFetch(
            cacheKey = POPULAR_MOVIES_KEY,
            ttlMillis = HOME_TTL_MILLIS,
            type = MovieResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    suspend fun getUpcomingMovies(fetchFromNetwork: suspend () -> MovieResponseDto): MovieResponseDto {
        return getOrFetch(
            cacheKey = UPCOMING_MOVIES_KEY,
            ttlMillis = HOME_TTL_MILLIS,
            type = MovieResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    suspend fun getPopularTvSeries(fetchFromNetwork: suspend () -> TvSeriesResponseDto): TvSeriesResponseDto {
        return getOrFetch(
            cacheKey = POPULAR_TV_SERIES_KEY,
            ttlMillis = HOME_TTL_MILLIS,
            type = TvSeriesResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    suspend fun getTopRatedTvSeries(fetchFromNetwork: suspend () -> TvSeriesResponseDto): TvSeriesResponseDto {
        return getOrFetch(
            cacheKey = TOP_RATED_TV_SERIES_KEY,
            ttlMillis = HOME_TTL_MILLIS,
            type = TvSeriesResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    suspend fun searchMovies(
        query: String,
        fetchFromNetwork: suspend () -> MovieResponseDto
    ): MovieResponseDto {
        return getOrFetch(
            cacheKey = "$SEARCH_MOVIES_PREFIX${query.normalizedCacheQuery()}",
            ttlMillis = SEARCH_TTL_MILLIS,
            type = MovieResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    suspend fun searchTvSeries(
        query: String,
        fetchFromNetwork: suspend () -> TvSeriesResponseDto
    ): TvSeriesResponseDto {
        return getOrFetch(
            cacheKey = "$SEARCH_TV_SERIES_PREFIX${query.normalizedCacheQuery()}",
            ttlMillis = SEARCH_TTL_MILLIS,
            type = TvSeriesResponseDto::class.java,
            fetchFromNetwork = fetchFromNetwork
        )
    }

    private suspend fun <T> getOrFetch(
        cacheKey: String,
        ttlMillis: Long,
        type: Type,
        fetchFromNetwork: suspend () -> T
    ): T {
        val now = System.currentTimeMillis()
        val cachedEntry = dao.getCacheEntry(cacheKey)
        val cachedValue = cachedEntry?.decodePayload<T>(type)

        if (cachedEntry != null && cachedValue != null && now - cachedEntry.fetchedAtMillis <= ttlMillis) {
            return cachedValue
        }

        return try {
            val freshValue = fetchFromNetwork()
            dao.upsertCacheEntry(
                ApiCacheEntry(
                    cacheKey = cacheKey,
                    payloadJson = moshi.adapter<T>(type).toJson(freshValue),
                    fetchedAtMillis = now
                )
            )
            freshValue
        } catch (e: Exception) {
            cachedValue ?: throw e
        }
    }

    private fun <T> ApiCacheEntry.decodePayload(type: Type): T? {
        return runCatching {
            moshi.adapter<T>(type).fromJson(payloadJson)
        }.getOrNull()
    }

    private fun String.normalizedCacheQuery(): String {
        return trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
    }

    companion object {
        private val HOME_TTL_MILLIS = TimeUnit.HOURS.toMillis(12)
        private val SEARCH_TTL_MILLIS = TimeUnit.HOURS.toMillis(1)
        private val GENRE_TTL_MILLIS = TimeUnit.DAYS.toMillis(30)

        private const val MOVIE_GENRES_KEY = "genres_movies"
        private const val TV_SERIES_GENRES_KEY = "genres_tv"
        private const val POPULAR_MOVIES_KEY = "home_popular_movies"
        private const val UPCOMING_MOVIES_KEY = "home_upcoming_movies"
        private const val POPULAR_TV_SERIES_KEY = "home_popular_tv"
        private const val TOP_RATED_TV_SERIES_KEY = "home_top_rated_tv"
        private const val SEARCH_MOVIES_PREFIX = "search_movie:"
        private const val SEARCH_TV_SERIES_PREFIX = "search_tv:"
    }
}
