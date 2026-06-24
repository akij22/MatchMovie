package com.example.matchmovie.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchmovie.R
import com.example.matchmovie.components.InfoMessage
import com.example.matchmovie.enumentity.MessageSender
import com.example.matchmovie.model.ChatMessage
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.ChatRequestDto
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun AIChatScreen(
    chatMessages: List<ChatMessage>,
    onChatMessagesChange: (List<ChatMessage>) -> Unit,
    messagePrompt: String,
    onMessagePromptChange: (String) -> Unit
) {

    var hasSubmittedSearch by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            if (chatMessages.isEmpty()) {
                item {
                    InfoMessage(R.drawable.ai_chat, "Ask for your next movie recommendation")
                }

            // Renderizzo i messaggi nella chat
            } else {
                items(chatMessages) { message ->
                    ChatMessageBox(message)
                }
            }

            if (isSending) {
                item {
                    ChatMessageBox(
                        ChatMessage(
                            "...",
                            MessageSender.AIASSISTANT
                        )
                    )
                }
            }

            errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MatchMovieMutedText
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(MatchMovieCard)
                .padding(horizontal = 16.dp)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Barra di scrittura per l'invio di messaggi
            OutlinedTextField(
                value = messagePrompt,
                onValueChange = {
                    onMessagePromptChange(it)
                    if (it.isBlank()) {
                        hasSubmittedSearch = false
                    }
                },
                placeholder = { Text("Hey! What's next?") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MatchMovieLightText,
                    unfocusedTextColor = MatchMovieLightText,
                    focusedBorderColor = MatchMoviePrimary,
                    unfocusedBorderColor = MatchMovieMutedText,
                    cursorColor = MatchMoviePrimary,
                    focusedPlaceholderColor = MatchMovieMutedText,
                    unfocusedPlaceholderColor = MatchMovieMutedText
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (messagePrompt.isNotBlank()) {
                        val currentPrompt = messagePrompt.trim()
                        val updatedChatMessages = chatMessages + ChatMessage(
                            currentPrompt,
                            MessageSender.USER
                        )

                        // Aggiunta del messaggio corrente alla lista di messaggi sa mostrare nella chat (lato UI)
                        onChatMessagesChange(updatedChatMessages)

                        // Creazione di un oggetto data class di tipo ChatRequestDto
                        val request = ChatRequestDto(
                            messagePrompt = currentPrompt
                        )

                        onMessagePromptChange("")
                        hasSubmittedSearch = true

                        coroutineScope.launch {
                            isSending = true
                            errorMessage = null
                            try {

                                // Invoco l'API del backend per l'invio del messaggio ad un modello AI
                                val response = withContext(Dispatchers.IO) {
                                    RetrofitInstance.api.sendPrompt(request)
                                }
                                val assistantReply = runCatching {
                                    response.messageReply
                                }.getOrNull().orEmpty()


                                // Aggiunta della risposta del modello AI alla lista di messaggi della chat
                                onChatMessagesChange(
                                    updatedChatMessages + ChatMessage(
                                        assistantReply,
                                        MessageSender.AIASSISTANT
                                    )
                                )

                            } catch (e: Exception) {
                                errorMessage = "Unable to send message, please try again."
                            } finally {

                                isSending = false
                            }

                        }

                    }
                },

                enabled = messagePrompt.isNotBlank() && !isSending,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatchMoviePrimary,
                    contentColor = MatchMovieLightText
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(72.dp)
            ) {
                Text("↑")
            }
        }
    }
}



@Composable
fun ChatMessageBox(message: ChatMessage) {
    Box(
        modifier = Modifier

            // Imposto una lunghezza massima per i messaggi, per contenere in modo completo la risposta dell'assistente AI
            .widthIn(max = 400.dp)
            .padding(vertical = 4.dp),
        contentAlignment = if (message.whoSent == MessageSender.USER) {
            Alignment.CenterEnd
        } else {
            Alignment.CenterStart
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .background(
                    color = if (message.whoSent == MessageSender.USER) MatchMoviePrimary else MatchMovieCard,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.message,
                color = MatchMovieLightText
            )
        }
    }
}
