package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainNotesScreen
import com.example.ui.screens.NoteDetailScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.LuminaNoteTheme
import com.example.ui.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by viewModel.preferences.collectAsStateWithLifecycle()

            LuminaNoteTheme(preferences = preferences) {
                LuminaNoteApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LuminaNoteApp(viewModel: NotesViewModel) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val selectedNotebook by viewModel.selectedNotebook.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val activeNote by viewModel.activeEditingNote.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()

    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val transcriptionText by viewModel.transcriptionText.collectAsStateWithLifecycle()
    val rmsVolume by viewModel.rmsVolume.collectAsStateWithLifecycle()
    val speechStatusMessage by viewModel.speechStatusMessage.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Audio recording permission launcher
    var hasAudioPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.startVoiceRecording()
        }
    }

    fun requestVoiceRecord() {
        viewModel.startVoiceRecording()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val currentNote = activeNote
        if (currentNote != null) {
            // Screen 2: Note Detail & Distraction-Free Editor
            val currentNotebook = notebooks.firstOrNull { it.id == currentNote.notebookId } ?: selectedNotebook
            val currentSection = sections.firstOrNull { it.id == currentNote.sectionId } ?: selectedSection

            NoteDetailScreen(
                note = currentNote,
                notebook = currentNotebook,
                section = currentSection,
                isDark = preferences.isDarkMode,
                isSepia = preferences.isEyeComfortSepia,
                isListening = isListening,
                transcriptionText = transcriptionText,
                rmsVolume = rmsVolume,
                speechStatusMessage = speechStatusMessage,
                onStartVoice = { requestVoiceRecord() },
                onStopVoice = { viewModel.stopVoiceRecording() },
                onSaveNote = { viewModel.saveNote(it) },
                onDeleteNote = { viewModel.deleteNote(it) },
                onNavigateBack = { viewModel.closeNote() }
            )
        } else {
            // Screen 1: OneNote Hierarchy, Student Notebooks & Sections, Widgets & Notes List
            MainNotesScreen(
                notebooks = notebooks,
                sections = sections,
                notes = notes,
                selectedNotebook = selectedNotebook,
                selectedSection = selectedSection,
                searchQuery = searchQuery,
                selectedTag = selectedTag,
                isSyncing = isSyncing,
                lastSyncTimestamp = lastSyncTimestamp,
                isDark = preferences.isDarkMode,
                isSepia = preferences.isEyeComfortSepia,
                onSelectNotebook = { viewModel.selectNotebook(it) },
                onSelectSection = { viewModel.selectSection(it) },
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSelectTag = { viewModel.setSelectedTag(it) },
                onCreateNotebook = { title, colorHex, icon -> viewModel.createNotebook(title, colorHex, icon) },
                onCreateSection = { nbId, name, colorHex -> viewModel.createSection(nbId, name, colorHex) },
                onCreateNote = { nbId, secId -> viewModel.createNewNote(nbId, secId) },
                onQuickScratchpadSubmit = { viewModel.submitQuickScratchpad(it) },
                onSelectNote = { viewModel.openNote(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onTogglePin = { viewModel.togglePin(it) },
                onTriggerSync = { viewModel.triggerCloudSync() },
                onOpenSettings = { showSettingsDialog = true },
                onOpenVoiceRecord = { requestVoiceRecord() }
            )
        }

        // Settings & Liquid Theme Customization Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                preferences = preferences,
                onUpdatePreferences = { viewModel.updatePreferences(it) },
                lastSyncTimestamp = lastSyncTimestamp,
                isSyncing = isSyncing,
                onTriggerSync = { viewModel.triggerCloudSync() },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
