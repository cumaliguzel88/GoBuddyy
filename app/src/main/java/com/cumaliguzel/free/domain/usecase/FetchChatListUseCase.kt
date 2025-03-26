package com.cumaliguzel.free.domain.usecase


import com.cumaliguzel.free.data.model.ChatSummary
import com.cumaliguzel.free.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchChatListUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatSummary>> {
        return chatRepository.fetchUserChats()
    }
}