package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FlashcardEntity
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    viewModel: StudyViewModel,
    onNavigateBack: () -> Unit
) {
    val isGenerating by viewModel.isFlashcardsGenerating.collectAsState()
    val rawFlashcards by viewModel.allFlashcardsFlow.collectAsState(initial = emptyList())
    
    // Filters to show either all or only difficult pinned cards
    var filterDifficultyOnly by remember { mutableStateOf(false) }
    
    val flashcards = remember(rawFlashcards, filterDifficultyOnly) {
        if (filterDifficultyOnly) {
            rawFlashcards.filter { it.isDifficult && !it.isMastered }
        } else {
            rawFlashcards.filter { !it.isMastered }
        }
    }
    
    var activeCardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    
    // Reset flipped state whenever deck resets or index moves
    LaunchedEffect(activeCardIndex) {
        isFlipped = false
    }

    // Modal dialog to add custom deck cards manually
    var showAddDialog by remember { mutableStateOf(false) }
    var manualFront by remember { mutableStateOf("") }
    var manualBack by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Recall Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Filter stars
                    IconButton(onClick = { filterDifficultyOnly = !filterDifficultyOnly }) {
                        Icon(
                            imageVector = if (filterDifficultyOnly) Icons.Default.Star else Icons.Default.List,
                            contentDescription = "Filter Stars",
                            tint = if (filterDifficultyOnly) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Card")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isGenerating) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "AI is distilling notes into flashcards...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (flashcards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💡", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (filterDifficultyOnly) "No pinned difficult cards!" else "No active flashcards",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (filterDifficultyOnly) "Great job! You have cleared your starred card definitions." else "Enter some manual cards using the '+' button above or analyze a lecture deck to automatically extract definitions!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                val currentCard = flashcards.getOrNull(activeCardIndex) ?: flashcards.first()
                val safeIndex = if (activeCardIndex >= flashcards.size) 0 else activeCardIndex
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Headline index details
                    Text(
                        text = "Card ${safeIndex + 1} of ${flashcards.size} • ${currentCard.category}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary visual card body with rotation flip effect
                    val rotationState by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                        label = "cardFlip"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("flashcard_body")
                            .clickable { isFlipped = !isFlipped },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isFlipped) "💡 ANSWER KEY" else "❓ QUESTION CARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFlipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isFlipped) currentCard.answer else currentCard.question,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp,
                                color = if (isFlipped) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Tap the card to reveal description",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Flashcard interaction bar: Star / Next / Done
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Toggle Difficulty
                        IconButton(
                            onClick = { viewModel.toggleFlashcardDifficulty(currentCard) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (currentCard.isDifficult) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Starred",
                                tint = if (currentCard.isDifficult) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 2. Clear Card (Mastered!)
                        IconButton(
                            onClick = {
                                viewModel.markFlashcardCompleted(currentCard)
                                if (activeCardIndex >= flashcards.size - 1 && activeCardIndex > 0) {
                                    activeCardIndex -= 1
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Mastered",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // 3. Move Deck index forward
                        IconButton(
                            onClick = {
                                activeCardIndex = (activeCardIndex + 1) % flashcards.size
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Next Card",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Manual dialog popup
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Compile Manual Card", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = manualFront,
                            onValueChange = { manualFront = it },
                            label = { Text("Front Side (Concept / Word)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualBack,
                            onValueChange = { manualBack = it },
                            label = { Text("Back Side (Answer / Detail)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualFront.isNotBlank() && manualBack.isNotBlank()) {
                                viewModel.addManualFlashcard(manualFront.trim(), manualBack.trim(), "Personal")
                                manualFront = ""
                                manualBack = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
