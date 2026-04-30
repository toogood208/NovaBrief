package com.example.novabrief.feature.personalization.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val interestCategories = listOf(
    // Categories
    "Top Stories", "Business", "Technology", "World", "Sports", "Science", "Health", "Entertainment",
    // Hobbies & Topics
    "AI", "Startups", "Crypto", "Climate", "Space", "Football", "Formula 1", "Basketball",
    "Cycling", "Hiking", "Photography", "Gaming", "Movies", "Music", "Cooking", "Travel",
    "Fashion", "Stock Market", "Real Estate", "Productivity", "Design", "Programming", "Cybersecurity",
    "Healthcare", "Education", "Politics", "Books", "EVs"
)

@Composable
fun PersonalizationScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    viewModel: PersonalizationViewModel = viewModel()
) {
    val uiState by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050911))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PersonalizationTopBar(onBack = onBack)
        HorizontalDivider(color = Color(0xFF1A2230))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 24.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                PersonalizationHeader()
            }

            item {
                InterestGrid(
                    interests = interestCategories,
                    selectedInterests = uiState.selectedInterests,
                    onToggleInterest = { viewModel.toggleInterest(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        HorizontalDivider(color = Color(0xFF1A2230))
        PersonalizationFooter(
            selectedCount = uiState.selectedInterests.size,
            onSave = {
                viewModel.saveInterests()
                onSave()
            }
        )
    }
}

@Composable
private fun PersonalizationTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Transparent)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFADB8CD),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "×",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFADB8CD),
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(end = 8.dp)
        )
    }
}

@Composable
private fun PersonalizationHeader() {
    Column {
        Text(
            text = "PERSONALIZE",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            ),
            color = Color(0xFFF04152)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Make this newsroom yours",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFFE9EEF9)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pick your interests and we'll tailor the feed. Skip anytime — you can reopen this from the header.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9AA5BB)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestGrid(
    interests: List<String>,
    selectedInterests: Set<String>,
    onToggleInterest: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Categories Section
        Text(
            text = "CATEGORIES",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = Color(0xFF8993A8)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interests.take(8).forEach { interest ->
                InterestButton(
                    text = interest,
                    isSelected = selectedInterests.contains(interest),
                    onClick = { onToggleInterest(interest) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hobbies Section
        Text(
            text = "HOBBIES, WORK & TOPICS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = Color(0xFF8993A8)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interests.drop(8).forEach { interest ->
                InterestButton(
                    text = interest,
                    isSelected = selectedInterests.contains(interest),
                    onClick = { onToggleInterest(interest) }
                )
            }
        }
    }
}

@Composable
private fun InterestButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Color(0xFFF04152) else Color(0xFF1A2230)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isSelected) Color(0xFFFDF5F5) else Color(0xFF9AA5BB)
        )
    }
}

@Composable
private fun PersonalizationFooter(
    selectedCount: Int,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedCount == 0) {
            Text(
                text = "ADD YOUR OWN (E.G. SAAS, YOGA, ANIME)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF9AA5BB),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1A2230))
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1A2230))
                        .clickable { /* Skip for now */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SKIP FOR NOW",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF9AA5BB),
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(if (selectedCount > 0) 0.7f else 1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (selectedCount > 0) Color(0xFFF04152) else Color(0xFF3A4557)
                    )
                    .clickable(enabled = selectedCount > 0, onClick = onSave)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE PREFERENCES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (selectedCount > 0) Color(0xFFFDF5F5) else Color(0xFF6A7585),
                    fontSize = 10.sp
                )
            }
        }
    }
}
