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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.RECAP_CATEGORIES
import com.example.data.model.RecapItem
import com.example.ui.components.RecapCard
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaElevated
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaTextMuted
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.viewmodel.MainUiState

@Composable
fun SearchScreen(
    uiState: MainUiState,
    onQueryChange: (String) -> Unit,
    onPlayRecap: (RecapItem) -> Unit,
    onToggleSave: (RecapItem) -> Unit,
    onToggleLike: (RecapItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val query = uiState.searchQuery.trim()
    val searchableRecaps = uiState.allRecaps.filter { it.isPublished || it.isDraft }
    val results = if (query.isEmpty()) {
        searchableRecaps
    } else {
        searchableRecaps.filter { recap ->
            recap.title.contains(query, ignoreCase = true) ||
                recap.category.contains(query, ignoreCase = true) ||
                recap.scenes.any { it.narrationMy.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaObsidian)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Recap ရှာဖွေရန်",
            style = MaterialTheme.typography.titleLarge,
            color = CinemaTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "ဇာတ်ကားအမည်၊ အမျိုးအစား သို့မဟုတ် ဇာတ်ညွှန်း ရှာပါ...",
                    color = CinemaTextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "ရှာဖွေရန်",
                    tint = CinemaTextSecondary
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.testTag("search_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "ရှင်းလင်းမည်",
                            tint = CinemaTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CinemaElevated,
                unfocusedContainerColor = CinemaElevated,
                focusedBorderColor = CinemaCrimson,
                unfocusedBorderColor = CinemaBorder,
                focusedTextColor = CinemaTextPrimary,
                unfocusedTextColor = CinemaTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(RECAP_CATEGORIES) { category ->
                AssistChip(
                    onClick = { onQueryChange(category) },
                    label = { Text(category) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CinemaElevated,
                        labelColor = CinemaTextPrimary
                    ),
                    border = null,
                    modifier = Modifier.testTag("search_quick_chip_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.VideoLibrary,
                    contentDescription = null,
                    tint = CinemaTextMuted,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (searchableRecaps.isEmpty()) {
                        "ရှာဖွေရန် Recap မရှိသေးပါ — Create မှ ပထမဆုံး recap ထုတ်လုပ်ပါ"
                    } else {
                        "\"$query\" နှင့် ကိုက်ညီသော Recap မတွေ့ပါ"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = CinemaTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(results, key = { it.jobId }) { recap ->
                    RecapCard(
                        recap = recap,
                        onPlay = { onPlayRecap(recap) },
                        onToggleSave = { onToggleSave(recap) },
                        onToggleLike = { onToggleLike(recap) }
                    )
                }
            }
        }
    }
}
