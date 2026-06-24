package com.example.matchmovie.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.matchmovie.R
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.LoginRequestDto
import com.example.matchmovie.network.dto.RegisterRequestDto
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    dao: FilmDAO,
    onAuthenticated: (User) -> Unit,
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatchMovieBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Renderizzo il logo dell'app
        Image(
            painter = painterResource(id = R.drawable.matchmovie_logo),
            contentDescription = "MatchMovie logo",
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(40.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (isRegisterMode) "Create account" else "Welcome back",
            color = MatchMovieLightText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        Spacer(modifier = Modifier.height(24.dp))

        if (isRegisterMode) {
            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
        )
        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
        )


        // Se utente è in fase di registrazione, mostro anche il campo per ripetere la password
        if (isRegisterMode) {
            Spacer(modifier = Modifier.height(12.dp))
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )
        }

        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MatchMovieMutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    message = null

                    try {

                        // Controllo se, durante la registrazione, viene utilizzato un account già esistente
                        val normalizedEmail = email.trim().lowercase()
                        if (isRegisterMode) {

                            // Se password e confirmPassword differiscono, segnalo errore
                            if (password != confirmPassword) {
                                message = "Passwords do not match."
                                return@launch
                            }

                            val existingUser = withContext(Dispatchers.IO) {
                                dao.getUserByEmail(normalizedEmail)
                            }
                            if (existingUser != null) {
                                message = "An account with this email already exists."
                                return@launch
                            }

                            val response = withContext(Dispatchers.IO) {

                                // Chiamo endpoint del backend per la registrazione
                                RetrofitInstance.api.register(
                                    RegisterRequestDto(
                                        name = name.trim(),
                                        email = normalizedEmail,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                    )
                                )
                            }
                            val user = User(
                                name = response.name.ifBlank { normalizedEmail },
                                email = response.email,
                                password = response.passwordHash,
                                profileImage = null,
                                bio = null,
                                createdAt = System.currentTimeMillis(),
                                isLoggedIn = true,
                            )

                            val savedUserId = withContext(Dispatchers.IO) {
                                dao.logoutAllUsers()
                                dao.insertUser(user)
                            }
                            onAuthenticated(user.copy(_id = savedUserId.toInt()))
                        } else {
                            val localUser = withContext(Dispatchers.IO) {
                                dao.getUserByEmail(normalizedEmail)
                            }

                            if (localUser == null) {
                                message = "No local account found for this email."
                                return@launch
                            }

                            val response = withContext(Dispatchers.IO) {

                                // Chiamo endpoint del backend per eseguire il login
                                RetrofitInstance.api.login(
                                    LoginRequestDto(
                                        email = normalizedEmail,
                                        password = password,
                                        passwordHash = localUser.password,
                                    )
                                )
                            }

                            // Se l'autenticazione è andata a buon fine, imposto tutti gli altri utenti come non attivi
                            if (response.authenticated) {
                                withContext(Dispatchers.IO) {
                                    dao.logoutAllUsers()

                                    // Imposto come attivo solo lo user corrente (`localUser`)
                                    dao.setUserLoggedIn(localUser.email)
                                }
                                onAuthenticated(localUser.copy(isLoggedIn = true))
                            } else {
                                message = "Invalid credentials."
                            }
                        }
                    } catch (e: Exception) {

                        // Stampo un semplice messaggio di errore in caso l'autenticazione fallisca
                        message = "Authentication failed."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    (!isRegisterMode || (name.isNotBlank() && confirmPassword.isNotBlank())),
            colors = ButtonDefaults.buttonColors(
                containerColor = MatchMoviePrimary,
                contentColor = MatchMovieLightText
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Please wait..." else if (isRegisterMode) "Register" else "Login")
        }

        TextButton(
            onClick = {
                isRegisterMode = !isRegisterMode
                confirmPassword = ""
                message = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isRegisterMode) "Already have an account? Login" else "New here? Register",
                color = MatchMoviePrimary
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MatchMovieLightText,
            unfocusedTextColor = MatchMovieLightText,
            focusedBorderColor = MatchMoviePrimary,
            unfocusedBorderColor = MatchMovieMutedText,
            cursorColor = MatchMoviePrimary,
            focusedLabelColor = MatchMoviePrimary,
            unfocusedLabelColor = MatchMovieMutedText
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
