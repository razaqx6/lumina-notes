package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.LiquidColorScheme
import com.example.data.model.NoteFontFamily
import com.example.data.model.UserPreferences
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsDialog(
    preferences: UserPreferences,
    onUpdatePreferences: (UserPreferences) -> Unit,
    lastSyncTimestamp: Long,
    isSyncing: Boolean,
    onTriggerSync: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Liquid Glass Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Appearance Mode
                Text(
                    text = "APPEARANCE & EYE COMFORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Light Mode
                    ThemeModeButton(
                        title = "Light",
                        icon = Icons.Default.LightMode,
                        isSelected = !preferences.isDarkMode && !preferences.isEyeComfortSepia,
                        modifier = Modifier.weight(1f)
                    ) {
                        onUpdatePreferences(preferences.copy(isDarkMode = false, isEyeComfortSepia = false))
                    }

                    // Dark Mode
                    ThemeModeButton(
                        title = "Dark",
                        icon = Icons.Default.DarkMode,
                        isSelected = preferences.isDarkMode && !preferences.isEyeComfortSepia,
                        modifier = Modifier.weight(1f)
                    ) {
                        onUpdatePreferences(preferences.copy(isDarkMode = true, isEyeComfortSepia = false))
                    }

                    // Sepia Eye Comfort
                    ThemeModeButton(
                        title = "Sepia",
                        icon = Icons.Default.MenuBook,
                        isSelected = preferences.isEyeComfortSepia,
                        modifier = Modifier.weight(1f)
                    ) {
                        onUpdatePreferences(preferences.copy(isEyeComfortSepia = !preferences.isEyeComfortSepia))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Section 2: Liquid Color Schemes
                Text(
                    text = "LIQUID COLOR SCHEMES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidColorScheme.values().forEach { scheme ->
                        val isSelected = preferences.colorScheme == scheme
                        val schemePrimary = Color(scheme.primaryHex)
                        val schemeGlow = Color(scheme.glowHex)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onUpdatePreferences(preferences.copy(colorScheme = scheme)) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(schemePrimary)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scheme.displayName.replace(" Glass", "").replace(" Chrome", "").replace(" Mist", "").replace(" Quartz", "").replace(" Sunset", ""),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Section 3: Typography & Reading Experience
                Text(
                    text = "TYPOGRAPHY & READING EXPERIENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                // Font Family Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoteFontFamily.values().forEach { font ->
                        val isSelected = preferences.fontFamily == font
                        Box(
                            modifier = Modifier
                                .liquidGlassPill(isSelected = isSelected, accentColor = MaterialTheme.colorScheme.primary)
                                .clickable { onUpdatePreferences(preferences.copy(fontFamily = font)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = font.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Font size slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Font Size", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("${preferences.fontSizeSp.toInt()} sp", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = preferences.fontSizeSp,
                        onValueChange = { onUpdatePreferences(preferences.copy(fontSizeSp = it)) },
                        valueRange = 14f..24f,
                        steps = 5
                    )
                }

                // Line spacing slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Line Spacing", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(String.format(Locale.getDefault(), "%.1fx", preferences.lineSpacingMultiplier), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = preferences.lineSpacingMultiplier,
                        onValueChange = { onUpdatePreferences(preferences.copy(lineSpacingMultiplier = it)) },
                        valueRange = 1.2f..2.0f,
                        steps = 4
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Section 4: Cloud Sync & Offline
                Text(
                    text = "SEAMLESS CLOUD SYNC & OFFLINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Sync Across Devices", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Real-time cloud sync engine", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = preferences.isAutoSyncEnabled,
                        onCheckedChange = { onUpdatePreferences(preferences.copy(isAutoSyncEnabled = it)) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Local Room DB: Offline Ready",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF059669)
                            )
                            val syncDateStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
                            Text(
                                text = if (isSyncing) "Sync in progress..." else "Last Synced: $syncDateStr",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onTriggerSync,
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("force_sync_button")
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSyncing) "Syncing" else "Sync Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("close_settings_button")
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun ThemeModeButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlassPill(isSelected = isSelected, accentColor = MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
