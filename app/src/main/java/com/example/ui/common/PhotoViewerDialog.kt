package com.example.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.WeddingPhoto
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SelectionHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerDialog(
    photo: WeddingPhoto,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onToggleSelect: (WeddingPhoto, String) -> Unit,
    onDownload: (WeddingPhoto) -> Unit
) {
    val context = LocalContext.current
    var notesText by remember(photo.id) { mutableStateOf(photo.clientNotes) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0B0C)),
            color = Color(0xFF0D0B0C)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x33FFFFFF), CircleShape)
                            .testTag("close_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Download Button
                        Button(
                            onClick = { onDownload(photo) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("viewer_download_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.BENGALI) "ডাউনলোড" else "Download",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Select/Shortlist Button
                        IconButton(
                            onClick = { onToggleSelect(photo, notesText) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (photo.isSelected) SelectionHighlight else Color(0x33FFFFFF),
                                    CircleShape
                                )
                                .testTag("viewer_select_btn")
                        ) {
                            Icon(
                                imageVector = if (photo.isSelected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Select",
                                tint = if (photo.isSelected) Color.White else GoldPrimary
                            )
                        }
                    }
                }

                // Photo Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1719)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photo.photoUri.startsWith("content://") || photo.photoUri.startsWith("file://")) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photo.photoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = photo.caption,
                            contentScale = ContentScale.Fit,
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
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Watermark Preview Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(Color(0x88000000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "পারফেক্ট ক্যাপচার ফটোগ্রাফি ©" else "Perfect Capture Photography ©",
                            color = Color(0xDDFFFFFF),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caption and Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A1C)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = photo.caption,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Category Tag
                            FilterChip(
                                selected = true,
                                onClick = {},
                                label = { Text(photo.category, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF38290D),
                                    selectedLabelColor = GoldPrimary
                                )
                            )

                            // Face info
                            if (photo.detectedFaceCount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xFF262124), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${photo.detectedFaceCount} " + if (language == AppLanguage.BENGALI) "মুখ শনাক্ত" else "Faces",
                                        color = Color(0xFFDDD0C8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Selection Status & Notes Input
                        Text(
                            text = if (language == AppLanguage.BENGALI) "ফটোগ্রাফারের জন্য নির্দেশনা / নোট:" else "Notes for Photographer:",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = {
                                notesText = it
                                onToggleSelect(photo.copy(isSelected = true), it)
                            },
                            placeholder = {
                                Text(
                                    text = if (language == AppLanguage.BENGALI)
                                        "যেমন: কভার পেজ ছবি, স্কিন স্মুথিং, কালো-সাদা এডিট..."
                                    else
                                        "e.g., Cover photo, skin retouching, B&W edit...",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("photo_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF4A3F35)
                            ),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}
