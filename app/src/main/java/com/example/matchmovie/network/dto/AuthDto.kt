package com.example.matchmovie.network.dto

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
)

data class RegisterResponseDto(
    val name: String,
    val email: String,
    val passwordHash: String,
)

data class LoginRequestDto(
    val email: String,
    val password: String,
    val passwordHash: String,
)

data class LoginResponseDto(
    val authenticated: Boolean,
    val email: String?,
)
