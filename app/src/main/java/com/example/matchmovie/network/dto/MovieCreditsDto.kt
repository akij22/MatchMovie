package com.example.matchmovie.network.dto

data class MovieCreditsDto(
    val id: Int,
    val cast: List<MovieCastMemberDto>,
    val crew: List<MovieCrewMemberDto>
)

data class MovieCastMemberDto(
    val id: Int,
    val name: String,
    val character: String?,
    val profile_path: String?,
    val order: Int?
)

data class MovieCrewMemberDto(
    val id: Int,
    val name: String,
    val job: String?,
    val department: String?,
    val profile_path: String?
)
