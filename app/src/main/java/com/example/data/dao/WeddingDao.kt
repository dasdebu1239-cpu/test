package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientSubmission
import com.example.data.model.WeddingEvent
import com.example.data.model.WeddingPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface WeddingDao {

    // Events
    @Query("SELECT * FROM wedding_events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<WeddingEvent>>

    @Query("SELECT * FROM wedding_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): WeddingEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: WeddingEvent): Long

    @Query("UPDATE wedding_events SET totalPhotos = :count WHERE id = :eventId")
    suspend fun updateEventPhotoCount(eventId: Long, count: Int)

    // Photos
    @Query("SELECT * FROM wedding_photos WHERE eventId = :eventId ORDER BY id ASC")
    fun getPhotosForEvent(eventId: Long): Flow<List<WeddingPhoto>>

    @Query("SELECT * FROM wedding_photos WHERE eventId = :eventId AND isSelected = 1 ORDER BY selectionOrder ASC, id ASC")
    fun getSelectedPhotos(eventId: Long): Flow<List<WeddingPhoto>>

    @Query("SELECT * FROM wedding_photos WHERE id = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: Long): WeddingPhoto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<WeddingPhoto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: WeddingPhoto): Long

    @Update
    suspend fun updatePhoto(photo: WeddingPhoto)

    @Query("UPDATE wedding_photos SET isSelected = :isSelected, clientNotes = :notes WHERE id = :photoId")
    suspend fun setPhotoSelected(photoId: Long, isSelected: Boolean, notes: String = "")

    @Query("UPDATE wedding_photos SET isDownloaded = 1 WHERE id = :photoId")
    suspend fun markPhotoDownloaded(photoId: Long)

    @Query("UPDATE wedding_photos SET isSelected = 0 WHERE eventId = :eventId")
    suspend fun clearSelectionsForEvent(eventId: Long)

    @Query("DELETE FROM wedding_photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: Long)

    // Submissions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: ClientSubmission): Long

    @Query("SELECT * FROM client_submissions WHERE eventId = :eventId ORDER BY submittedAt DESC")
    fun getSubmissionsForEvent(eventId: Long): Flow<List<ClientSubmission>>

    @Query("SELECT * FROM client_submissions ORDER BY submittedAt DESC")
    fun getAllSubmissions(): Flow<List<ClientSubmission>>
}
