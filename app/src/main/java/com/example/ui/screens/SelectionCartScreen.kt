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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.WeddingPhoto
import com.example.ui.common.AppLanguage
import com.example.ui.common.AppStrings
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SelectionHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionCartScreen(
    selectedPhotos: List<WeddingPhoto>,
    selectionQuota: Int,
    language: AppLanguage,
    onRemoveFromSelection: (WeddingPhoto) -> Unit,
    onUpdateNotes: (Long, String) -> Unit,
    onDownloadSelectedAll: (List<WeddingPhoto>) -> Unit,
    onSubmitSelection: (name: String, phone: String, albumType: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }

    val albumOptions = if (language == AppLanguage.BENGALI) listOf(
        "রয়্যাল লেদারেট ৪০-পৃষ্ঠা লাক্সারি অ্যালবাম",
        "ভেলভেট ফ্লাশমাউন্ট কভার বুক",
        "ম্যাগাজিন হার্ডকভার অ্যালবাম",
        "শুধুমাত্র ডিজিটাল হাই-রেজ কালেকশন"
    ) else listOf(
        "Royal Leatherette 40-Page Luxury Album",
        "Velvet Flushmount Presentation Book",
        "Magazine Hardcover Wedding Book",
        "Digital High-Res Collection Only"
    )

    var expandedAlbumMenu by remember { mutableStateOf(false) }
    var selectedAlbumType by remember { mutableStateOf(albumOptions.first()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120F11))
    ) {
        if (selectedPhotos.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoAlbum,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "এখনো কোনো ছবি বাছাই করা হয়নি"
                    else
                        "No photos shortlisted yet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "গ্যালারি বা ফেস সার্চ থেকে পছন্দের ছবির হার্ট বাটন ট্যাপ করে অ্যালবামের জন্য বাছাই করুন।"
                    else
                        "Tap the heart icon on any photo in the gallery or face search to shortlist for your album.",
                    color = Color(0xFFAFA299),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quota Progress Card
                item {
                    val progress = (selectedPhotos.size.toFloat() / selectionQuota.coerceAtLeast(1)).coerceIn(0f, 1f)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F181B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BENGALI)
                                        "অ্যালবাম সিলেকশন কোটা"
                                    else
                                        "Album Selection Quota",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = AppStrings.albumQuotaText(selectedPhotos.size, selectionQuota, language),
                                    color = SelectionHighlight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SelectionHighlight,
                                trackColor = Color(0xFF33292E)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Download All Shortlisted Photos
                            Button(
                                onClick = { onDownloadSelectedAll(selectedPhotos) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("download_all_selected_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI)
                                        "বাছাইকৃত সব ${selectedPhotos.size}টি ছবি ডাউনলোড করুন"
                                    else
                                        "Download All ${selectedPhotos.size} Shortlisted Photos",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Selected Photos List
                item {
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "বাছাইকৃত ছবির তালিকা (${selectedPhotos.size}টি)"
                        else
                            "Shortlisted Photos (${selectedPhotos.size})",
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(selectedPhotos, key = { it.id }) { photo ->
                    SelectedPhotoRow(
                        photo = photo,
                        language = language,
                        onRemove = { onRemoveFromSelection(photo) },
                        onUpdateNotes = { notes -> onUpdateNotes(photo.id, notes) }
                    )
                }

                // Client Details & Submission Form
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E191C)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (language == AppLanguage.BENGALI)
                                    "ফটোগ্রাফারকে সিলেকশন জমা দিন"
                                else
                                    "Submit Selections to Studio",
                                color = GoldPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Name
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it },
                                label = {
                                    Text(if (language == AppLanguage.BENGALI) "আপনার নাম" else "Your Name")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedLabelColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Phone
                            OutlinedTextField(
                                value = clientPhone,
                                onValueChange = { clientPhone = it },
                                label = {
                                    Text(if (language == AppLanguage.BENGALI) "মোবাইল / হোয়াটসঅ্যাপ নম্বর" else "Phone / WhatsApp Number")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_phone_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedLabelColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Album Style Picker
                            ExposedDropdownMenuBox(
                                expanded = expandedAlbumMenu,
                                onExpandedChange = { expandedAlbumMenu = !expandedAlbumMenu }
                            ) {
                                OutlinedTextField(
                                    value = selectedAlbumType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = {
                                        Text(if (language == AppLanguage.BENGALI) "অ্যালবামের ধরন" else "Album Type")
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlbumMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        focusedLabelColor = GoldPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedAlbumMenu,
                                    onDismissRequest = { expandedAlbumMenu = false }
                                ) {
                                    albumOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedAlbumType = option
                                                expandedAlbumMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Special Instructions
                            OutlinedTextField(
                                value = specialInstructions,
                                onValueChange = { specialInstructions = it },
                                label = {
                                    Text(if (language == AppLanguage.BENGALI) "ফটোগ্রাফারের জন্য বিশেষ নির্দেশনা" else "Special Instructions for Photographer")
                                },
                                placeholder = {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI)
                                            "যেমন: অ্যালবাম ডেলিভারি তারিখ, রিটাচিং স্টাইল..."
                                        else
                                            "e.g., Delivery date, color grading style..."
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("special_instructions_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedLabelColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Button
                            Button(
                                onClick = {
                                    val name = if (clientName.isBlank()) "সম্মানিত কাস্টমার" else clientName
                                    onSubmitSelection(name, clientPhone, selectedAlbumType, specialInstructions)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SelectionHighlight,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_selection_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.submitToPhotographer(language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedPhotoRow(
    photo: WeddingPhoto,
    language: AppLanguage,
    onRemove: () -> Unit,
    onUpdateNotes: (String) -> Unit
) {
    val context = LocalContext.current
    var isEditingNote by remember { mutableStateOf(false) }
    var currentNote by remember(photo.clientNotes) { mutableStateOf(photo.clientNotes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_row_${photo.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E171A))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Thumbnail
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Caption and Notes
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = photo.caption,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (photo.clientNotes.isNotBlank())
                            "নোট: ${photo.clientNotes}"
                        else
                            if (language == AppLanguage.BENGALI) "নোট যুক্ত করুন" else "Add note for print",
                        color = if (photo.clientNotes.isNotBlank()) GoldPrimary else Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Edit Note Action
                IconButton(
                    onClick = { isEditingNote = !isEditingNote },
                    modifier = Modifier.testTag("edit_note_btn_${photo.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Edit note",
                        tint = GoldPrimary
                    )
                }

                // Remove from selection
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_selection_${photo.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove",
                        tint = Color(0xFFC04E6F)
                    )
                }
            }

            // Expanded Note Editing Input
            if (isEditingNote) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentNote,
                        onValueChange = { currentNote = it },
                        placeholder = {
                            Text(
                                if (language == AppLanguage.BENGALI) "যেমন: কভার পেজ, স্কিন রিটাচ..." else "e.g., Cover photo...",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("note_input_${photo.id}"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF4A3F35)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            onUpdateNotes(currentNote)
                            isEditingNote = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (language == AppLanguage.BENGALI) "সেভ" else "Save", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
