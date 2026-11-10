package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidColors
import com.example.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RichTextEditorToolbar(
    onInsertText: (prefix: String, suffix: String) -> Unit,
    onInsertLinePrefix: (prefix: String) -> Unit,
    onVoiceClick: () -> Unit,
    onAiTagClick: () -> Unit,
    isPreviewMode: Boolean,
    onTogglePreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHighlighterMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                elevation = 6.dp
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode toggle: Edit vs Rich Preview
            IconButton(
                onClick = onTogglePreview,
                modifier = Modifier
                    .testTag("preview_toggle_button")
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPreviewMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = if (isPreviewMode) Icons.Filled.Edit else Icons.Outlined.Visibility,
                    contentDescription = if (isPreviewMode) "Switch to Editor" else "Switch to Rich Preview",
                    tint = if (isPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ToolbarDivider()

            // Headers
            ToolbarPillButton(label = "H1", onClick = { onInsertLinePrefix("# ") })
            ToolbarPillButton(label = "H2", onClick = { onInsertLinePrefix("## ") })
            ToolbarPillButton(label = "H3", onClick = { onInsertLinePrefix("### ") })

            ToolbarDivider()

            // Styling
            ToolbarIconButton(
                icon = Icons.Default.FormatBold,
                description = "Bold",
                onClick = { onInsertText("**", "**") }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatItalic,
                description = "Italic",
                onClick = { onInsertText("*", "*") }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatStrikethrough,
                description = "Strikethrough",
                onClick = { onInsertText("~~", "~~") }
            )

            // Highlighter
            IconButton(
                onClick = { showHighlighterMenu = !showHighlighterMenu },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Highlight,
                    contentDescription = "Highlight",
                    tint = Color(0xFFF59E0B)
                )
            }

            if (showHighlighterMenu) {
                HighlighterDot(color = Color(0xFFFDE047)) {
                    onInsertText("<mark color=\"yellow\">", "</mark>")
                    showHighlighterMenu = false
                }
                HighlighterDot(color = Color(0xFF67E8F9)) {
                    onInsertText("<mark color=\"cyan\">", "</mark>")
                    showHighlighterMenu = false
                }
                HighlighterDot(color = Color(0xFF86EFAC)) {
                    onInsertText("<mark color=\"green\">", "</mark>")
                    showHighlighterMenu = false
                }
                HighlighterDot(color = Color(0xFFF472B6)) {
                    onInsertText("<mark color=\"pink\">", "</mark>")
                    showHighlighterMenu = false
                }
            }

            ToolbarDivider()

            // Lists & Checklists
            ToolbarIconButton(
                icon = Icons.Default.FormatListBulleted,
                description = "Bullet List",
                onClick = { onInsertLinePrefix("- ") }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatListNumbered,
                description = "Numbered List",
                onClick = { onInsertLinePrefix("1. ") }
            )
            ToolbarIconButton(
                icon = Icons.Default.CheckCircle,
                description = "Task Checkbox",
                onClick = { onInsertLinePrefix("[ ] ") }
            )

            ToolbarDivider()

            // Code & Quote
            ToolbarIconButton(
                icon = Icons.Default.Code,
                description = "Code Block",
                onClick = { onInsertText("```kotlin\n", "\n```") }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatQuote,
                description = "Blockquote",
                onClick = { onInsertLinePrefix("> ") }
            )

            // Date Stamp
            ToolbarIconButton(
                icon = Icons.Default.Today,
                description = "Insert Date",
                onClick = {
                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
                    onInsertText("**$dateStr** ", "")
                }
            )

            ToolbarDivider()

            // Voice Dictation
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .testTag("voice_dictation_button")
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7).copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Dictation",
                    tint = Color(0xFF0284C7)
                )
            }

            // AI Smart Tagging & Organization
            IconButton(
                onClick = onAiTagClick,
                modifier = Modifier
                    .testTag("ai_assistant_button")
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Smart Tagging & Summary",
                    tint = Color(0xFF7C3AED)
                )
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ToolbarPillButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 20.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
private fun HighlighterDot(
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
    )
}
