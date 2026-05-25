package com.lethe.data

import android.net.Uri

data class Album(
    val id: String,
    val name: String,
    val photoCount: Int,
    val coverUri: Uri?,
) {
    companion object {
        const val ALL_ID = "__all__"
    }
}
