package com.example.data.model

enum class SyncState {
    SYNCED,
    LOCAL_ONLY,
    SYNCING,
    OFFLINE_PENDING
}

enum class LiquidColorScheme(val displayName: String, val primaryHex: Long, val glowHex: Long) {
    COBALT_GLASS("Cobalt Glass", 0xFF0284C7, 0xFF38BDF8),
    EMERALD_AURORA("Emerald Aurora", 0xFF059669, 0xFF34D399),
    AMETHYST_MIST("Amethyst Mist", 0xFF7C3AED, 0xFFA78BFA),
    ROSE_QUARTZ("Rose Quartz", 0xFFE11D48, 0xFFFB7185),
    AMBER_SUNSET("Amber Sunset", 0xFFD97706, 0xFFFBBF24),
    OBSIDIAN_CHROME("Obsidian Chrome", 0xFF475569, 0xFF94A3B8)
}

enum class NoteFontFamily(val displayName: String) {
    SANS("Modern Sans"),
    SERIF("Academic Serif"),
    MONO("Technical Mono"),
    DYSLEXIC("High Legibility")
}

data class NotebookItem(
    val id: String,
    val title: String,
    val colorHex: String,
    val iconName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val noteCount: Int = 0,
    val collaborators: List<String> = emptyList(),
    val shareLink: String = ""
)

data class SectionItem(
    val id: String,
    val notebookId: String,
    val name: String,
    val colorHex: String,
    val orderIndex: Int = 0,
    val noteCount: Int = 0
)

data class NoteItem(
    val id: String,
    val sectionId: String,
    val notebookId: String,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val hasAudioRecording: Boolean = false,
    val audioDurationSec: Int = 0,
    val audioTranscript: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.SYNCED,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val readingTimeMin: Int = 1,
    val collaboratorsSummary: String = "You"
)

data class UserPreferences(
    val colorScheme: LiquidColorScheme = LiquidColorScheme.COBALT_GLASS,
    val isDarkMode: Boolean = false,
    val isEyeComfortSepia: Boolean = false,
    val fontFamily: NoteFontFamily = NoteFontFamily.SANS,
    val fontSizeSp: Float = 16f,
    val lineSpacingMultiplier: Float = 1.4f,
    val isAutoSyncEnabled: Boolean = true,
    val isDistractionFreeMode: Boolean = false
)
