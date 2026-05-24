package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- User Operations ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY xp DESC LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    // --- PDF Operations ---
    @Query("SELECT * FROM pdf_documents ORDER BY timestamp DESC")
    fun getAllPdfs(): Flow<List<PdfDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfDocumentEntity): Long

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getPdfById(id: Int): PdfDocumentEntity?

    @Query("UPDATE pdf_documents SET summaryText = :summary WHERE id = :id")
    suspend fun updatePdfSummary(id: Int, summary: String)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun deletePdf(id: Int)

    // --- Flashcard Operations ---
    @Query("SELECT * FROM flashcards ORDER BY id DESC")
    fun getAllFlashcardsFlow(): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards")
    suspend fun clearAllFlashcards()

    // --- Quiz Operations ---
    @Query("SELECT * FROM quizzes ORDER BY timestamp DESC")
    fun getAllQuizzesFlow(): Flow<List<QuizEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    // --- Study Session / Timer Operations ---
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long
}
