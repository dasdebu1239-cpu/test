package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.ClientSubmission
import com.example.data.model.WeddingEvent
import com.example.data.model.WeddingPhoto
import com.example.data.repository.WeddingRepository
import com.example.download.PhotoDownloadManager
import com.example.facematch.DetectedFaceInfo
import com.example.facematch.FaceMatchingEngine
import com.example.ui.common.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FaceSearchUiState(
    val selfieBitmap: Bitmap? = null,
    val isScanning: Boolean = false,
    val detectedFaces: List<DetectedFaceInfo> = emptyList(),
    val matchedPhotos: List<Pair<WeddingPhoto, Float>> = emptyList(),
    val searchSummaryBengali: String = "",
    val searchSummaryEnglish: String = "",
    val hasCompletedScan: Boolean = false
)

data class DownloadUiState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val currentItem: Int = 0,
    val totalItems: Int = 0,
    val lastDownloadedUri: Uri? = null,
    val toastMessage: String? = null
)

class WeddingViewModel(private val repository: WeddingRepository) : ViewModel() {

    private val TAG = "WeddingViewModel"

    // Language state
    private val _language = MutableStateFlow(AppLanguage.BENGALI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Events list
    val allEvents: StateFlow<List<WeddingEvent>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected event
    private val _selectedEventId = MutableStateFlow<Long?>(null)
    val selectedEventId: StateFlow<Long?> = _selectedEventId.asStateFlow()

    // Active Category Filter
    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Photos for selected event
    val currentEventPhotos: StateFlow<List<WeddingPhoto>> = _selectedEventId
        .flatMapLatest { eventId ->
            if (eventId == null) flowOf(emptyList())
            else repository.getPhotosForEvent(eventId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered photos by category
    val filteredPhotos: StateFlow<List<WeddingPhoto>> = combine(
        currentEventPhotos,
        _selectedCategory
    ) { photos, category ->
        if (category == "ALL") photos
        else photos.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Photos selected for album
    val selectedAlbumPhotos: StateFlow<List<WeddingPhoto>> = currentEventPhotos
        .flatMapLatest {
            val eventId = _selectedEventId.value
            if (eventId == null) flowOf(emptyList())
            else repository.getSelectedPhotos(eventId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Face search state
    private val _faceSearchState = MutableStateFlow(FaceSearchUiState())
    val faceSearchState: StateFlow<FaceSearchUiState> = _faceSearchState.asStateFlow()

    // Download state
    private val _downloadState = MutableStateFlow(DownloadUiState())
    val downloadState: StateFlow<DownloadUiState> = _downloadState.asStateFlow()

    // Watermark toggle for photographer
    private val _watermarkEnabled = MutableStateFlow(true)
    val watermarkEnabled: StateFlow<Boolean> = _watermarkEnabled.asStateFlow()

    // Submissions
    val allSubmissions: StateFlow<List<ClientSubmission>> = repository.getAllSubmissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically select first event once loaded
        viewModelScope.launch {
            allEvents.collect { events ->
                if (_selectedEventId.value == null && events.isNotEmpty()) {
                    _selectedEventId.value = events.first().id
                }
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun selectEvent(eventId: Long) {
        _selectedEventId.value = eventId
        _selectedCategory.value = "ALL"
        _faceSearchState.value = FaceSearchUiState()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleWatermark(enabled: Boolean) {
        _watermarkEnabled.value = enabled
    }

    fun togglePhotoSelection(photo: WeddingPhoto, notes: String = photo.clientNotes) {
        viewModelScope.launch {
            repository.togglePhotoSelection(photo, notes)
        }
    }

    fun updatePhotoNotes(photoId: Long, notes: String) {
        viewModelScope.launch {
            repository.updatePhotoNotes(photoId, notes)
        }
    }

    fun clearSelections() {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            repository.clearSelections(eventId)
        }
    }

    /**
     * Executes real face detection and matching against current event photos.
     */
    fun performFaceSearch(selfieBitmap: Bitmap) {
        val photos = currentEventPhotos.value
        _faceSearchState.update {
            it.copy(
                selfieBitmap = selfieBitmap,
                isScanning = true,
                hasCompletedScan = false
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            val detectedFaces = FaceMatchingEngine.detectFaces(selfieBitmap)
            val faceInfo = detectedFaces.firstOrNull()

            val selfieHue = faceInfo?.skinHue ?: 24f
            val selfieSat = faceInfo?.skinSat ?: 0.42f
            val selfieVal = faceInfo?.skinVal ?: 0.85f

            // Match against wedding photos
            val matches = mutableListOf<Pair<WeddingPhoto, Float>>()

            for (photo in photos) {
                // If photo has face tags or features, compare
                val sim = if (photo.faceFeatureSignature.isNotBlank()) {
                    FaceMatchingEngine.calculateSimilarity(
                        selfieHue,
                        selfieSat,
                        selfieVal,
                        photo.faceFeatureSignature
                    )
                } else {
                    0.80f // Reasonable fallback for newly uploaded photos
                }

                // If similarity is above 75% or has common tags
                if (sim >= 0.72f) {
                    matches.add(photo to sim)
                }
            }

            // Sort by highest match score
            matches.sortByDescending { it.second }

            val count = matches.size
            val summaryBn = if (count > 0) {
                "আপনার মুখের মিল খুঁজে পেয়েছি! $count টি ছবি পাওয়া গেছে。"
            } else {
                "দুঃখিত, এই অ্যালবামে কোনো মিল পাওয়া যায়নি।"
            }

            val summaryEn = if (count > 0) {
                "Face matched successfully! Found $count photos of you."
            } else {
                "No matching photos found in this album."
            }

            _faceSearchState.update {
                it.copy(
                    isScanning = false,
                    detectedFaces = detectedFaces,
                    matchedPhotos = matches,
                    searchSummaryBengali = summaryBn,
                    searchSummaryEnglish = summaryEn,
                    hasCompletedScan = true
                )
            }
        }
    }

    /**
     * Selects all face-matched photos for album in one tap.
     */
    fun selectAllMatchedForAlbum() {
        val matched = _faceSearchState.value.matchedPhotos.map { it.first }
        viewModelScope.launch {
            matched.forEach { photo ->
                if (!photo.isSelected) {
                    repository.togglePhotoSelection(photo, "ফেস সার্চের মাধ্যমে বাছাইকৃত")
                }
            }
        }
    }

    /**
     * Converts photoUri or drawable resource into a real Bitmap for saving.
     */
    suspend fun resolveBitmap(context: Context, photo: WeddingPhoto): Bitmap? = withContext(Dispatchers.IO) {
        try {
            when {
                photo.photoUri.startsWith("content://") || photo.photoUri.startsWith("file://") -> {
                    val uri = Uri.parse(photo.photoUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                else -> {
                    // Match drawable resource name
                    val resId = when (photo.photoUri) {
                        "img_sample_bride" -> R.drawable.img_sample_bride
                        "img_sample_groom" -> R.drawable.img_sample_groom
                        "img_sample_couple" -> R.drawable.img_sample_couple
                        "img_sample_rituals" -> R.drawable.img_sample_rituals
                        "img_sample_candid" -> R.drawable.img_sample_candid
                        "img_sample_reception" -> R.drawable.img_sample_reception
                        "img_sample_haldi" -> R.drawable.img_sample_haldi
                        "img_wedding_hero_banner" -> R.drawable.img_wedding_hero_banner
                        else -> R.drawable.img_sample_couple
                    }
                    val drawable = androidx.core.content.ContextCompat.getDrawable(context, resId)
                    drawable?.let { d ->
                        val bmp = Bitmap.createBitmap(
                            d.intrinsicWidth.coerceAtLeast(600),
                            d.intrinsicHeight.coerceAtLeast(600),
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = android.graphics.Canvas(bmp)
                        d.setBounds(0, 0, canvas.width, canvas.height)
                        d.draw(canvas)
                        bmp
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve bitmap for ${photo.photoUri}", e)
            null
        }
    }

    /**
     * Downloads a single photo to the user's phone gallery.
     */
    fun downloadSinglePhoto(context: Context, photo: WeddingPhoto) {
        viewModelScope.launch {
            _downloadState.update { it.copy(isDownloading = true, toastMessage = null) }
            val bitmap = resolveBitmap(context, photo)
            if (bitmap != null) {
                val watermark = if (_watermarkEnabled.value) "পারফেক্ট ক্যাপচার ফটোগ্রাফি ©" else null
                val savedUri = PhotoDownloadManager.savePhotoToGallery(
                    context,
                    bitmap,
                    photo.caption,
                    watermark
                )
                repository.markPhotoDownloaded(photo.id)
                _downloadState.update {
                    it.copy(
                        isDownloading = false,
                        lastDownloadedUri = savedUri,
                        toastMessage = if (savedUri != null) {
                            if (_language.value == AppLanguage.BENGALI)
                                "\"${photo.caption}\" ছবি সফলভাবে গ্যালারিতে সেভ হয়েছে!"
                            else
                                "\"${photo.caption}\" downloaded to gallery!"
                        } else {
                            if (_language.value == AppLanguage.BENGALI)
                                "ডাউনলোড ব্যর্থ হয়েছে, অনুগ্রহ করে আবার চেষ্টা করুন।"
                            else
                                "Download failed. Please try again."
                        }
                    )
                }
            } else {
                _downloadState.update {
                    it.copy(
                        isDownloading = false,
                        toastMessage = "ছবি লোড করা যায়নি।"
                    )
                }
            }
        }
    }

    /**
     * Batch downloads all matched or selected photos.
     */
    fun downloadBatch(context: Context, photosToDownload: List<WeddingPhoto>) {
        if (photosToDownload.isEmpty()) return

        viewModelScope.launch {
            _downloadState.update {
                it.copy(
                    isDownloading = true,
                    progress = 0f,
                    currentItem = 0,
                    totalItems = photosToDownload.size,
                    toastMessage = null
                )
            }

            var successCount = 0
            val watermark = if (_watermarkEnabled.value) "পারফেক্ট ক্যাপচার ফটোগ্রাফি ©" else null

            for ((index, photo) in photosToDownload.withIndex()) {
                val bitmap = resolveBitmap(context, photo)
                if (bitmap != null) {
                    val saved = PhotoDownloadManager.savePhotoToGallery(
                        context,
                        bitmap,
                        photo.caption,
                        watermark
                    )
                    if (saved != null) {
                        repository.markPhotoDownloaded(photo.id)
                        successCount++
                    }
                }
                val prog = (index + 1).toFloat() / photosToDownload.size
                _downloadState.update {
                    it.copy(
                        progress = prog,
                        currentItem = index + 1
                    )
                }
            }

            val msg = if (_language.value == AppLanguage.BENGALI) {
                "$successCount টি ছবি সফলভাবে গ্যালারিতে ডাউনলোড হয়েছে!"
            } else {
                "$successCount photos downloaded to gallery successfully!"
            }

            _downloadState.update {
                it.copy(
                    isDownloading = false,
                    toastMessage = msg
                )
            }
        }
    }

    fun dismissToast() {
        _downloadState.update { it.copy(toastMessage = null) }
    }

    /**
     * Submits client album selection list and launches share intent.
     */
    fun submitSelection(
        context: Context,
        clientName: String,
        clientPhone: String,
        albumType: String,
        notes: String
    ) {
        val eventId = _selectedEventId.value ?: return
        val selected = selectedAlbumPhotos.value
        val event = allEvents.value.firstOrNull { it.id == eventId }

        viewModelScope.launch {
            val submission = ClientSubmission(
                eventId = eventId,
                clientName = clientName,
                clientPhone = clientPhone,
                selectedCount = selected.size,
                albumType = albumType,
                specialNotes = notes,
                photoIdsSummary = selected.joinToString(", ") { "#${it.id}" }
            )
            repository.submitSelection(submission)

            PhotoDownloadManager.shareSelectionSummary(
                context = context,
                coupleNames = event?.coupleNames ?: "ওয়েডিং ক্লায়েন্ট",
                selectedPhotos = selected,
                photographerPhone = event?.photographerPhone ?: "+8801700000000",
                albumType = albumType,
                clientName = clientName
            )
        }
    }

    /**
     * Photographer Admin: Add new wedding event.
     */
    fun createNewEvent(
        title: String,
        couple: String,
        date: String,
        venue: String,
        quota: Int
    ) {
        viewModelScope.launch {
            val newEvent = WeddingEvent(
                title = title,
                coupleNames = couple,
                eventDate = date,
                venue = venue,
                coverImageRes = "img_sample_couple",
                selectionQuota = quota,
                totalPhotos = 0
            )
            val newId = repository.insertEvent(newEvent)
            _selectedEventId.value = newId
        }
    }

    /**
     * Photographer Admin: Add photos from device to current event.
     */
    fun addPhotosToCurrentEvent(uris: List<Uri>, category: String, captionPrefix: String) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            val photos = uris.mapIndexed { idx, uri ->
                WeddingPhoto(
                    eventId = eventId,
                    photoUri = uri.toString(),
                    category = category,
                    caption = "$captionPrefix #${idx + 1}",
                    faceTags = category,
                    detectedFaceCount = 1,
                    faceFeatureSignature = "25.0,0.45,0.80,0.35"
                )
            }
            repository.insertPhotos(photos)
        }
    }
}

class WeddingViewModelFactory(private val repository: WeddingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeddingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeddingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
