package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PdfDocumentEntity
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    onNavigateToPdfUpload: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToServiceStats: () -> Unit = {}, // Fallback or direct progress
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUserFlow.collectAsState(initial = null)
    val pdfsList by viewModel.allPdfsFlow.collectAsState(initial = emptyList())

    // Translate user minutes into beautiful statistics
    val totalMinutes = currentUser?.studyMinutes ?: 0
    val studyHours = totalMinutes / 60f
    val goalHours = 4.0f
    val progressFraction = (studyHours / goalHours).coerceIn(0f, 1f)
    val progressPercent = (progressFraction * 100).toInt()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPdfUpload,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("fab_add_notes")
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Notes")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Notes", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar / App Header (Material Design 3 Clean Minimalism Style)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "GOOD DAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser?.name ?: "Placement Aspirant",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.testTag("dashboard_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.logout()
                                onLogout()
                            },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Circular initials badge
                        val nameStr = currentUser?.name ?: "Learner"
                        val initials = if (nameStr.contains(" ")) {
                            val parts = nameStr.split(" ")
                            "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
                        } else {
                            nameStr.take(2).uppercase()
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 2. Daily Progress Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Daily Goal",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$progressPercent% of your path completed",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                            // Active status pill
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Custom elegant sleek progress track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress statistics bottom row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format("%.1fh focused", studyHours),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "4h goal",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 3. Quick Tools Grid (2x2 Column structure layout mimicking exact Design HTML grid parameters)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // AI Summary Card
                        QuickToolGridItem(
                            modifier = Modifier.weight(1f),
                            title = "AI Summary",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            icon = Icons.Default.Menu,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onNavigateToSummary
                        )

                        // Smart Quiz Card
                        QuickToolGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Smart Quiz",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            icon = Icons.Default.Create,
                            iconColor = MaterialTheme.colorScheme.secondary,
                            onClick = onNavigateToQuiz
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Flashcards Card
                        QuickToolGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Flashcards",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            icon = Icons.Default.Star,
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateToFlashcards
                        )

                        // Focus Timer Card
                        QuickToolGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Focus Timer",
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            icon = Icons.Default.PlayArrow,
                            iconColor = MaterialTheme.colorScheme.error,
                            onClick = onNavigateToTimer
                        )
                    }
                }
            }

            // 4. Recent Notes List Title Header & Navigation Link
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT NOTES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "VIEW STREAKS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNavigateToProgress() }
                            .padding(4.dp)
                    )
                }
            }

            // 5. Recent Notes list
            if (pdfsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("📁", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No materials loaded yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap 'Add Notes' below to type concepts or load standard PDF notes, and immediately unlock summary and exam cards!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(pdfsList) { pdf ->
                    RecentNoteItemRow(
                        doc = pdf,
                        onSelectPdf = {
                            viewModel.selectPdf(pdf)
                            onNavigateToSummary()
                        },
                        onDeletePdf = {
                            viewModel.deletePdfDoc(pdf.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
fun QuickToolGridItem(
    modifier: Modifier = Modifier,
    title: String,
    containerColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(16.dp)
            .heightIn(min = 100.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Icon in white small card with shadow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp))
                    .background(Color.White, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun RecentNoteItemRow(
    doc: PdfDocumentEntity,
    onSelectPdf: () -> Unit,
    onDeletePdf: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectPdf() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PDF circular container
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.background, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PDF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = doc.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Summarized • Quiz ready",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(
                onClick = onDeletePdf,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
