package com.cumaliguzel.free.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cumaliguzel.free.data.model.ChatSummary
import com.cumaliguzel.free.domain.usecase.FetchChatListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val fetchChatListUseCase: FetchChatListUseCase,
) : ViewModel() {

    private val _chatList = MutableStateFlow<List<ChatSummary>>(emptyList())
    val chatList: StateFlow<List<ChatSummary>> = _chatList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isInitialLoad = MutableStateFlow(true)
    val isInitialLoad: StateFlow<Boolean> = _isInitialLoad

    init {
        fetchChatList()
    }

    fun fetchChatList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("ChatListViewModel", "Sohbet listesi alınıyor...")
                fetchChatListUseCase().collectLatest { chats ->
                    Log.d("ChatListViewModel", "Alınan sohbet sayısı: ${chats.size}")
                    _chatList.value = chats.sortedByDescending { it.lastMessageTimestamp }
                    _isLoading.value = false
                    _isInitialLoad.value = false
                }
            } catch (e: Exception) {
                Log.e("ChatListViewModel", "Sohbet listesi alınamadı", e)
                _isLoading.value = false
                _isInitialLoad.value = false
            }
        }
    }

    fun refreshChatList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                fetchChatListUseCase().collect { chats ->
                    _chatList.value = chats.sortedByDescending { it.lastMessageTimestamp }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("ChatListViewModel", "Sohbet listesi alınamadı", e)
                _isLoading.value = false
            }
        }
    }
}