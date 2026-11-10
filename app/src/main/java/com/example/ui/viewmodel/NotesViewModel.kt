package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.LiquidColorScheme
import com.example.data.model.NoteItem
import com.example.data.model.NotebookItem
import com.example.data.model.SectionItem
import com.example.data.model.SyncState
import com.example.data.model.UserPreferences
import com.example.data.repository.NoteRepository
import com.example.speech.SpeechTranscriptionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = NoteRepository(database.noteDao())
    val speechManager = SpeechTranscriptionManager(application)

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val transcriptionText: StateFlow<String> = speechManager.transcription
    val rmsVolume: StateFlow<Float> = speechManager.rmsVolume
    val speechStatusMessage: StateFlow<String?> = speechManager.statusMessage

    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val lastSyncTimestamp: StateFlow<Long> = repository.lastSyncTimestamp

    // User preferences
    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    fun updatePreferences(newPrefs: UserPreferences) {
        _preferences.value = newPrefs
    }

    // Notebooks
    val notebooks: StateFlow<List<NotebookItem>> = repository.notebooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNotebook = MutableStateFlow<NotebookItem?>(null)
    val selectedNotebook: StateFlow<NotebookItem?> = _selectedNotebook.asStateFlow()

    // Sections for selected notebook
    val sections: StateFlow<List<SectionItem>> = _selectedNotebook.flatMapLatest { nb ->
        if (nb != null) repository.getSectionsForNotebook(nb.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSection = MutableStateFlow<SectionItem?>(null)
    val selectedSection: StateFlow<SectionItem?> = _selectedSection.asStateFlow()

    // Search and Tags filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Active editing note (null if on main list)
    private val _activeEditingNote = MutableStateFlow<NoteItem?>(null)
    val activeEditingNote: StateFlow<NoteItem?> = _activeEditingNote.asStateFlow()

    // Reactive filtered notes
    val notes: StateFlow<List<NoteItem>> = combine(
        _selectedSection,
        _selectedNotebook,
        _searchQuery,
        _selectedTag
    ) { sec, nb, query, tag ->
        Triple(sec, nb, Pair(query, tag))
    }.flatMapLatest { (sec, nb, filters) ->
        val (query, tag) = filters
        val sourceFlow = when {
            query.isNotBlank() -> repository.searchNotes(query)
            sec != null -> repository.getNotesForSection(sec.id)
            nb != null -> repository.getNotesForNotebook(nb.id)
            else -> repository.allNotes
        }
        sourceFlow.combine(_selectedTag) { list, activeTag ->
            if (activeTag == null) list
            else list.filter { it.tags.any { t -> t.equals(activeTag, ignoreCase = true) } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically select first notebook and section once loaded
        viewModelScope.launch {
            notebooks.collect { list ->
                if (_selectedNotebook.value == null && list.isNotEmpty()) {
                    _selectedNotebook.value = list.first()
                }
            }
        }
        viewModelScope.launch {
            sections.collect { secList ->
                if (_selectedSection.value == null && secList.isNotEmpty()) {
                    _selectedSection.value = secList.first()
                }
            }
        }
    }

    fun selectNotebook(notebook: NotebookItem) {
        _selectedNotebook.value = notebook
        _selectedSection.value = null
    }

    fun selectSection(section: SectionItem) {
        _selectedSection.value = section
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun openNote(note: NoteItem) {
        _activeEditingNote.value = note
    }

    fun closeNote() {
        _activeEditingNote.value = null
        speechManager.stopListening()
    }

    fun saveNote(note: NoteItem) {
        viewModelScope.launch {
            repository.saveNote(note)
            _activeEditingNote.value = note
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            if (_activeEditingNote.value?.id == noteId) {
                _activeEditingNote.value = null
            }
        }
    }

    fun toggleFavorite(noteId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(noteId)
        }
    }

    fun togglePin(noteId: String) {
        viewModelScope.launch {
            repository.togglePin(noteId)
        }
    }

    fun createNotebook(title: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            val id = repository.createNotebook(title, colorHex, iconName)
            val created = repository.notebooks
        }
    }

    fun createSection(notebookId: String, name: String, colorHex: String) {
        viewModelScope.launch {
            val id = repository.createSection(notebookId, name, colorHex)
        }
    }

    fun createNewNote(notebookId: String, sectionId: String) {
        val newNote = NoteItem(
            id = UUID.randomUUID().toString(),
            sectionId = sectionId,
            notebookId = notebookId,
            title = "",
            content = "",
            tags = emptyList(),
            isFavorite = false,
            isPinned = false,
            hasAudioRecording = false,
            audioDurationSec = 0,
            audioTranscript = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.SYNCED,
            collaboratorsSummary = "You"
        )
        viewModelScope.launch {
            repository.saveNote(newNote)
            _activeEditingNote.value = newNote
        }
    }

    fun submitQuickScratchpad(text: String) {
        val nbId = _selectedNotebook.value?.id ?: notebooks.value.firstOrNull()?.id ?: return
        val secId = _selectedSection.value?.id ?: sections.value.firstOrNull()?.id ?: return
        val title = if (text.length > 30) text.take(30) + "..." else text
        val newNote = NoteItem(
            id = UUID.randomUUID().toString(),
            sectionId = secId,
            notebookId = nbId,
            title = "Quick Note: $title",
            content = text,
            tags = listOf("scratchpad", "quick_note"),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveNote(newNote)
        }
    }

    fun startVoiceRecording() {
        speechManager.startListening { transcribedText ->
            // If in active note, append; otherwise create a voice note
            val active = _activeEditingNote.value
            if (active != null) {
                val updatedContent = if (active.content.isBlank()) transcribedText else "${active.content}\n\n$transcribedText"
                saveNote(active.copy(
                    content = updatedContent,
                    hasAudioRecording = true,
                    audioTranscript = transcribedText,
                    audioDurationSec = 30
                ))
            } else {
                // Create a standalone lecture voice note
                val nbId = _selectedNotebook.value?.id ?: notebooks.value.firstOrNull()?.id ?: return@startListening
                val secId = _selectedSection.value?.id ?: sections.value.firstOrNull()?.id ?: return@startListening
                val voiceNote = NoteItem(
                    id = UUID.randomUUID().toString(),
                    sectionId = secId,
                    notebookId = nbId,
                    title = "Voice Memo: Lecture Recording",
                    content = "# Lecture Voice Transcript\n\n$transcribedText\n\n- Transcribed via Lumina Note speech engine",
                    tags = listOf("voice_memo", "lecture"),
                    hasAudioRecording = true,
                    audioTranscript = transcribedText,
                    audioDurationSec = 30,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                viewModelScope.launch {
                    repository.saveNote(voiceNote)
                }
            }
        }
    }

    fun stopVoiceRecording() {
        speechManager.stopListening()
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.triggerCloudSync()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
