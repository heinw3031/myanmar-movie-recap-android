package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.RecapItem
import com.example.ui.components.RecapCard
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaTextMuted
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.viewmodel.MainUiState

@Composable
fun LibraryScreen(
    uiState: MainUiState,
    onPlayRecap: (RecapItem) -> Unit,
    onToggleSave: (RecapItem) -> Unit,
    onToggleLike: (RecapItem) -> Unit,
    onPublishDraft: (String) -> Unit,
    onDeleteRecap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("သိမ်းထားသည်", "ကြိုက်နှစ်သက်", "မူကြမ်းများ", "ကြည့်ရှုခဲ့သည်")

    val items = when (selectedTab) {
        0 -> uiState.savedRecaps
        1 -> uiState.likedRecaps
        2 -> uiState.draftRecaps
        else -> uiState.historyRecaps
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaObsidian)
    ) {
        Text(
            text = "စာကြည့်တိုက်",
            style = MaterialTheme.typography.titleLarge,
            color = CinemaTextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CinemaSurface,
            contentColor = CinemaTextPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CinemaCrimson
                )
            }
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = label,
                            color = if (selectedTab == index) CinemaTextPrimary else CinemaTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("library_tab_$index")
                )
            }
        }

        if (items.isEmpty()) {
            val (emptyIcon, emptyMsg) = when (selectedTab) {
                0 -> Icons.Filled.BookmarkBorder to "သိမ်းဆည်းထားသော Recap မရှိသေးပါ"
                1 -> Icons.Filled.FavoriteBorder to "ကြိုက်နှစ်သက်ထားသော Recap မရှိသေးပါ"
                2 -> Icons.Filled.Drafts to "မူကြမ်း (Draft) သိမ်းထားသည် မရှိသေးပါ"
                else -> Icons.Filled.History to "ကြည့်ရှုခဲ့သော မှတ်တမ်း မရှိသေးပါ"
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = emptyIcon,
                    contentDescription = null,
                    tint = CinemaTextMuted,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = emptyMsg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CinemaTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items, key = { it.jobId }) { recap ->
                    RecapCard(
                        recap = recap,
                        onPlay = { onPlayRecap(recap) },
                        onToggleSave = { onToggleSave(recap) },
                        onToggleLike = { onToggleLike(recap) },
                        onPublishDraft = if (selectedTab == 2) {
                            { onPublishDraft(recap.jobId) }
                        } else null,
                        onDelete = { onDeleteRecap(recap.jobId) }
                    )
                }
            }
        }
    }
}
