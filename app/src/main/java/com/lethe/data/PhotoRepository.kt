package com.lethe.data

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoRepository(private val resolver: ContentResolver) {

    suspend fun loadAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        )
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        data class Acc(var count: Int, var coverId: Long, var name: String)

        val buckets = linkedMapOf<String, Acc>()
        var total = 0
        var allCoverId = -1L

        resolver.query(collection, projection, null, null, sort)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bidCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val bucketId = cursor.getString(bidCol) ?: continue
                val bucketName = cursor.getString(nameCol) ?: "Unnamed"
                if (allCoverId == -1L) allCoverId = id
                total++
                val acc = buckets.getOrPut(bucketId) { Acc(0, id, bucketName) }
                acc.count++
            }
        }

        val albums = mutableListOf<Album>()
        if (total > 0) {
            albums += Album(
                id = Album.ALL_ID,
                name = "All photos",
                photoCount = total,
                coverUri = ContentUris.withAppendedId(collection, allCoverId),
            )
        }
        buckets.entries
            .sortedByDescending { it.value.count }
            .mapTo(albums) { (id, acc) ->
                Album(
                    id = id,
                    name = acc.name,
                    photoCount = acc.count,
                    coverUri = ContentUris.withAppendedId(collection, acc.coverId),
                )
            }
        albums
    }

    suspend fun loadPhotos(bucketId: String?): List<Photo> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
        )
        val (selection, args) = if (bucketId == null || bucketId == Album.ALL_ID) {
            null to null
        } else {
            "${MediaStore.Images.Media.BUCKET_ID} = ?" to arrayOf(bucketId)
        }
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val result = mutableListOf<Photo>()
        resolver.query(collection, projection, selection, args, sort)?.use { cursor ->
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
