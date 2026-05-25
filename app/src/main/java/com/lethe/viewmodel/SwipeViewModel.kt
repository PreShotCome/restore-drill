package com.lethe.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lethe.data.Photo
import com.lethe.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwipeUiState(
    val loading: Boolean = true,
    val photos: List<Photo> = emptyList(),
    val index: Int = 0,
    val pendingTrash: List<Uri> = emptyList(),
    val kept: Int = 0,
) {
    val current: Photo? get() = photos.getOrNull(index)
    val next: Photo? get() = photos.getOrNull(index + 1)
    val finished: Boolean get() = !loading && index >= photos.size
}

class SwipeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PhotoRepository(app.contentResolver)

    private val _state = MutableStateFlow(SwipeUiState())
    val state: StateFlow<SwipeUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val photos = repo.loadAll()
            _state.update {
                SwipeUiState(loading = false, photos = photos)
            }
        }
    }

    fun keep() {
        _state.update {
            if (it.current == null) it
            else it.copy(index = it.index + 1, kept = it.kept + 1)
        }
    }

    fun discard() {
        _state.update {
            val cur = it.current ?: return@update it
            it.copy(
                index = it.index + 1,
                pendingTrash = it.pendingTrash + cur.uri,
            )
        }
    }

    fun buildTrashRequest(): android.app.PendingIntent? {
        val uris = _state.value.pendingTrash
        if (uris.isEmpty()) return null
        return MediaStore.createTrashRequest(
            getApplication<Application>().contentResolver,
            uris,
            true,
        )
    }

    fun onTrashConfirmed() {
        _state.update { it.copy(pendingTrash = emptyList()) }
    }
}
