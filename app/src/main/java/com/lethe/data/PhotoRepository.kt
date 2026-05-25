package com.lethe.data

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoRepository(private val resolver: ContentResolver) {

    suspend fun loadAll(): List<Photo> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
        )
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val result = mutableListOf<Photo>()
        resolver.query(collection, projection, null, null, sort)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val date = cursor.getLong(dateCol)
                result += Photo(
                    id = id,
                    uri = ContentUris.withAppendedId(collection, id),
                    dateTaken = date,
                )
            }
        }
        result
    }
}
