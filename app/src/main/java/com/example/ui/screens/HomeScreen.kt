package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.RECAP_CATEGORIES
import com.example.data.model.RecapItem
import com.example.ui.components.RecapCard
import com.example.ui.components.formatDurationMmSs
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaCard
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaElevated
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaError
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.viewmodel.MainUiState

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onSelectCategory: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenCreate: () -> Unit,
    onPlayRecap: (RecapItem) -> Unit,
    onToggleSave: (RecapItem) -> Unit,
    onToggleLike: (RecapItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("အားလုံး") + RECAP_CATEGORIES
    val recaps = uiState.filteredHomeRecaps

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaObsidian),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "မြန်မာ ရုပ်ရှင် Recap",
                        style = MaterialTheme.typography.titleLarge,
                        color = CinemaTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.isBackendConfigured) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = if (uiState.isBackendConfigured) CinemaEmerald else CinemaError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isBackendConfigured) "Backend ချိတ်ဆက်ပြီး" else "Backend မချိတ်ရသေးပါ",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isBackendConfigured) CinemaEmerald else CinemaError
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = uiState.isBackendConfigured && !uiState.isRefreshingFeed,
                    modifier = Modifier.testTag("home_refresh_button")
                ) {
                    if (uiState.isRefreshingFeed) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CinemaCrimson,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "ပြန်လည်စစ်ဆေးမည်",
                            tint = CinemaTextPrimary
                        )
                    }
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val selected = uiState.selectedHomeCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaCrimson,
                            selectedLabelColor = Color.White,
                            containerColor = CinemaElevated,
                            labelColor = CinemaTextSecondary
                        ),
                        border = null,
                        modifier = Modifier.testTag("home_category_chip_$category")
                    )
                }
            }
        }

        if (recaps.isEmpty()) {
            item {
                HomeEmptyStateCard(
                    hasAnyPublishedInOtherCategory = uiState.publishedRecaps.isNotEmpty(),
                    selectedCategory = uiState.selectedHomeCategory,
                    onOpenCreate = onOpenCreate,
                    onResetCategory = { onSelectCategory("အားလုံး") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            val hero = recaps.first()
            item {
                FeaturedRecapHero(
                    recap = hero,
                    onPlay = { onPlayRecap(hero) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (recaps.size > 1) {
                item {
                    Text(
                        text = "ထုတ်လုပ်ပြီးသား Recap များ (${recaps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = CinemaTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(recaps.drop(1), key = { it.jobId }) { recap ->
                    RecapCard(
                        recap = recap,
                        onPlay = { onPlayRecap(recap) },
                        onToggleSave = { onToggleSave(recap) },
                        onToggleLike = { onToggleLike(recap) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedRecapHero(
    recap: RecapItem,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CinemaCrimson.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .testTag("home_featured_hero"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_cinema_empty_banner),
                contentDescription = recap.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                CinemaObsidian.copy(alpha = 0.94f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = CinemaGold,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "နောက်ဆုံးထွက် Recap",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = CinemaElevated.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${recap.category} • ${formatDurationMmSs(recap.durationSec)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = recap.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CinemaTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("hero_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ယခုကြည့်ရှုမည်")
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyStateCard(
    hasAnyPublishedInOtherCategory: Boolean,
    selectedCategory: String,
    onOpenCreate: () -> Unit,
    onResetCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CinemaBorder, RoundedCornerShape(22.dp))
            .testTag("home_empty_state_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaCard)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_cinema_empty_banner),
                    contentDescription = "Myanmar Movie Recap Cinema Stage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CinemaCard
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CinemaCrimson.copy(alpha = 0.9f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MovieFilter,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (hasAnyPublishedInOtherCategory) {
                        "$selectedCategory အမျိုးအစားတွင် Recap မရှိသေးပါ"
                    } else {
                        "Recap မရှိသေးပါ — Create မှာ video တင်ပြီး ပထမဆုံး recap ထုတ်ကြည့်ပါ"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "နမူနာ ကာတွန်းဗီဒီယိုအတုများ မပါဝင်ပါ — ကိုယ်တိုင် Backend မှ ထုတ်လုပ်ထားသော ရုပ်ရှင် Recap အစစ်များသာ ပြသပါမည်။",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinemaTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                if (hasAnyPublishedInOtherCategory) {
                    Button(
                        onClick = onResetCategory,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaElevated),
                        modifier = Modifier.testTag("home_reset_category_button")
                    ) {
                        Text("အားလုံးပြသမည်", color = CinemaTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = onOpenCreate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("home_go_to_create_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create မှာ Recap ဖန်တီးမည်")
                }
            }
        }
    }
}
