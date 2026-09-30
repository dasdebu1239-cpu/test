package com.example.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage {
    BENGALI,
    ENGLISH
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.BENGALI }

object AppStrings {

    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "পারফেক্ট ক্যাপচার"
        AppLanguage.ENGLISH -> "Perfect Capture"
    }

    fun appSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ওয়েডিং ফটো সিলেকশন ও ফেস ডাউনলোড"
        AppLanguage.ENGLISH -> "Wedding Photo Selection & Face Download"
    }

    fun homeTab(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "অ্যালবাম"
        AppLanguage.ENGLISH -> "Albums"
    }

    fun faceSearchTab(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ফেস সার্চ"
        AppLanguage.ENGLISH -> "Face Search"
    }

    fun selectionTab(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "বাছাই তালিকা"
        AppLanguage.ENGLISH -> "Selections"
    }

    fun adminTab(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ফটোগ্রাফার"
        AppLanguage.ENGLISH -> "Photographer"
    }

    fun findMyPhotos(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "নিজের ফেস দিয়ে ছবি খুঁজুন"
        AppLanguage.ENGLISH -> "Find My Photos by Face"
    }

    fun selfiePrompt(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "একটি সেলফি তুলুন বা গ্যালারি থেকে নিজের ছবি দিন"
        AppLanguage.ENGLISH -> "Take a selfie or choose your face photo"
    }

    fun scanButton(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ফেস স্ক্যান ও ছবি খুঁজুন"
        AppLanguage.ENGLISH -> "Scan Face & Find Photos"
    }

    fun takeLiveSelfie(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "লাইভ সেলফি তুলুন"
        AppLanguage.ENGLISH -> "Take Live Selfie"
    }

    fun pickFromGallery(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "গ্যালারি থেকে নিন"
        AppLanguage.ENGLISH -> "Pick from Gallery"
    }

    fun scanningFace(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "মুখমণ্ডল বিশ্লেষণ ও অ্যালবাম সার্চ হচ্ছে..."
        AppLanguage.ENGLISH -> "Analyzing facial features & scanning wedding album..."
    }

    fun photosFound(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "আপনার $count টি ছবি পাওয়া গেছে!"
        AppLanguage.ENGLISH -> "$count photos found with your face!"
    }

    fun downloadAllMatched(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "সবগুলো ছবি ডাউনলোড করুন"
        AppLanguage.ENGLISH -> "Download All Found Photos"
    }

    fun selectAllMatched(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "অ্যালবামে বাছাই করুন"
        AppLanguage.ENGLISH -> "Select All for Album"
    }

    fun downloadPhoto(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ডাউনলোড"
        AppLanguage.ENGLISH -> "Download"
    }

    fun selectForAlbum(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "অ্যালবামে পছন্দ"
        AppLanguage.ENGLISH -> "Select for Album"
    }

    fun submitToPhotographer(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ফটোগ্রাফারকে সিলেকশন পাঠান"
        AppLanguage.ENGLISH -> "Submit Selections to Studio"
    }

    fun photoNotesPlaceholder(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ফটোগ্রাফারের জন্য নোট লিখুন (যেমন: কভার পেজ, স্কিন রিটাচ)..."
        AppLanguage.ENGLISH -> "Add note for photographer (e.g. cover photo, skin retouch)..."
    }

    fun albumQuotaText(selected: Int, total: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "বাছাই: $selected / $total টি ছবি"
        AppLanguage.ENGLISH -> "Selected: $selected / $total photos"
    }

    fun downloadSuccess(title: String, lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "\"$title\" সফলভাবে গ্যালারিতে সেভ হয়েছে!"
        AppLanguage.ENGLISH -> "\"$title\" saved to gallery successfully!"
    }

    fun batchDownloadSuccess(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "$count টি ছবি সফলভাবে ডাউনলোড হয়েছে!"
        AppLanguage.ENGLISH -> "$count photos downloaded successfully!"
    }

    fun studioName(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "পারফেক্ট ক্যাপচার ফটোগ্রাফি"
        AppLanguage.ENGLISH -> "Perfect Capture Photography"
    }

    fun categories(lang: AppLanguage): Map<String, String> = when (lang) {
        AppLanguage.BENGALI -> mapOf(
            "ALL" to "সব ছবি",
            "BRIDE" to "কনে (Bride)",
            "GROOM" to "বর (Groom)",
            "COUPLE" to "যুগল (Couple)",
            "RITUALS" to "অনুষ্ঠান (Rituals)",
            "CANDID_GUESTS" to "অতিথি ও আনন্দ (Candid)",
            "RECEPTION" to "রিসেপশন (Reception)"
        )
        AppLanguage.ENGLISH -> mapOf(
            "ALL" to "All",
            "BRIDE" to "Bride",
            "GROOM" to "Groom",
            "COUPLE" to "Couple",
            "RITUALS" to "Rituals",
            "CANDID_GUESTS" to "Candid & Guests",
            "RECEPTION" to "Reception"
        )
    }
}
