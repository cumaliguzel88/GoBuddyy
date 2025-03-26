package com.cumaliguzel.free.domain.usecase


import com.cumaliguzel.free.data.model.ChatMessage
import com.cumaliguzel.free.data.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        chatId: String,
        message: ChatMessage,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        chatRepository.sendMessage(chatId, message, onSuccess, onFailure)
    }
}