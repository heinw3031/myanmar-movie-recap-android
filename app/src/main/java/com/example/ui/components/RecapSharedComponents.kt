package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.RecapItem
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaCard
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaElevated
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaTextMuted
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import java.util.Locale

fun formatDurationMmSs(seconds: Double): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val mins = total / 60
    val secs = total % 60
    return String.format(Locale.US, "%d:%02d", mins, secs)
}

fun formatFileSizeMb(bytes: Long): String {
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f MB", mb)
}

@Composable
fun RecapCard(
    recap: RecapItem,
    onPlay: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier,
    onPublishDraft: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CinemaBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onPlay)
            .testTag("recap_card_${recap.jobId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF280B10),
                                CinemaElevated,
                                CinemaObsidian
                            )
                        )
                    )
            ) {
                if (!recap.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = recap.thumbnailUrl,
                        contentDescription = recap.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                // Subtle bottom scrim for contrast
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.72f)
                                )
                            )
                        )
                )

                if (recap.thumbnailUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Movie,
                        contentDescription = null,
                        tint = CinemaCrimson.copy(alpha = 0.35f),
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                    )
                }

                // Category badge
                Surface(
                    color = CinemaCrimson,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = recap.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Draft / Published badge
                if (recap.isDraft && !recap.isPublished) {
                    Surface(
                        color = CinemaGold,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "မူကြမ်း",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = formatDurationMmSs(recap.durationSec),
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Center Play circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CinemaCrimson.copy(alpha = 0.92f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "ဖွင့်ကြည့်မည်",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Scene count bottom left
                Text(
                    text = "${recap.scenes.size} ခန်း • ${recap.lengthMinutes} မိနစ် Recap",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaTextSecondary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = recap.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val previewNarration = recap.scenes.firstOrNull()?.narrationMy.orEmpty()
                if (previewNarration.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = previewNarration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleLike,
                            modifier = Modifier.testTag("like_button_${recap.jobId}")
                        ) {
                            Icon(
                                imageVector = if (recap.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "ကြိုက်နှစ်သက်",
                                tint = if (recap.isLiked) CinemaCrimson else CinemaTextSecondary
                            )
                        }
                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier.testTag("save_button_${recap.jobId}")
                        ) {
                            Icon(
                                imageVector = if (recap.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "သိမ်းဆည်း",
                                tint = if (recap.isSaved) CinemaGold else CinemaTextSecondary
                            )
                        }
                        if (onPublishDraft != null && recap.isDraft && !recap.isPublished) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = CinemaEmerald.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clickable(onClick = onPublishDraft)
                                    .testTag("publish_draft_${recap.jobId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Publish,
                                        contentDescription = "တင်မည်",
                                        tint = CinemaEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "တင်မည်",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CinemaEmerald
                                    )
                                }
                            }
                        }
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_recap_${recap.jobId}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "ဖျက်မည်",
                                tint = CinemaTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
