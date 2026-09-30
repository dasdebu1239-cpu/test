package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.WeddingPhoto
import com.example.ui.common.AppLanguage
import com.example.ui.common.AppStrings
import com.example.ui.common.PhotoViewerDialog
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SelectionHighlight

@Composable
fun EventGalleryScreen(
    photos: List<WeddingPhoto>,
    selectedCategory: String,
    selectedCount: Int,
    selectionQuota: Int,
    language: AppLanguage,
    onCategorySelected: (String) -> Unit,
    onToggleSelect: (WeddingPhoto, String) -> Unit,
    onDownloadPhoto: (WeddingPhoto) -> Unit,
    onNavigateToFaceSearch: () -> Unit,
    onNavigateToSelections: () -> Unit
) {
    var viewingPhoto by remember { mutableStateOf<WeddingPhoto?>(null) }
    val categories = AppStrings.categories(language)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120F11))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories.entries.toList()) { (key, label) ->
                    val isSelected = selectedCategory == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(key) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF211B1E),
                            labelColor = Color(0xFFD6CBC3)
                        ),
                        border = null,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_$key")
                    )
                }
            }

            // Photos Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    WeddingPhotoCard(
                        photo = photo,
                        language = language,
                        onClick = { viewingPhoto = photo },
                        onToggleSelect = { onToggleSelect(photo, photo.clientNotes) },
                        onDownload = { onDownloadPhoto(photo) }
                    )
                }
            }
        }

        // Bottom Sticky Floating Bar for Selections & Face Search
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            color = Color(0xFF241B20),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3A42))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Face Search Action
                IconButton(
                    onClick = onNavigateToFaceSearch,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF38290D), CircleShape)
                        .testTag("gallery_face_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Search by Face",
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Selection Counter Text
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(
                        text = AppStrings.albumQuotaText(selectedCount, selectionQuota, language),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.BENGALI) "অ্যালবাম প্রিন্টের জন্য" else "For Album Print",
                        color = Color(0xFFAEA199),
                        fontSize = 11.sp
                    )
                }

                // Review & Submit Button
                Button(
                    onClick = onNavigateToSelections,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SelectionHighlight,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("gallery_review_selections_btn")
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "তালিকা দেখুন" else "Review",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Fullscreen Viewer Dialog
        viewingPhoto?.let { photo ->
            PhotoViewerDialog(
                photo = photo,
                language = language,
                onDismiss = { viewingPhoto = null },
                onToggleSelect = { p, notes ->
                    onToggleSelect(p, notes)
                    viewingPhoto = p.copy(isSelected = !p.isSelected, clientNotes = notes)
                },
                onDownload = { p -> onDownloadPhoto(p) }
            )
        }
    }
}

@Composable
fun WeddingPhotoCard(
    photo: WeddingPhoto,
    language: AppLanguage,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("photo_card_${photo.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1719)),
        border = if (photo.isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, SelectionHighlight)
        else null
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF140F11))
            ) {
                if (photo.photoUri.startsWith("content://") || photo.photoUri.startsWith("file://")) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photo.photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = photo.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
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
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = photo.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dark gradient at bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000)))
                        )
                )

                // Select / Favorite Button (Top Right)
                IconButton(
                    onClick = onToggleSelect,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .background(
                            if (photo.isSelected) SelectionHighlight else Color(0x77000000),
                            CircleShape
                        )
                        .testTag("select_toggle_${photo.id}")
                ) {
                    Icon(
                        imageVector = if (photo.isSelected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Select photo",
                        tint = if (photo.isSelected) Color.White else GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Download Button (Top Left)
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(36.dp)
                        .background(Color(0x77000000), CircleShape)
                        .testTag("quick_download_${photo.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Face detected indicator
                if (photo.detectedFaceCount > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .background(Color(0x99000000), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${photo.detectedFaceCount}",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Caption
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = photo.caption,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                if (photo.clientNotes.isNotBlank()) {
                    Text(
                        text = "📝 ${photo.clientNotes}",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
