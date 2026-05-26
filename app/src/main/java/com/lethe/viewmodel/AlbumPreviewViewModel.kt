package com.lethe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lethe.data.Photo
import com.lethe.data.PhotoRepository
import com.lethe.data.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlbumPreviewUiState(
    val loading: Boolean = true,
    val photos: List<Photo> = emptyList(),
    val processed: Set<Long> = emptySet(),
) {
    val unprocessedCount: Int get() = photos.count { it.id !in processed }
    val firstUnprocessedId: Long? get() = photos.firstOrNull { it.id !in processed }?.id
    val firstUnprocessedIndex: Int
        get() = photos.indexOfFirst { it.id !in processed }.coerceAtLeast(0)
}

class AlbumPreviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PhotoRepository(app.contentResolver)
    private val session = SessionStore(app)

    private val _state = MutableStateFlow(AlbumPreviewUiState())
    val state: StateFlow<AlbumPreviewUiState> = _state.asStateFlow()

    fun load(bucketId: String) {
        viewModelScope.launch {
            _state.value = AlbumPreviewUiState(loading = true)
            val photos = repo.loadPhotos(bucketId)
            val processed = session.loadProcessed()
            _state.value = AlbumPreviewUiState(
                loading = false,
                photos = photos,
                processed = processed,
            )
        }
    }
}
