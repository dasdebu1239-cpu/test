package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.WeddingPhoto
import com.example.ui.common.AppLanguage
import com.example.ui.common.AppStrings
import com.example.ui.common.PhotoViewerDialog
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MaroonAccent
import com.example.ui.theme.SelectionHighlight
import com.example.ui.viewmodel.FaceSearchUiState

@Composable
fun FaceSearchScreen(
    state: FaceSearchUiState,
    language: AppLanguage,
    onPerformSearch: (Bitmap) -> Unit,
    onDownloadAll: (List<WeddingPhoto>) -> Unit,
    onSelectAllMatched: () -> Unit,
    onToggleSelectPhoto: (WeddingPhoto, String) -> Unit,
    onDownloadSinglePhoto: (WeddingPhoto) -> Unit
) {
    val context = LocalContext.current
    var viewingPhoto by remember { mutableStateOf<WeddingPhoto?>(null) }

    // Camera Launcher for Taking Live Selfie
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onPerformSearch(bitmap)
        }
    }

    // Photo Picker for choosing face from Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        onPerformSearch(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Scanner Laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_offset"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120F11))
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header & Scanner Card (Full Span)
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "ফেস রিকগনিশন ফটো সার্চ"
                    else
                        "AI Face Recognition Photo Search",
                    color = GoldPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "আপনার মুখমণ্ডল শনাক্ত করে ওয়েডিং অ্যালবামে আপনার উপস্থিতি আছে এমন সব ছবি খুঁজে বের করা হবে।"
                    else
                        "Upload a selfie to detect your face and instantly find every photo where you appear in the wedding album.",
                    color = Color(0xFFC7BBB3),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Face Oval Scanner Box
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF1F181B))
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(GoldPrimary, MaroonAccent, GoldLight, GoldPrimary)),
                            RoundedCornerShape(32.dp)
                        )
                        .testTag("face_scan_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.selfieBitmap != null) {
                        Image(
                            bitmap = state.selfieBitmap.asImageBitmap(),
                            contentDescription = "Uploaded Selfie",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (language == AppLanguage.BENGALI) "সেলফি দিন" else "Upload Face",
                                color = Color(0xFFB5A69E),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Scanning Laser Line
                    if (state.isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = laserOffset.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0xFFFFDF73), Color(0xFFE5A93C), Color.Transparent)
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Camera & Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("camera_selfie_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) "লাইভ সেলফি" else "Take Selfie",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E2226),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gallery_face_picker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) "গ্যালারি ছবি" else "From Gallery",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Quick Demo Face Presets (For fast test verification in browser emulator)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "অথবা টেস্ট করুন: " else "Or test demo face: ",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    // Bride Demo
                    Text(
                        text = if (language == AppLanguage.BENGALI) "কনে (Bride)" else "Bride",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF261A1E))
                            .clickable {
                                val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_sample_bride)
                                onPerformSearch(bmp)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("demo_face_bride")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Groom Demo
                    Text(
                        text = if (language == AppLanguage.BENGALI) "বর (Groom)" else "Groom",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2129))
                            .clickable {
                                val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_sample_groom)
                                onPerformSearch(bmp)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("demo_face_groom")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Guest/Candid Demo
                    Text(
                        text = if (language == AppLanguage.BENGALI) "অতিথি (Guest)" else "Guest",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF281F26))
                            .clickable {
                                val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_sample_candid)
                                onPerformSearch(bmp)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("demo_face_guest")
                    )
                }

                // Scanning Progress Indicator
                if (state.isScanning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = AppStrings.scanningFace(language),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Search Results Header & Batch Actions (Full Span)
        if (state.hasCompletedScan) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231B20)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.BENGALI)
                                    state.searchSummaryBengali
                                else
                                    state.searchSummaryEnglish,
                                color = GoldPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Matched Count Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldPrimary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${state.matchedPhotos.size}",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (state.matchedPhotos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Batch Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onDownloadAll(state.matchedPhotos.map { it.first }) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("download_all_matched_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.downloadAllMatched(language),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = onSelectAllMatched,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF38290D),
                                        contentColor = GoldLight
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("select_all_matched_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = SelectionHighlight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.selectAllMatched(language),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Grid of Matched Photos
            items(state.matchedPhotos, key = { it.first.id }) { (photo, score) ->
                MatchedPhotoCard(
                    photo = photo,
                    matchScore = score,
                    language = language,
                    onClick = { viewingPhoto = photo },
                    onToggleSelect = { onToggleSelectPhoto(photo, photo.clientNotes) },
                    onDownload = { onDownloadSinglePhoto(photo) }
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
                onToggleSelectPhoto(p, notes)
                viewingPhoto = p.copy(isSelected = !p.isSelected, clientNotes = notes)
            },
            onDownload = { p -> onDownloadSinglePhoto(p) }
        )
    }
}

@Composable
fun MatchedPhotoCard(
    photo: WeddingPhoto,
    matchScore: Float,
    language: AppLanguage,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current
    val percent = (matchScore * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("matched_card_${photo.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D171A)),
        border = if (photo.isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, SelectionHighlight)
        else
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38290D))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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

                // Match Accuracy Badge (Top Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "✨ $percent% " + if (language == AppLanguage.BENGALI) "মিল" else "Match",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Select / Favorite Button (Top Right)
                IconButton(
                    onClick = onToggleSelect,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(34.dp)
                        .background(
                            if (photo.isSelected) SelectionHighlight else Color(0x77000000),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (photo.isSelected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Select",
                        tint = if (photo.isSelected) Color.White else GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Download Button (Bottom Right)
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(34.dp)
                        .background(Color(0x99000000), CircleShape)
                        .testTag("download_matched_${photo.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = photo.caption,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
