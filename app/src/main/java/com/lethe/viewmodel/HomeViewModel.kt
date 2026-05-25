package com.lethe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lethe.data.Album
import com.lethe.data.PhotoRepository
import com.lethe.data.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val albums: List<Album> = emptyList(),
    val processedCount: Int = 0,
    val pendingTrashCount: Int = 0,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PhotoRepository(app.contentResolver)
    private val session = SessionStore(app)

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeUiState(loading = true)
            val albums = repo.loadAlbums()
            _state.value = HomeUiState(
                loading = false,
                albums = albums,
                processedCount = session.loadProcessed().size,
                pendingTrashCount = session.loadPendingTrash().size,
            )
        }
    }
}
