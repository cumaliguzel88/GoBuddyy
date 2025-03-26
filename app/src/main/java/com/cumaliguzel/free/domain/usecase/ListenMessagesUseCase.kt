package com.cumaliguzel.free.domain.usecase


import com.cumaliguzel.free.data.model.ChatMessage
import com.cumaliguzel.free.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListenMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(chatId: String): Flow<List<ChatMessage>> {
        return chatRepository.listenForMessages(chatId)
    }
}