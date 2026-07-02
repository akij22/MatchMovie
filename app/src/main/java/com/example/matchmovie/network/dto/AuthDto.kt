package com.example.matchmovie.network.dto

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)

data class RegisterResponseDto(
    val token: String,
    val user: AuthUserDto,
)

data class LoginRequestDto(
    val email: String,
    val password: String,
)

data class LoginResponseDto(
    val authenticated: Boolean,
    val token: String,
    val user: AuthUserDto,
)

data class UpdateProfileRequestDto(
    val profileImage: String?,
    val bio: String?,
)

data class AuthUserDto(
    val id: Int,
    val name: String,
    val email: String,
    val profileImage: String?,
    val bio: String?,
)
