package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FlashcardEntity
import com.example.data.local.PdfDocumentEntity
import com.example.data.local.QuizEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.UserEntity
import com.example.repository.RawFlashcardItem
import com.example.repository.RawQuizItem
import com.example.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    // Global flows representing local reactive DB tables
    val allPdfsFlow: Flow<List<PdfDocumentEntity>>
    val allFlashcardsFlow: Flow<List<FlashcardEntity>>
    val allQuizzesFlow: Flow<List<QuizEntity>>
    val allSessionsFlow: Flow<List<StudySessionEntity>>
    val currentUserFlow: Flow<UserEntity?>

    // --- Authentication states ---
    private val _loginState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val loginState: StateFlow<AuthResult> = _loginState.asStateFlow()

    private val _authenticatedUser = MutableStateFlow<UserEntity?>(null)
    val authenticatedUser: StateFlow<UserEntity?> = _authenticatedUser.asStateFlow()

    // --- Quiz flow states ---
    private val _activeQuizQuestions = MutableStateFlow<List<RawQuizItem>>(emptyList())
    val activeQuizQuestions: StateFlow<List<RawQuizItem>> = _activeQuizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null) // index representing A, B, C, D (0, 1, 2, 3)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    private val _quizTopicName = MutableStateFlow("Placement Quiz")
    val quizTopicName: StateFlow<String> = _quizTopicName.asStateFlow()

    // --- Active Document Content for Summary/Quiz details ---
    private val _selectedPdf = MutableStateFlow<PdfDocumentEntity?>(null)
    val selectedPdf: StateFlow<PdfDocumentEntity?> = _selectedPdf.asStateFlow()

    // --- Loading UI states ---
    private val _isSummaryGenerating = MutableStateFlow(false)
    val isSummaryGenerating: StateFlow<Boolean> = _isSummaryGenerating.asStateFlow()

    private val _isQuizGenerating = MutableStateFlow(false)
    val isQuizGenerating: StateFlow<Boolean> = _isQuizGenerating.asStateFlow()

    private val _isFlashcardsGenerating = MutableStateFlow(false)
    val isFlashcardsGenerating: StateFlow<Boolean> = _isFlashcardsGenerating.asStateFlow()

    // --- Study Timer (Pomodoro) States ---
    private val _timerMinutes = MutableStateFlow(25)
    val timerMinutes: StateFlow<Int> = _timerMinutes.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerMode = MutableStateFlow("Study Focus") // "Study Focus" or "Short Break"
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    private var timerJob: Job? = null
    private var totalSecondsAccumulator = 0

    // --- Theme Settings State ---
    private val _isDarkMode = MutableStateFlow(true) // Dark Mode active by default (gorgeous premium look!)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val studyDao = database.studyDao()
        repository = StudyRepository(studyDao)

        allPdfsFlow = repository.allPdfsFlow
        allFlashcardsFlow = repository.allFlashcardsFlow
        allQuizzesFlow = repository.allQuizzesFlow
        allSessionsFlow = repository.allSessionsFlow
        currentUserFlow = repository.currentUserFlow

        // Auto creation of a dummy mock user on initial launch so dashboard is populated
        viewModelScope.launch {
            val dbUser = studyDao.getUserByEmail("student@example.com")
            if (dbUser == null) {
                val demoUser = UserEntity(
                    email = "student@example.com",
                    name = "Placement Aspirant",
                    passwordHash = "password123",
                    xp = 240,
                    quizzesPlayed = 3,
                    flashcardsMastered = 12,
                    studyMinutes = 45
                )
                studyDao.insertUser(demoUser)
                _authenticatedUser.value = demoUser
            } else {
                _authenticatedUser.value = dbUser
            }
        }
    }

    // --- Theme Control ---
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- Auth Management ---
    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = AuthResult.Loading
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _loginState.value = AuthResult.Error("Columns cannot be blank")
                return@launch
            }
            val success = repository.registerUser(name, email, pass)
            if (success) {
                val registeredUser = repository.loginUser(email, pass)
                _authenticatedUser.value = registeredUser
                _loginState.value = AuthResult.Success(registeredUser!!)
            } else {
                _loginState.value = AuthResult.Error("Email already registered")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = AuthResult.Loading
            val loggedInUser = repository.loginUser(email, pass)
            if (loggedInUser != null) {
                _authenticatedUser.value = loggedInUser
                _loginState.value = AuthResult.Success(loggedInUser)
            } else {
                _loginState.value = AuthResult.Error("Invalid email or password")
            }
        }
    }

    fun logout() {
        _authenticatedUser.value = null
        _loginState.value = AuthResult.Idle
    }

    // --- Local App Statistics Updates ---
    private fun addStudyActivityStats(minutesEarned: Int) {
        viewModelScope.launch {
            val user = _authenticatedUser.value
            if (user != null) {
                val updated = user.copy(
                    xp = user.xp + (minutesEarned * 10),
                    studyMinutes = user.studyMinutes + minutesEarned
                )
                // Save back
                val database = AppDatabase.getDatabase(getApplication())
                database.studyDao().updateUser(updated)
                _authenticatedUser.value = updated
            }
        }
    }

    private fun addQuizPlayedStats(finalScore: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val user = _authenticatedUser.value
            if (user != null) {
                val xpEarned = finalScore * 20 + 20
                val updated = user.copy(
                    xp = user.xp + xpEarned,
                    quizzesPlayed = user.quizzesPlayed + 1
                )
                val database = AppDatabase.getDatabase(getApplication())
                database.studyDao().updateUser(updated)
                _authenticatedUser.value = updated
            }
        }
    }

    fun addFlashcardMasteredStats() {
        viewModelScope.launch {
            val user = _authenticatedUser.value
            if (user != null) {
                val updated = user.copy(
                    xp = user.xp + 15,
                    flashcardsMastered = user.flashcardsMastered + 1
                )
                val database = AppDatabase.getDatabase(getApplication())
                database.studyDao().updateUser(updated)
                _authenticatedUser.value = updated
            }
        }
    }

    // --- PDF Operations ---
    fun selectPdf(pdf: PdfDocumentEntity) {
        _selectedPdf.value = pdf
    }

    fun uploadPdfAndGenerateAI(title: String, notesContext: String) {
        viewModelScope.launch {
            val finalTitle = if (title.isBlank()) "CS Revision notes" else title
            val finalContent = if (notesContext.isBlank()) {
                "Computer Science fundamentals - Operating Systems paging, thread execution, database transactions, ACID properties, QuickSort."
            } else notesContext

            // 1. Save Document locally
            val docId = repository.savePdf(finalTitle, "local_uri", finalContent)
            val newlyCreatedDoc = PdfDocumentEntity(id = docId.toInt(), title = finalTitle, sourceUri = "local_uri", contentText = finalContent)
            _selectedPdf.value = newlyCreatedDoc

            // 2. Automatically kickstart Summary + Quiz + Flashcard background parsing
            triggerSummaryGeneration(newlyCreatedDoc)
        }
    }

    fun triggerSummaryGeneration(pdfDoc: PdfDocumentEntity) {
        viewModelScope.launch {
            _isSummaryGenerating.value = true
            val parsedSummary = repository.generateSummary(pdfDoc.title, pdfDoc.contentText)
            repository.updatePdfSummary(pdfDoc.id, parsedSummary)
            _selectedPdf.value = pdfDoc.copy(summaryText = parsedSummary)
            _isSummaryGenerating.value = false
        }
    }

    fun deletePdfDoc(id: Int) {
        viewModelScope.launch {
             repository.deletePdf(id)
             if (_selectedPdf.value?.id == id) {
                 _selectedPdf.value = null
             }
        }
    }

    // --- Quiz Systems ---
    fun launchQuizFromPdf(pdfDoc: PdfDocumentEntity) {
        viewModelScope.launch {
            _isQuizGenerating.value = true
            _quizTopicName.value = "Quiz: ${pdfDoc.title}"
            val questions = repository.generateQuiz(pdfDoc.title, pdfDoc.contentText)
            _activeQuizQuestions.value = questions
            _currentQuestionIndex.value = 0
            _selectedAnswerIndex.value = null
            _quizScore.value = 0
            _isQuizFinished.value = false
            _isQuizGenerating.value = false
        }
    }

    fun answerCurrentQuestion(optionIndex: Int) {
        if (_selectedAnswerIndex.value != null) return // Already submitted
        _selectedAnswerIndex.value = optionIndex
        val currentQuestion = _activeQuizQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return

        val chosenLetter = when(optionIndex) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            else -> "A"
        }

        if (chosenLetter == currentQuestion.correctLetter) {
            _quizScore.value = _quizScore.value + 1
        }
    }

    fun nextQuizQuestion() {
        _selectedAnswerIndex.value = null
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _activeQuizQuestions.value.size) {
            _currentQuestionIndex.value = nextIndex
        } else {
            // Quiz completed!
            _isQuizFinished.value = true
            viewModelScope.launch {
                repository.saveQuizHistory(_quizTopicName.value, _quizScore.value, _activeQuizQuestions.value.size)
                addQuizPlayedStats(_quizScore.value, _activeQuizQuestions.value.size)
            }
        }
    }

    // --- AI Flashcard Deck generator ---
    fun launchFlashcardDeck(pdfDoc: PdfDocumentEntity) {
        viewModelScope.launch {
            _isFlashcardsGenerating.value = true
            val items = repository.generateFlashcards(pdfDoc.title, pdfDoc.contentText)
            // Empty existing ones first to populate active deck
            repository.allFlashcardsFlow.firstOrNull()?.forEach {
                repository.deleteFlashcard(it)
            }
            items.forEach {
                repository.addFlashcard(it.front, it.back, pdfDoc.title)
            }
            _isFlashcardsGenerating.value = false
        }
    }

    fun markFlashcardCompleted(entity: FlashcardEntity) {
        viewModelScope.launch {
            repository.updateFlashcard(entity.copy(isMastered = true))
            addFlashcardMasteredStats()
        }
    }

    fun toggleFlashcardDifficulty(entity: FlashcardEntity) {
        viewModelScope.launch {
            repository.updateFlashcard(entity.copy(isDifficult = !entity.isDifficult))
        }
    }

    fun addManualFlashcard(front: String, back: String, category: String) {
        viewModelScope.launch {
            repository.addFlashcard(front, back, category)
        }
    }

    fun deleteCard(entity: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(entity)
        }
    }

    // --- Pomodoro Study Ticker Engine ---
    fun setTimerMode(mode: String) {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timerMode.value = mode
        if (mode == "Study Focus") {
            _timerMinutes.value = 25
            _timerSeconds.value = 0
        } else {
            _timerMinutes.value = 5
            _timerSeconds.value = 0
        }
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            _isTimerRunning.value = false
            timerJob?.cancel()
        } else {
            _isTimerRunning.value = true
            timerJob = viewModelScope.launch {
                while (_timerMinutes.value > 0 || _timerSeconds.value > 0) {
                    delay(1000)
                    if (_timerSeconds.value > 0) {
                        _timerSeconds.value = _timerSeconds.value - 1
                    } else {
                        _timerMinutes.value = _timerMinutes.value - 1
                        _timerSeconds.value = 59
                    }
                    totalSecondsAccumulator += 1

                    // Record minute stats periodically
                    if (totalSecondsAccumulator >= 60) {
                        addStudyActivityStats(1)
                        totalSecondsAccumulator = 0
                    }
                }
                // Completed timer ring! Save session record
                if (_timerMode.value == "Study Focus") {
                    repository.saveStudySession(25, "Pomodoro Focus")
                }
                _isTimerRunning.value = false
                // Swap mode automatically
                if (_timerMode.value == "Study Focus") {
                    setTimerMode("Short Break")
                } else {
                    setTimerMode("Study Focus")
                }
            }
        }
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        setTimerMode(_timerMode.value)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

sealed interface AuthResult {
    object Idle : AuthResult
    object Loading : AuthResult
    data class Success(val user: UserEntity) : AuthResult
    data class Error(val errorMessage: String) : AuthResult
}
