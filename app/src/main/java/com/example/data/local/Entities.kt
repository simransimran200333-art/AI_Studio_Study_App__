package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- 1. User Entity (Offline local backup for testing / mock authentication) ---
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val name: String,
    val passwordHash: String,
    val xp: Int = 0,
    val quizzesPlayed: Int = 0,
    val flashcardsMastered: Int = 0,
    val studyMinutes: Int = 0
)

// --- 2. PDF Document Entity ---
@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val sourceUri: String,
    val contentText: String,
    val summaryText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// --- 3. Saved Flashcard Entity ---
@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val isDifficult: Boolean = false,
    val isMastered: Boolean = false,
    val category: String = "General"
)

// --- 4. Saved Quiz History Entity ---
@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// --- 5. Study Session (Timer) Entity ---
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMinutes: Int,
    val focusCategory: String, // e.g. "Data Structures", "Operating Systems"
    val timestamp: Long = System.currentTimeMillis()
)
