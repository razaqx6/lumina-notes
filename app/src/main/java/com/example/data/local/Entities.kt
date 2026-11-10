package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val colorHex: String,
    val iconName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val collaboratorsJson: String = "",
    val shareLink: String = ""
)

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey val id: String,
    val notebookId: String,
    val name: String,
    val colorHex: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val sectionId: String,
    val notebookId: String,
    val title: String,
    val content: String,
    val tagsJson: String = "",
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val hasAudioRecording: Boolean = false,
    val audioDurationSec: Int = 0,
    val audioTranscript: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String = "SYNCED",
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val collaboratorsSummary: String = "You"
)
