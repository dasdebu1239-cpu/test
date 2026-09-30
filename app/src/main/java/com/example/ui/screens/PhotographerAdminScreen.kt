package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientSubmission
import com.example.data.model.WeddingEvent
import com.example.ui.common.AppLanguage
import com.example.ui.common.AppStrings
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SelectionHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotographerAdminScreen(
    events: List<WeddingEvent>,
    selectedEventId: Long?,
    submissions: List<ClientSubmission>,
    watermarkEnabled: Boolean,
    language: AppLanguage,
    onToggleWatermark: (Boolean) -> Unit,
    onCreateEvent: (title: String, couple: String, date: String, venue: String, quota: Int) -> Unit,
    onUploadPhotos: (uris: List<Uri>, category: String, captionPrefix: String) -> Unit
) {
    var newEventTitle by remember { mutableStateOf("") }
    var newCoupleNames by remember { mutableStateOf("") }
    var newEventDate by remember { mutableStateOf("") }
    var newEventVenue by remember { mutableStateOf("") }
    var newEventQuota by remember { mutableStateOf("40") }

    val categories = listOf("BRIDE", "GROOM", "COUPLE", "RITUALS", "CANDID_GUESTS", "RECEPTION")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var photoCaptionPrefix by remember { mutableStateOf("ওয়েডিং মোমেন্ট") }

    // Multi-photo picker from device gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 30)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onUploadPhotos(uris, selectedCategory, photoCaptionPrefix)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120F11)),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Dashboard Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF241A1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "ফটোগ্রাফার স্টুডিও কন্ট্রোল প্যানেল"
                        else
                            "Photographer Studio Control Panel",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "নতুন ইভেন্ট অ্যালবাম যুক্ত করুন, ক্যামেরা ছবি আপলোড করুন এবং কাস্টমারদের জমা দেওয়া সিলেকশন তালিকা দেখুন।"
                        else
                            "Manage client wedding albums, upload shoot photos, and review client selections.",
                        color = Color(0xFFC7BBB3),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Watermark setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BrandingWatermark,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "স্টুডিও ওয়াটারমার্ক" else "Studio Watermark",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "পারফেক্ট ক্যাপচার ফটোগ্রাফি ©" else "Perfect Capture Photography ©",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = watermarkEnabled,
                            onCheckedChange = onToggleWatermark,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GoldPrimary
                            ),
                            modifier = Modifier.testTag("watermark_switch")
                        )
                    }
                }
            }
        }

        // Upload Photos to Selected Event
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E181C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "অ্যালবামে নতুন ছবি আপলোড করুন"
                        else
                            "Upload Photos to Current Album",
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryMenuExpanded,
                        onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ক্যাটাগরি / Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
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
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = photoCaptionPrefix,
                        onValueChange = { photoCaptionPrefix = it },
                        label = { Text("ছবির শিরোনাম / Caption Prefix") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_photos_picker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI)
                                "ডিভাইস গ্যালারি থেকে ছবি সিলেক্ট করুন"
                            else
                                "Pick Photos from Device",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Create New Wedding Event
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E181C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "নতুন ওয়েডিং ইভেন্ট তৈরি করুন"
                        else
                            "Create New Wedding Event",
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newEventTitle,
                        onValueChange = { newEventTitle = it },
                        label = { Text("ইভেন্টের নাম (যেমন: রয়্যাল ওয়েডিং)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newCoupleNames,
                        onValueChange = { newCoupleNames = it },
                        label = { Text("বর ও কনের নাম (যেমন: সৌমিক ও মৌমিতা)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newEventDate,
                        onValueChange = { newEventDate = it },
                        label = { Text("অনুষ্ঠানের তারিখ") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newEventVenue,
                        onValueChange = { newEventVenue = it },
                        label = { Text("ভেন্যু / লোকেশন") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newEventQuota,
                        onValueChange = { newEventQuota = it },
                        label = { Text("সিলেকশন কোটা (ফটো সংখ্যা)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (newEventTitle.isNotBlank() && newCoupleNames.isNotBlank()) {
                                onCreateEvent(
                                    newEventTitle,
                                    newCoupleNames,
                                    newEventDate.ifBlank { "২০২৬" },
                                    newEventVenue.ifBlank { "কলকাতা" },
                                    newEventQuota.toIntOrNull() ?: 40
                                )
                                newEventTitle = ""
                                newCoupleNames = ""
                                newEventDate = ""
                                newEventVenue = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SelectionHighlight,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_event_submit_btn")
                    ) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "ইভেন্ট সংরক্ষণ করুন" else "Save Wedding Event",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Client Submissions Received
        item {
            Text(
                text = if (language == AppLanguage.BENGALI)
                    "কাস্টমারদের জমাকৃত সিলেকশন (${submissions.size}টি)"
                else
                    "Received Client Submissions (${submissions.size})",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (submissions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1618)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "এখনো কোনো কাস্টমার সিলেকশন জমা দেননি।"
                        else
                            "No client submissions received yet.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(submissions, key = { it.id }) { sub ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF221A1E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "👤 ${sub.clientName}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sub.selectedCount} টি ছবি",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (sub.clientPhone.isNotBlank()) {
                            Text(
                                text = "📞 ${sub.clientPhone}",
                                color = Color(0xFFDDD2CA),
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "📖 ${sub.albumType}",
                            color = Color(0xFFAFA299),
                            fontSize = 12.sp
                        )

                        if (sub.specialNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📝 \"${sub.specialNotes}\"",
                                color = SelectionHighlight,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "আইডি: ${sub.photoIdsSummary}",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
