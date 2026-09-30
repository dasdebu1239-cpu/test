package com.example.ui.common

import com.example.data.dao.WeddingDao
import com.example.data.model.WeddingEvent
import com.example.data.model.WeddingPhoto
import com.example.facematch.FaceMatchingEngine
import kotlinx.coroutines.flow.first

object SampleDataGenerator {

    suspend fun populateIfEmpty(dao: WeddingDao) {
        val existingEvents = dao.getAllEvents().first()
        if (existingEvents.isNotEmpty()) return

        // Insert Events
        val event1 = WeddingEvent(
            title = "রাজ ও প্রিয়ার রাজকীয় বিবাহ",
            coupleNames = "রাজেশ চক্রবর্তী ও প্রিয়াঙ্কা রায়",
            eventDate = "২৪ নভেম্বর ২০২৬",
            venue = "রয়্যাল ওবেরয় গ্র্যান্ড বলরুম, কলকাতা",
            coverImageRes = "img_sample_couple",
            pinCode = "2026",
            totalPhotos = 8,
            selectionQuota = 40,
            photographerName = "পারফেক্ট ক্যাপচার ফটোগ্রাফি",
            photographerPhone = "+8801712345678"
        )
        val eventId1 = dao.insertEvent(event1)

        val event2 = WeddingEvent(
            title = "অনির্বাণ ও স্নেহার গায়ে হলুদ ও রিসেপশন",
            coupleNames = "অনির্বাণ বোস ও স্নেহা দত্ত",
            eventDate = "১৮ ডিসেম্বর ২০২৬",
            venue = "হায়াত রিজেন্সি লন, কলকাতা",
            coverImageRes = "img_sample_haldi",
            pinCode = "1234",
            totalPhotos = 6,
            selectionQuota = 35,
            photographerName = "পারফেক্ট ক্যাপচার ফটোগ্রাফি",
            photographerPhone = "+8801712345678"
        )
        val eventId2 = dao.insertEvent(event2)

        // Signatures for sample faces:
        // Bride signature: warm soft hue around 24, sat 0.42, val 0.88, eyeRatio 0.35
        val brideSig = FaceMatchingEngine.createSignature(24f, 0.42f, 0.88f, 0.35f)
        // Groom signature: warm bronze hue around 26, sat 0.48, val 0.80, eyeRatio 0.38
        val groomSig = FaceMatchingEngine.createSignature(26f, 0.48f, 0.80f, 0.38f)
        // Guest/Friend signature: vibrant hue 22, sat 0.40, val 0.85, eyeRatio 0.33
        val guestSig = FaceMatchingEngine.createSignature(22f, 0.40f, 0.85f, 0.33f)

        // Event 1 Photos
        val photosEvent1 = listOf(
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_sample_bride",
                category = "BRIDE",
                caption = "কনের অপরূপ বেনারসি সাজ ও মুখচন্দ্রিকা",
                faceTags = "BRIDE,FEMALE",
                detectedFaceCount = 1,
                faceFeatureSignature = brideSig
            ),
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_sample_groom",
                category = "GROOM",
                caption = "রাজকীয় শেরওয়ানি ও পাগড়িতে বর রাজেশ",
                faceTags = "GROOM,MALE",
                detectedFaceCount = 1,
                faceFeatureSignature = groomSig
            ),
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_sample_couple",
                category = "COUPLE",
                caption = "মালাবদল ও শুভদৃষ্টির অমূল্য মুহূর্ত",
                faceTags = "BRIDE,GROOM,COUPLE",
                detectedFaceCount = 2,
                faceFeatureSignature = brideSig
            ),
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_sample_rituals",
                category = "RITUALS",
                caption = "পবিত্র অগ্নিসাক্ষী সিঁদুরদান পর্ব",
                faceTags = "BRIDE,GROOM,RITUAL",
                detectedFaceCount = 2,
                faceFeatureSignature = brideSig
            ),
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_sample_candid",
                category = "CANDID_GUESTS",
                caption = "সখীদের হাসি ও আনন্দঘন পুষ্পবৃষ্টি",
                faceTags = "GUESTS,FRIENDS,ANANYA",
                detectedFaceCount = 3,
                faceFeatureSignature = guestSig
            ),
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_sample_reception",
                category = "RECEPTION",
                caption = "রিসেপশন স্টেজে রাজকীয় শুভ প্রবেশ",
                faceTags = "BRIDE,GROOM,COUPLE",
                detectedFaceCount = 2,
                faceFeatureSignature = groomSig
            ),
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_sample_haldi",
                category = "RITUALS",
                caption = "গায়ে হলুদ ও সুবর্ণ স্মৃতি",
                faceTags = "BRIDE,FAMILY",
                detectedFaceCount = 1,
                faceFeatureSignature = brideSig
            ),
            WeddingPhoto(
                eventId = eventId1,
                photoUri = "img_wedding_hero_banner",
                category = "COUPLE",
                caption = "বিবাহ বন্ধন ও আলোকসজ্জা",
                faceTags = "COUPLE,STAGE",
                detectedFaceCount = 2,
                faceFeatureSignature = brideSig
            )
        )
        dao.insertPhotos(photosEvent1)
        dao.updateEventPhotoCount(eventId1, photosEvent1.size)

        // Event 2 Photos
        val photosEvent2 = listOf(
            WeddingPhoto(
                eventId = eventId2,
                photoUri = "img_sample_haldi",
                category = "RITUALS",
                caption = "স্নেহার গায়ে হলুদ ও পুষ্পসজ্জা",
                faceTags = "BRIDE,HALDI",
                detectedFaceCount = 1,
                faceFeatureSignature = brideSig
            ),
            WeddingPhoto(
                eventId = eventId2,
                photoUri = "img_sample_candid",
                category = "CANDID_GUESTS",
                caption = "বন্ধুবান্ধবদের উচ্ছ্বাস ও আড্ডা",
                faceTags = "GUESTS,FRIENDS",
                detectedFaceCount = 3,
                faceFeatureSignature = guestSig
            ),
            WeddingPhoto(
                eventId = eventId2,
                photoUri = "img_sample_reception",
                category = "RECEPTION",
                caption = "গ্র্যান্ড রিসেপশন ও সংবর্ধনা",
                faceTags = "COUPLE,RECEPTION",
                detectedFaceCount = 2,
                faceFeatureSignature = groomSig
            )
        )
        dao.insertPhotos(photosEvent2)
        dao.updateEventPhotoCount(eventId2, photosEvent2.size)
    }
}
