package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // Notebooks
    @Query("SELECT * FROM notebooks ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE id = :notebookId LIMIT 1")
    suspend fun getNotebookById(notebookId: String): NotebookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(notebook: NotebookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebooks(notebooks: List<NotebookEntity>)

    @Update
    suspend fun updateNotebook(notebook: NotebookEntity)

    @Delete
    suspend fun deleteNotebook(notebook: NotebookEntity)

    // Sections
    @Query("SELECT * FROM sections WHERE notebookId = :notebookId ORDER BY orderIndex ASC")
    fun getSectionsForNotebook(notebookId: String): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections ORDER BY orderIndex ASC")
    fun getAllSections(): Flow<List<SectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Update
    suspend fun updateSection(section: SectionEntity)

    @Delete
    suspend fun deleteSection(section: SectionEntity)

    // Notes
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE sectionId = :sectionId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesForSection(sectionId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE notebookId = :notebookId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesForNotebook(notebookId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tagsJson LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT COUNT(*) FROM notes WHERE notebookId = :notebookId")
    suspend fun getNoteCountForNotebook(notebookId: String): Int

    @Query("SELECT COUNT(*) FROM notes WHERE sectionId = :sectionId")
    suspend fun getNoteCountForSection(sectionId: String): Int

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getTotalNotesCount(): Int
}
