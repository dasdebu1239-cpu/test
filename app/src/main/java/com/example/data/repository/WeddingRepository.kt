package com.example.data.repository

import com.example.data.dao.WeddingDao
import com.example.data.model.ClientSubmission
import com.example.data.model.WeddingEvent
import com.example.data.model.WeddingPhoto
import kotlinx.coroutines.flow.Flow

class WeddingRepository(private val dao: WeddingDao) {

    fun getAllEvents(): Flow<List<WeddingEvent>> = dao.getAllEvents()

    suspend fun getEventById(id: Long): WeddingEvent? = dao.getEventById(id)

    suspend fun insertEvent(event: WeddingEvent): Long = dao.insertEvent(event)

    fun getPhotosForEvent(eventId: Long): Flow<List<WeddingPhoto>> = dao.getPhotosForEvent(eventId)

    fun getSelectedPhotos(eventId: Long): Flow<List<WeddingPhoto>> = dao.getSelectedPhotos(eventId)

    suspend fun insertPhotos(photos: List<WeddingPhoto>) = dao.insertPhotos(photos)

    suspend fun insertPhoto(photo: WeddingPhoto): Long = dao.insertPhoto(photo)

    suspend fun togglePhotoSelection(photo: WeddingPhoto, notes: String = photo.clientNotes) {
        dao.setPhotoSelected(photo.id, !photo.isSelected, notes)
    }

    suspend fun updatePhotoNotes(photoId: Long, notes: String) {
        val current = dao.getPhotoById(photoId)
        if (current != null) {
            dao.updatePhoto(current.copy(clientNotes = notes))
        }
    }

    suspend fun markPhotoDownloaded(photoId: Long) = dao.markPhotoDownloaded(photoId)

    suspend fun clearSelections(eventId: Long) = dao.clearSelectionsForEvent(eventId)

    suspend fun submitSelection(submission: ClientSubmission): Long = dao.insertSubmission(submission)

    fun getSubmissions(eventId: Long): Flow<List<ClientSubmission>> = dao.getSubmissionsForEvent(eventId)

    fun getAllSubmissions(): Flow<List<ClientSubmission>> = dao.getAllSubmissions()

    suspend fun deletePhoto(photoId: Long) = dao.deletePhoto(photoId)
}
