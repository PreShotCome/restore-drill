package com.lethe.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lethe.data.Photo
import com.lethe.data.PhotoRepository
import com.lethe.data.SessionStore
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
    private val session = SessionStore(app)

    private val _state = MutableStateFlow(SwipeUiState())
    val state: StateFlow<SwipeUiState> = _state.asStateFlow()

    private var loadedBucket: String? = null

    fun load(bucketId: String?) {
        loadedBucket = bucketId
        viewModelScope.launch {
            _state.update { SwipeUiState(loading = true) }
            val processed = session.loadProcessed()
            val pending = session.loadPendingTrash()
            val photos = repo.loadPhotos(bucketId).filter { it.id !in processed }
            _state.update {
                SwipeUiState(
                    loading = false,
                    photos = photos,
                    pendingTrash = pending,
                )
            }
        }
    }

    fun keep() {
        val cur = _state.value.current ?: return
        session.addProcessed(listOf(cur.id))
        _state.update {
            it.copy(index = it.index + 1, kept = it.kept + 1)
        }
    }

    fun discard() {
        val cur = _state.value.current ?: return
        session.addProcessed(listOf(cur.id))
        _state.update { st ->
            val next = st.pendingTrash + cur.uri
            session.savePendingTrash(next)
            st.copy(index = st.index + 1, pendingTrash = next)
        }
    }

    fun undo() {
        val st = _state.value
        if (st.index == 0) return
        val prev = st.photos[st.index - 1]
        session.removeProcessed(prev.id)
        val wasDiscarded = st.pendingTrash.lastOrNull() == prev.uri
        val newPending = if (wasDiscarded) st.pendingTrash.dropLast(1) else st.pendingTrash
        if (wasDiscarded) session.savePendingTrash(newPending)
        _state.update {
            it.copy(
                index = it.index - 1,
                pendingTrash = newPending,
                kept = if (wasDiscarded) it.kept else (it.kept - 1).coerceAtLeast(0),
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
        session.clearPendingTrash()
        _state.update { it.copy(pendingTrash = emptyList()) }
    }
}
