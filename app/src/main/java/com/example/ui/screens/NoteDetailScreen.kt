package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.SmartNoteAnalyzer
import com.example.data.model.NoteItem
import com.example.data.model.NotebookItem
import com.example.data.model.SectionItem
import com.example.ui.components.CollaboratorSheet
import com.example.ui.components.MarkdownViewer
import com.example.ui.components.RichTextEditorToolbar
import com.example.ui.components.VoiceRecordingBar
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassGradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: NoteItem,
    notebook: NotebookItem?,
    section: SectionItem?,
    isDark: Boolean,
    isSepia: Boolean,
    isListening: Boolean,
    transcriptionText: String,
    rmsVolume: Float,
    speechStatusMessage: String?,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onSaveNote: (NoteItem) -> Unit,
    onDeleteNote: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var title by remember(note.id) { mutableStateOf(note.title) }
    var content by remember(note.id) { mutableStateOf(note.content) }
    var tags by remember(note.id) { mutableStateOf(note.tags) }
    var isFavorite by remember(note.id) { mutableStateOf(note.isFavorite) }
    var isPinned by remember(note.id) { mutableStateOf(note.isPinned) }

    var isPreviewMode by remember { mutableStateOf(false) }
    var isZenMode by remember { mutableStateOf(false) }
    var showCollaboratorSheet by remember { mutableStateOf(false) }
    var showAiAnalysisDialog by remember { mutableStateOf(false) }
    var aiAnalysisResult by remember { mutableStateOf<SmartNoteAnalyzer.AiAnalysisResult?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Auto-save when leaving or typing
    val currentNote = remember(title, content, tags, isFavorite, isPinned) {
        note.copy(
            title = title,
            content = content,
            tags = tags,
            isFavorite = isFavorite,
            isPinned = isPinned,
            updatedAt = System.currentTimeMillis()
        )
    }

    BackHandler {
        onSaveNote(currentNote)
        onNavigateBack()
    }

    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
    }
    val readingTime = remember(wordCount) {
        maxOf(1, (wordCount / 200))
    }

    val primaryAccent = notebook?.colorHex?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (_: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .liquidGlassGradientBackground(
                primaryColor = primaryAccent,
                isDark = isDark,
                isSepia = isSepia
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Top Navigation & Stats Bar (hidden in Zen mode for full distraction-free writing)
            AnimatedVisibility(
                visible = !isZenMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                onSaveNote(currentNote)
                                onNavigateBack()
                            },
                            modifier = Modifier.testTag("note_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Notebook and section badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${notebook?.title ?: "Notebook"} • ${section?.name ?: "Section"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryAccent,
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Word Count Live Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$wordCount words • $readingTime min read",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Zen mode toggle
                        IconButton(
                            onClick = { isZenMode = true },
                            modifier = Modifier.testTag("zen_mode_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Zen Focus Mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Pin toggle
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin Note",
                                tint = if (isPinned) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Favorite toggle
                        IconButton(onClick = { isFavorite = !isFavorite }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Collaborator sheet
                        IconButton(
                            onClick = { showCollaboratorSheet = true },
                            modifier = Modifier.testTag("collaborate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Collaborate",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Delete
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Note",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            // Zen mode exit button floating header
            AnimatedVisibility(visible = isZenMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zen Focus Mode ($wordCount words)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { isZenMode = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Zen Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Audio transcript banner if audio was transcribed
            if (note.hasAudioRecording && note.audioTranscript.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .liquidGlass(
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = Color(0xFF0284C7).copy(alpha = 0.12f),
                            borderColor = Color(0xFF0284C7).copy(alpha = 0.3f),
                            elevation = 1.dp
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Recorded Lecture Voice Memo (${note.audioDurationSec}s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                            Text(
                                text = "\"${note.audioTranscript}\"",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tags display & adder
            if (tags.isNotEmpty() && !isZenMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Note Content Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isPreviewMode) {
                    // Interactive Rich Rendered Markdown Preview
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 8.dp, bottom = 80.dp)
                    ) {
                        Text(
                            text = title.ifBlank { "Untitled Note" },
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MarkdownViewer(
                            content = content,
                            onToggleCheckbox = { lineIdx, isChecked ->
                                val lines = content.lines().toMutableList()
                                if (lineIdx < lines.size) {
                                    val currentLine = lines[lineIdx].trim()
                                    lines[lineIdx] = if (isChecked) {
                                        currentLine.replaceFirst("[ ]", "[x]")
                                    } else {
                                        currentLine.replaceFirst("[x]", "[ ]")
                                    }
                                    content = lines.joinToString("\n")
                                }
                            }
                        )
                    }
                } else {
                    // Clean Distraction-Free Text Editor
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 8.dp, bottom = 80.dp)
                    ) {
                        // Title Input
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = {
                                Text(
                                    "Note Title",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            textStyle = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Body Content Input
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = {
                                Text(
                                    "Start writing notes, lectures, formulas, or checklists...",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .testTag("note_content_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }

                // Voice Recording Overlay Bar
                androidx.compose.animation.AnimatedVisibility(
                    visible = isListening,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                ) {
                    VoiceRecordingBar(
                        isListening = isListening,
                        transcriptionText = transcriptionText,
                        rmsVolume = rmsVolume,
                        statusMessage = speechStatusMessage,
                        onStopAndApply = { textToInsert ->
                            content = if (content.isBlank()) textToInsert else "$content\n\n$textToInsert"
                            onStopVoice()
                        },
                        onCancel = onStopVoice
                    )
                }

                // Docked/Floating Rich Formatting Toolbar
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isListening,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 8.dp)
                ) {
                    RichTextEditorToolbar(
                        onInsertText = { prefix, suffix ->
                            content = "$content$prefix$suffix"
                        },
                        onInsertLinePrefix = { prefix ->
                            content = if (content.endsWith("\n") || content.isEmpty()) {
                                "$content$prefix"
                            } else {
                                "$content\n$prefix"
                            }
                        },
                        onVoiceClick = onStartVoice,
                        onAiTagClick = {
                            val analysis = SmartNoteAnalyzer.analyzeNote(title, content)
                            aiAnalysisResult = analysis
                            showAiAnalysisDialog = true
                        },
                        isPreviewMode = isPreviewMode,
                        onTogglePreview = { isPreviewMode = !isPreviewMode }
                    )
                }
            }
        }

        // AI Smart Analysis & Organization Dialog
        if (showAiAnalysisDialog && aiAnalysisResult != null) {
            val result = aiAnalysisResult!!
            AlertDialog(
                onDismissRequest = { showAiAnalysisDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Smart Analysis", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SUGGESTED TAGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED),
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            result.suggestedTags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("#$tag", fontSize = 12.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Text(
                            text = "SMART EXECUTIVE SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = result.summary,
                            fontSize = 13.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "EXTRACTED ACTION ITEMS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED),
                            letterSpacing = 0.8.sp
                        )
                        result.actionItems.forEach { action ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("• ", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                                Text(action, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Apply AI tags and append summary
                            val mergedTags = (tags + result.suggestedTags).distinct()
                            tags = mergedTags
                            val appendedSection = "\n\n## AI Executive Summary\n${result.summary}\n\n### Action Items\n" +
                                    result.actionItems.joinToString("\n") { "[ ] $it" }
                            content = "$content$appendedSection"
                            showAiAnalysisDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_ai_insights_button")
                    ) {
                        Text("Apply Tags & Summary")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showAiAnalysisDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        // Collaborators Bottom Sheet
        if (showCollaboratorSheet) {
            CollaboratorSheet(
                note = currentNote,
                notebook = notebook,
                onDismiss = { showCollaboratorSheet = false }
            )
        }

        // Delete Confirm Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Note?") },
                text = { Text("Are you sure you want to delete \"${title.ifBlank { "Untitled Note" }}\"? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteNote(note.id)
                            showDeleteConfirmDialog = false
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = false },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
