package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteItem
import com.example.data.model.NotebookItem
import com.example.data.model.SectionItem
import com.example.ui.components.WidgetsCarousel
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassGradientBackground
import com.example.ui.theme.liquidGlassPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainNotesScreen(
    notebooks: List<NotebookItem>,
    sections: List<SectionItem>,
    notes: List<NoteItem>,
    selectedNotebook: NotebookItem?,
    selectedSection: SectionItem?,
    searchQuery: String,
    selectedTag: String?,
    isSyncing: Boolean,
    lastSyncTimestamp: Long,
    isDark: Boolean,
    isSepia: Boolean,
    onSelectNotebook: (NotebookItem) -> Unit,
    onSelectSection: (SectionItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectTag: (String?) -> Unit,
    onCreateNotebook: (title: String, colorHex: String, iconName: String) -> Unit,
    onCreateSection: (notebookId: String, name: String, colorHex: String) -> Unit,
    onCreateNote: (notebookId: String, sectionId: String) -> Unit,
    onQuickScratchpadSubmit: (String) -> Unit,
    onSelectNote: (NoteItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onTriggerSync: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVoiceRecord: () -> Unit
) {
    var showNewNotebookDialog by remember { mutableStateOf(false) }
    var showNewSectionDialog by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    // Extract all tags across notes
    val allTags = remember(notes) {
        notes.flatMap { it.tags }.distinct()
    }
    val totalWords = remember(notes) {
        notes.sumOf { it.wordCount }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .liquidGlassGradientBackground(primaryColor = primaryColor, isDark = isDark, isSepia = isSepia)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar with Liquid Glass header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(primaryColor, Color(0xFF38BDF8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Lumina Note",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        // Sync status badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSyncing) Color(0xFFF59E0B) else Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncing) "Syncing cloud..." else "Offline Ready",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Notes",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onTriggerSync,
                        modifier = Modifier.testTag("appbar_sync_button")
                    ) {
                        Icon(
                            imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                            contentDescription = "Sync",
                            tint = if (isSyncing) primaryColor else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Search Bar (expandable)
            AnimatedVisibility(visible = isSearchExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .liquidGlass(
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            elevation = 2.dp
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search notes, lectures, tags (#exam)...", fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_text_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Main Content: Hierarchy + Widgets + Notes
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: OneNote Notebooks row
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STUDENT NOTEBOOKS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "+ Add Notebook",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                modifier = Modifier
                                    .testTag("add_notebook_button")
                                    .clickable { showNewNotebookDialog = true }
                            )
                        }

                        // Notebook cards
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            notebooks.forEach { nb ->
                                val isSelected = selectedNotebook?.id == nb.id
                                val nbColor = try {
                                    Color(android.graphics.Color.parseColor(nb.colorHex))
                                } catch (_: Exception) {
                                    primaryColor
                                }

                                Box(
                                    modifier = Modifier
                                        .width(155.dp)
                                        .height(84.dp)
                                        .liquidGlass(
                                            shape = RoundedCornerShape(18.dp),
                                            backgroundColor = if (isSelected) nbColor.copy(alpha = 0.22f)
                                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            borderColor = if (isSelected) nbColor else Color.Transparent,
                                            borderWidth = if (isSelected) 2.dp else 1.dp,
                                            elevation = if (isSelected) 4.dp else 1.dp
                                        )
                                        .clickable { onSelectNotebook(nb) }
                                        .padding(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(nbColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getNotebookIcon(nb.iconName),
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            if (nb.isPinned) {
                                                Icon(
                                                    imageVector = Icons.Default.PushPin,
                                                    contentDescription = "Pinned",
                                                    tint = nbColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = nb.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${nb.noteCount} notes",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Sections pill tabs for selected notebook
                item {
                    if (selectedNotebook != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SECTIONS IN ${selectedNotebook.title.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "+ Section",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryColor,
                                    modifier = Modifier
                                        .testTag("add_section_button")
                                        .clickable { showNewSectionDialog = true }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sections.forEach { sec ->
                                    val isSelected = selectedSection?.id == sec.id
                                    Box(
                                        modifier = Modifier
                                            .liquidGlassPill(
                                                isSelected = isSelected,
                                                accentColor = primaryColor
                                            )
                                            .clickable { onSelectSection(sec) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = sec.name,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) primaryColor.copy(alpha = 0.25f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${sec.noteCount}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Customizable Widgets Carousel
                item {
                    WidgetsCarousel(
                        totalNotes = notes.size,
                        totalWords = totalWords,
                        notebooksCount = notebooks.size,
                        allTags = allTags,
                        selectedTag = selectedTag,
                        onTagSelect = onSelectTag,
                        onQuickScratchpadSubmit = onQuickScratchpadSubmit,
                        onQuickVoiceMemoClick = onOpenVoiceRecord,
                        onSyncClick = onTriggerSync,
                        isSyncing = isSyncing,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Section 4: Notes Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "SEARCH RESULTS (${notes.size})"
                            else if (selectedTag != null) "NOTES TAGGED #$selectedTag (${notes.size})"
                            else "${selectedSection?.name ?: "All"} Notes (${notes.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Section 5: Note Cards
                if (notes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .liquidGlass(
                                    shape = RoundedCornerShape(20.dp),
                                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = primaryColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No notes in this section yet",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Tap the + button to create a new lecture note or study guide.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(notes, key = { it.id }) { note ->
                        NoteCardItem(
                            note = note,
                            onNoteClick = { onSelectNote(note) },
                            onToggleFavorite = { onToggleFavorite(note.id) },
                            onTogglePin = { onTogglePin(note.id) },
                            primaryColor = primaryColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Floating Action Button ("+ New Note")
        FloatingActionButton(
            onClick = {
                val nbId = selectedNotebook?.id ?: notebooks.firstOrNull()?.id ?: ""
                val secId = selectedSection?.id ?: sections.firstOrNull()?.id ?: ""
                if (nbId.isNotBlank() && secId.isNotBlank()) {
                    onCreateNote(nbId, secId)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = primaryColor,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("create_note_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Note")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Note", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Dialog: Create Notebook
        if (showNewNotebookDialog) {
            var newNotebookTitle by remember { mutableStateOf("") }
            var selectedColorHex by remember { mutableStateOf("#0284C7") }
            var selectedIcon by remember { mutableStateOf("menu_book") }

            AlertDialog(
                onDismissRequest = { showNewNotebookDialog = false },
                title = { Text("Create Student Notebook", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newNotebookTitle,
                            onValueChange = { newNotebookTitle = it },
                            label = { Text("Notebook Title") },
                            placeholder = { Text("e.g. Organic Chemistry, Algorithms...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_notebook_title_input"),
                            singleLine = true
                        )

                        Text("Notebook Color Accent", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf("#0284C7", "#059669", "#7C3AED", "#E11D48", "#D97706", "#475569").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = hex }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newNotebookTitle.isNotBlank()) {
                                onCreateNotebook(newNotebookTitle.trim(), selectedColorHex, selectedIcon)
                                showNewNotebookDialog = false
                            }
                        },
                        enabled = newNotebookTitle.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_new_notebook_button")
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showNewNotebookDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Create Section
        if (showNewSectionDialog && selectedNotebook != null) {
            var newSectionName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showNewSectionDialog = false },
                title = { Text("Add Section to ${selectedNotebook.title}", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newSectionName,
                            onValueChange = { newSectionName = it },
                            label = { Text("Section Name") },
                            placeholder = { Text("e.g. Midterm Prep, Lecture Notes...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_section_name_input"),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSectionName.isNotBlank()) {
                                onCreateSection(selectedNotebook.id, newSectionName.trim(), selectedNotebook.colorHex)
                                showNewSectionDialog = false
                            }
                        },
                        enabled = newSectionName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_new_section_button")
                    ) {
                        Text("Add Section")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showNewSectionDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun NoteCardItem(
    note: NoteItem,
    onNoteClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(note.updatedAt) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(note.updatedAt))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                borderColor = if (note.isPinned) primaryColor.copy(alpha = 0.45f) else Color.Transparent,
                borderWidth = if (note.isPinned) 1.5.dp else 1.dp,
                elevation = 3.dp
            )
            .clickable(onClick = onNoteClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header row: Title + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = primaryColor,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(onClick = onTogglePin)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (note.isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onToggleFavorite)
                    )
                }
            }

            // Snippet content
            val snippet = remember(note.content) {
                note.content
                    .replace("#", "")
                    .replace("*", "")
                    .replace("`", "")
                    .take(120)
                    .replace("\n", " ")
            }
            if (snippet.isNotBlank()) {
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Audio transcript chip
            if (note.hasAudioRecording && note.audioTranscript.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Voice: \"${note.audioTranscript.take(45)}...\"",
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tags row
            if (note.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    note.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
                            )
                        }
                    }
                }
            }

            // Footer row: Date, Words, Collaborator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dateStr • ${note.wordCount} words",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Text(
                    text = note.collaboratorsSummary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor
                )
            }
        }
    }
}

private fun getNotebookIcon(iconName: String): ImageVector {
    return when (iconName) {
        "code" -> Icons.Default.Code
        "science" -> Icons.Default.Science
        "calculate" -> Icons.Default.Calculate
        "psychology" -> Icons.Default.Psychology
        else -> Icons.Default.MenuBook
    }
}
