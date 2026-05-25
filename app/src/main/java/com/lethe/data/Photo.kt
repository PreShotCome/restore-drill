package com.lethe.data

import android.net.Uri

data class Photo(
    val id: Long,
    val uri: Uri,
    val dateTaken: Long,
)
