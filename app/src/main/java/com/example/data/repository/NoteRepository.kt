package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.local.NotebookEntity
import com.example.data.local.SectionEntity
import com.example.data.model.NoteItem
import com.example.data.model.NotebookItem
import com.example.data.model.SectionItem
import com.example.data.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class NoteRepository(private val noteDao: NoteDao) {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkAndSeedInitialData()
        }
    }

    // Notebooks with dynamic note counts
    val notebooks: Flow<List<NotebookItem>> = combine(
        noteDao.getAllNotebooks(),
        noteDao.getAllNotes()
    ) { notebookEntities, noteEntities ->
        val countMap = noteEntities.groupingBy { it.notebookId }.eachCount()
        notebookEntities.map { entity ->
            NotebookItem(
                id = entity.id,
                title = entity.title,
                colorHex = entity.colorHex,
                iconName = entity.iconName,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                isPinned = entity.isPinned,
                noteCount = countMap[entity.id] ?: 0,
                collaborators = if (entity.collaboratorsJson.isBlank()) emptyList() else entity.collaboratorsJson.split(",").map { it.trim() },
                shareLink = entity.shareLink
            )
        }
    }

    fun getSectionsForNotebook(notebookId: String): Flow<List<SectionItem>> {
        return combine(
            noteDao.getSectionsForNotebook(notebookId),
            noteDao.getAllNotes()
        ) { sectionEntities, noteEntities ->
            val countMap = noteEntities.groupingBy { it.sectionId }.eachCount()
            sectionEntities.map { entity ->
                SectionItem(
                    id = entity.id,
                    notebookId = entity.notebookId,
                    name = entity.name,
                    colorHex = entity.colorHex,
                    orderIndex = entity.orderIndex,
                    noteCount = countMap[entity.id] ?: 0
                )
            }
        }
    }

    fun getNotesForSection(sectionId: String): Flow<List<NoteItem>> {
        return noteDao.getNotesForSection(sectionId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getNotesForNotebook(notebookId: String): Flow<List<NoteItem>> {
        return noteDao.getNotesForNotebook(notebookId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    val allNotes: Flow<List<NoteItem>> = noteDao.getAllNotes().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val favoriteNotes: Flow<List<NoteItem>> = noteDao.getFavoriteNotes().map { entities ->
        entities.map { it.toDomainModel() }
    }

    fun searchNotes(query: String): Flow<List<NoteItem>> {
        return noteDao.searchNotes(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getNoteById(noteId: String): NoteItem? {
        return noteDao.getNoteById(noteId)?.toDomainModel()
    }

    suspend fun saveNote(note: NoteItem) {
        val wordCount = calculateWordCount(note.content)
        val entity = NoteEntity(
            id = note.id.ifBlank { UUID.randomUUID().toString() },
            sectionId = note.sectionId,
            notebookId = note.notebookId,
            title = note.title,
            content = note.content,
            tagsJson = note.tags.joinToString(","),
            isFavorite = note.isFavorite,
            isPinned = note.isPinned,
            hasAudioRecording = note.hasAudioRecording,
            audioDurationSec = note.audioDurationSec,
            audioTranscript = note.audioTranscript,
            createdAt = if (note.createdAt == 0L) System.currentTimeMillis() else note.createdAt,
            updatedAt = System.currentTimeMillis(),
            syncStatus = note.syncState.name,
            lastSyncedAt = note.lastSyncedAt,
            collaboratorsSummary = note.collaboratorsSummary
        )
        noteDao.insertNote(entity)
    }

    suspend fun deleteNote(noteId: String) {
        val entity = noteDao.getNoteById(noteId)
        if (entity != null) {
            noteDao.deleteNote(entity)
        }
    }

    suspend fun toggleFavorite(noteId: String) {
        val entity = noteDao.getNoteById(noteId)
        if (entity != null) {
            noteDao.updateNote(entity.copy(isFavorite = !entity.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun togglePin(noteId: String) {
        val entity = noteDao.getNoteById(noteId)
        if (entity != null) {
            noteDao.updateNote(entity.copy(isPinned = !entity.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun createNotebook(title: String, colorHex: String, iconName: String): String {
        val id = UUID.randomUUID().toString()
        val notebook = NotebookEntity(
            id = id,
            title = title,
            colorHex = colorHex,
            iconName = iconName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false,
            collaboratorsJson = "You",
            shareLink = "https://lumina.notes/nb/${title.lowercase().replace(" ", "-")}"
        )
        noteDao.insertNotebook(notebook)

        // Create default "General Notes" section
        val sectionId = UUID.randomUUID().toString()
        val defaultSection = SectionEntity(
            id = sectionId,
            notebookId = id,
            name = "General Notes",
            colorHex = colorHex,
            orderIndex = 0
        )
        noteDao.insertSection(defaultSection)
        return id
    }

    suspend fun createSection(notebookId: String, name: String, colorHex: String): String {
        val id = UUID.randomUUID().toString()
        val section = SectionEntity(
            id = id,
            notebookId = notebookId,
            name = name,
            colorHex = colorHex,
            orderIndex = System.currentTimeMillis().toInt()
        )
        noteDao.insertSection(section)
        return id
    }

    suspend fun triggerCloudSync() {
        _isSyncing.value = true
        delay(1200) // Simulate cloud sync handshake
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _isSyncing.value = false
    }

    private fun NoteEntity.toDomainModel(): NoteItem {
        val words = calculateWordCount(content)
        val readTime = maxOf(1, (words / 200))
        val tagList = if (tagsJson.isBlank()) emptyList() else tagsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val syncEnum = try {
            SyncState.valueOf(syncStatus)
        } catch (e: Exception) {
            SyncState.SYNCED
        }

        return NoteItem(
            id = id,
            sectionId = sectionId,
            notebookId = notebookId,
            title = title,
            content = content,
            tags = tagList,
            isFavorite = isFavorite,
            isPinned = isPinned,
            hasAudioRecording = hasAudioRecording,
            audioDurationSec = audioDurationSec,
            audioTranscript = audioTranscript,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncState = syncEnum,
            lastSyncedAt = lastSyncedAt,
            wordCount = words,
            readingTimeMin = readTime,
            collaboratorsSummary = collaboratorsSummary
        )
    }

    private fun calculateWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).size
    }

    private suspend fun checkAndSeedInitialData() {
        val existingNotebooks = noteDao.getAllNotebooks().first()
        if (existingNotebooks.isNotEmpty()) return

        val nbCsId = UUID.randomUUID().toString()
        val nbBioId = UUID.randomUUID().toString()
        val nbCalcId = UUID.randomUUID().toString()
        val nbPersonalId = UUID.randomUUID().toString()

        val initialNotebooks = listOf(
            NotebookEntity(
                id = nbCsId,
                title = "Computer Science 101",
                colorHex = "#0284C7",
                iconName = "code",
                createdAt = System.currentTimeMillis() - 86400000L * 7,
                updatedAt = System.currentTimeMillis() - 3600000L * 2,
                isPinned = true,
                collaboratorsJson = "You, prof_davis@mit.edu, alex_m",
                shareLink = "https://lumina.notes/nb/cs101-algorithms"
            ),
            NotebookEntity(
                id = nbBioId,
                title = "Genetics & Molecular Bio",
                colorHex = "#059669",
                iconName = "science",
                createdAt = System.currentTimeMillis() - 86400000L * 5,
                updatedAt = System.currentTimeMillis() - 3600000L * 4,
                isPinned = true,
                collaboratorsJson = "You, lab_partner_priya@univ.edu",
                shareLink = "https://lumina.notes/nb/bio-genetics-lab"
            ),
            NotebookEntity(
                id = nbCalcId,
                title = "Calculus III & Linear Algebra",
                colorHex = "#D97706",
                iconName = "calculate",
                createdAt = System.currentTimeMillis() - 86400000L * 10,
                updatedAt = System.currentTimeMillis() - 86400000L * 2,
                isPinned = false,
                collaboratorsJson = "You",
                shareLink = "https://lumina.notes/nb/calc3-linalg"
            ),
            NotebookEntity(
                id = nbPersonalId,
                title = "Personal & Study Routine",
                colorHex = "#7C3AED",
                iconName = "menu_book",
                createdAt = System.currentTimeMillis() - 86400000L * 14,
                updatedAt = System.currentTimeMillis() - 1800000L,
                isPinned = false,
                collaboratorsJson = "You",
                shareLink = "https://lumina.notes/nb/zen-routine"
            )
        )
        noteDao.insertNotebooks(initialNotebooks)

        // Sections
        val secCsLectures = UUID.randomUUID().toString()
        val secCsLabs = UUID.randomUUID().toString()
        val secCsExam = UUID.randomUUID().toString()

        val secBioLectures = UUID.randomUUID().toString()
        val secBioLabs = UUID.randomUUID().toString()

        val secCalcVectors = UUID.randomUUID().toString()
        val secPersonalScratch = UUID.randomUUID().toString()

        val initialSections = listOf(
            SectionEntity(secCsLectures, nbCsId, "Lectures", "#0284C7", 0),
            SectionEntity(secCsLabs, nbCsId, "Lab Work", "#0EA5E9", 1),
            SectionEntity(secCsExam, nbCsId, "Exam Prep", "#38BDF8", 2),

            SectionEntity(secBioLectures, nbBioId, "Lecture Notes", "#059669", 0),
            SectionEntity(secBioLabs, nbBioId, "Lab Protocols", "#10B981", 1),

            SectionEntity(secCalcVectors, nbCalcId, "Eigenvalues & Matrices", "#D97706", 0),
            SectionEntity(secPersonalScratch, nbPersonalId, "Daily Scratchpad", "#7C3AED", 0)
        )
        noteDao.insertSections(initialSections)

        // Initial Notes
        val note1 = NoteEntity(
            id = UUID.randomUUID().toString(),
            sectionId = secCsLectures,
            notebookId = nbCsId,
            title = "Lecture 06: Dynamic Programming & Memoization",
            content = """# Dynamic Programming: Core Principles

Dynamic Programming (DP) solves problems by combining solutions to subproblems, storing intermediate results to avoid redundant recomputations.

## Two Essential Properties:
1. **Optimal Substructure**: An optimal solution to any instance contains optimal solutions to subinstances.
2. **Overlapping Subproblems**: The recursive space contains repeated calls to the same subproblems.

## Top-Down vs Bottom-Up:
- **Memoization (Top-Down)**: Cache recursion results in a hash table or array.
- **Tabulation (Bottom-Up)**: Iteratively compute smaller subproblems first and fill a table.

### Code Pattern: Fibonacci with Tabulation
```kotlin
fun fibonacci(n: Int): Long {
    if (n <= 1) return n.toLong()
    val dp = LongArray(n + 1)
    dp[0] = 0
    dp[1] = 1
    for (i in 2..n) {
        dp[i] = dp[i - 1] + dp[i - 2]
    }
    return dp[n]
}
```

## Action Items for Midterm:
[x] Review 0/1 Knapsack problem recurrence relation
[ ] Implement Longest Common Subsequence (LCS)
[ ] Solve LeetCode #300: Longest Increasing Subsequence
[ ] Review Bellman-Ford shortest paths algorithm

> "Those who cannot remember the past are condemned to repeat it." — Dynamic Programming mantra.
""".trimIndent(),
            tagsJson = "algorithms, cs101, memoization, exam_prep",
            isFavorite = true,
            isPinned = true,
            hasAudioRecording = true,
            audioDurationSec = 45,
            audioTranscript = "Professor Davis explained how tabulation uses less call-stack memory compared to deep recursion memoization.",
            createdAt = System.currentTimeMillis() - 86400000L * 2,
            updatedAt = System.currentTimeMillis() - 3600000L,
            syncStatus = "SYNCED",
            lastSyncedAt = System.currentTimeMillis(),
            collaboratorsSummary = "Alex M., You"
        )

        val note2 = NoteEntity(
            id = UUID.randomUUID().toString(),
            sectionId = secCsExam,
            notebookId = nbCsId,
            title = "Midterm Cheat Sheet: Big-O & Complexity Classes",
            content = """# Complexity Reference Table

## Common Growth Rates:
- **O(1)**: Hash table lookup, stack push/pop
- **O(log N)**: Binary search, balanced BST operations
- **O(N)**: Linear scan, tree traversal
- **O(N log N)**: Merge sort, Heap sort, Quick sort (average)
- **O(N²)**: Nested loops, Selection sort, Insertion sort
- **O(2^N)**: Recursive Fibonacci, exhaustive subsets

### Key Master Theorem Formula:
For recurrences of form: `T(n) = a*T(n/b) + f(n)`
- If `f(n) = O(n^(log_b(a) - ε))`, then `T(n) = Θ(n^log_b(a))`
- If `f(n) = Θ(n^log_b(a))`, then `T(n) = Θ(n^log_b(a) * log n)`

[ ] Practice 5 recurrence proofs
[ ] Memorize graph representation trade-offs (Adjacency Matrix vs List)
""".trimIndent(),
            tagsJson = "cs101, big_o, cheat_sheet, midterm",
            isFavorite = true,
            isPinned = false,
            hasAudioRecording = false,
            audioDurationSec = 0,
            audioTranscript = "",
            createdAt = System.currentTimeMillis() - 86400000L * 4,
            updatedAt = System.currentTimeMillis() - 7200000L,
            syncStatus = "SYNCED",
            lastSyncedAt = System.currentTimeMillis(),
            collaboratorsSummary = "You"
        )

        val note3 = NoteEntity(
            id = UUID.randomUUID().toString(),
            sectionId = secBioLectures,
            notebookId = nbBioId,
            title = "Lecture 12: CRISPR-Cas9 & Double-Strand Breaks",
            content = """# CRISPR-Cas9 Molecular Mechanics

CRISPR (Clustered Regularly Interspaced Short Palindromic Repeats) paired with Cas9 endonuclease allows targeted RNA-guided DNA cleavage.

## Primary Components:
- **sgRNA (single guide RNA)**: 20-nucleotide target-specific sequence matching genomic DNA.
- **PAM site (Protospacer Adjacent Motif)**: 5'-NGG-3' motif adjacent to target; essential for Cas9 recognition.
- **Cas9 Enzyme**: Performs double-strand break (DSB) 3-4 base pairs upstream of the PAM.

## DNA Repair Pathways:
1. **NHEJ (Non-Homologous End Joining)**: Error-prone, causes insertions/deletions (indels) leading to gene knockout.
2. **HDR (Homology-Directed Repair)**: High-fidelity repair when a donor template is introduced, enabling precise gene insertion.

### Lab Protocol Checklist:
[x] Design 3 candidate sgRNAs with off-target scoring < 5%
[ ] Order oligonucleotides for target cloning into pSpCas9 plasmid
[ ] Prepare electrocompetent E. coli cells
""".trimIndent(),
            tagsJson = "genetics, crispr, molecular_bio, lecture",
            isFavorite = true,
            isPinned = true,
            hasAudioRecording = true,
            audioDurationSec = 62,
            audioTranscript = "Remember to verify the PAM site is strictly NGG otherwise Cas9 will not cleave even if guide RNA binds perfectly.",
            createdAt = System.currentTimeMillis() - 86400000L * 3,
            updatedAt = System.currentTimeMillis() - 14400000L,
            syncStatus = "SYNCED",
            lastSyncedAt = System.currentTimeMillis(),
            collaboratorsSummary = "Priya K., You"
        )

        val note4 = NoteEntity(
            id = UUID.randomUUID().toString(),
            sectionId = secPersonalScratch,
            notebookId = nbPersonalId,
            title = "Zen Mode Study Routine & Flow State Principles",
            content = """# Distraction-Free Flow State Strategy

## Daily Study Blocks (Pomodoro 50/10):
- **Block 1 (08:30 - 09:20)**: Hardest algorithmic problem or proof. No phone in room.
- **Block 2 (09:30 - 10:20)**: Lecture review and note synthesis in Lumina Note.
- **Block 3 (10:30 - 11:20)**: Lab implementation & practice coding.

## Writing Environment:
- Use Liquid Glass theme with Cobalt or Emerald Aurora accent.
- Turn on Zen distraction-free mode for full-canvas concentration.
- Record lecture audio snippets for difficult explanations.

> "Simplicity is the prerequisite for reliability." — Edsger W. Dijkstra
""".trimIndent(),
            tagsJson = "productivity, study_habits, zen_mode",
            isFavorite = false,
            isPinned = false,
            hasAudioRecording = false,
            audioDurationSec = 0,
            audioTranscript = "",
            createdAt = System.currentTimeMillis() - 86400000L * 6,
            updatedAt = System.currentTimeMillis() - 1800000L,
            syncStatus = "SYNCED",
            lastSyncedAt = System.currentTimeMillis(),
            collaboratorsSummary = "You"
        )

        noteDao.insertNotes(listOf(note1, note2, note3, note4))
    }
}
