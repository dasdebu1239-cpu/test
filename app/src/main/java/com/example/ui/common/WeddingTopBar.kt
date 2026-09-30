package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary

@Composable
fun WeddingTopBar(
    currentLanguage: AppLanguage,
    selectedCount: Int,
    isAdminMode: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleAdminMode: () -> Unit,
    onNavigateToSelections: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141113))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo & Studio Brand
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_brand_header")
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF38290D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = AppStrings.appTitle(currentLanguage),
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = AppStrings.studioName(currentLanguage),
                    color = Color(0xFFAFA29A),
                    fontSize = 11.sp
                )
            }
        }

        // Action icons (Language Switcher, Selections Badge, Admin Toggle)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Language Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF262024))
                    .clickable(onClick = onToggleLanguage)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("lang_toggle_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Switch language",
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (currentLanguage == AppLanguage.BENGALI) "বাং" else "EN",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Selections Cart Icon with Badge
            IconButton(
                onClick = onNavigateToSelections,
                modifier = Modifier.testTag("selections_cart_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (selectedCount > 0) {
                            Badge(
                                containerColor = Color(0xFFE5A93C),
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = "$selectedCount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Selections",
                        tint = if (selectedCount > 0) GoldPrimary else Color(0xFF8E8178)
                    )
                }
            }

            // Photographer Admin Mode Toggle
            IconButton(
                onClick = onToggleAdminMode,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isAdminMode) GoldPrimary else Color(0xFF262024))
                    .size(36.dp)
                    .testTag("admin_mode_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ManageAccounts,
                    contentDescription = "Photographer Admin Mode",
                    tint = if (isAdminMode) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
