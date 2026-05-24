package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StudyViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard", // Auto login with default dummy session for quick-start testing
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. Login Screen
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }

                        // 2. Registration Screen
                        composable("register") {
                            RegisterScreen(
                                viewModel = viewModel,
                                onRegisterSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                }
                            )
                        }

                        // 3. Central Dashboard Layout
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToPdfUpload = { navController.navigate("pdf_upload") },
                                onNavigateToSummary = { navController.navigate("summary") },
                                onNavigateToQuiz = { navController.navigate("quiz") },
                                onNavigateToFlashcards = { navController.navigate("flashcards") },
                                onNavigateToTimer = { navController.navigate("timer") },
                                onNavigateToProgress = { navController.navigate("progress") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. Notes Board / PDF simulation picker
                        composable("pdf_upload") {
                            PdfUploadScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onAnalyzeSuccess = {
                                    navController.navigate("summary") {
                                        popUpTo("pdf_upload") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 5. Notes AI summary explorer
                        composable("summary") {
                            SummaryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.navigate("dashboard") {
                                    popUpTo("summary") { inclusive = true }
                                } },
                                onNavigateToQuiz = { navController.navigate("quiz") },
                                onNavigateToFlashcards = { navController.navigate("flashcards") }
                            )
                        }

                        // 6. MCQ Practice arena
                        composable("quiz") {
                            QuizScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Active Recall Starred deck
                        composable("flashcards") {
                            FlashcardScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 8. Focus countdown Pomodoro ticking clock
                        composable("timer") {
                            TimerScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 9. Charts & history analytics Tracker
                        composable("progress") {
                            ProgressScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 10. Prefs (Dark Mode shift) Screen
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
