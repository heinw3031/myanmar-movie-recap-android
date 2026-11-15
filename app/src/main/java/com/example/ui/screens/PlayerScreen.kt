package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.RecapItem
import com.example.data.model.RecapScene
import com.example.ui.components.formatDurationMmSs
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaCard
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaElevated
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaError
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import kotlinx.coroutines.delay

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    recap: RecapItem,
    videoUrl: String,
    selectedQuality: String,
    onSelectQuality: (String) -> Unit,
    onToggleSave: () -> Unit,
    onToggleLike: () -> Unit,
    onPublishDraft: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    var hasAudioTrack by remember(recap.jobId, selectedQuality) { mutableStateOf(true) }
    var tracksChecked by remember(recap.jobId, selectedQuality) { mutableStateOf(false) }
    var playerError by remember(recap.jobId, selectedQuality) { mutableStateOf<String?>(null) }
    var currentPosMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf((recap.durationSec * 1000).toLong().coerceAtLeast(1000L)) }
    var activeSceneIdx by remember { mutableIntStateOf(0) }

    val exoPlayer = remember(recap.jobId) {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val audioPresent = tracks.groups.any { group ->
                    group.type == C.TRACK_TYPE_AUDIO && group.length > 0 && group.isSupported
                }
                if (tracks.groups.isNotEmpty()) {
                    tracksChecked = true
                    hasAudioTrack = audioPresent
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val dur = exoPlayer.duration
                    if (dur > 0) totalDurationMs = dur
                    val audioPresent = exoPlayer.currentTracks.groups.any { group ->
                        group.type == C.TRACK_TYPE_AUDIO && group.length > 0 && group.isSupported
                    }
                    tracksChecked = true
                    hasAudioTrack = audioPresent
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playerError = "ဗီဒီယို ဖွင့်၍မရပါ: ${error.localizedMessage ?: error.errorCodeName}"
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoUrl) {
        val resumePosition = exoPlayer.currentPosition.coerceAtLeast(0L)
        tracksChecked = false
        hasAudioTrack = true
        playerError = null
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
        exoPlayer.prepare()
        if (resumePosition > 0L) {
            exoPlayer.seekTo(resumePosition)
        }
        exoPlayer.playWhenReady = true
    }

    LaunchedEffect(exoPlayer, recap.scenes) {
        while (true) {
            currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            val dur = exoPlayer.duration
            if (dur > 0) totalDurationMs = dur
            if (recap.scenes.isNotEmpty()) {
                activeSceneIdx = resolveActiveSceneIndex(
                    scenes = recap.scenes,
                    positionSec = currentPosMs / 1000.0,
                    totalDurationSec = totalDurationMs / 1000.0
                )
            }
            delay(350L)
        }
    }

    Scaffold(
        containerColor = CinemaObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = recap.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CinemaTextPrimary,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "နောက်သို့",
                            tint = CinemaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CinemaSurface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                            }
                        },
                        update = { view ->
                            view.player = exoPlayer
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("exoplayer_view")
                    )
                }
            }

            if (tracksChecked && !hasAudioTrack) {
                item {
                    Surface(
                        color = CinemaError.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(1.dp, CinemaError, RoundedCornerShape(12.dp))
                            .testTag("missing_audio_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeOff,
                                contentDescription = null,
                                tint = CinemaError
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "အသံမပါသေးပါ",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaError
                            )
                        }
                    }
                }
            }

            if (!playerError.isNullOrBlank()) {
                item {
                    Surface(
                        color = CinemaError.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = playerError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaError,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Synchronised Burmese subtitle box
            if (recap.scenes.isNotEmpty()) {
                val activeScene = recap.scenes.getOrNull(activeSceneIdx)
                if (activeScene != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .border(1.dp, CinemaGold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .testTag("live_burmese_caption_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CinemaCard)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "တိုက်ရိုက် မြန်မာစာတန်း • အခန်း ${activeScene.sceneNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CinemaGold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = activeScene.narrationMy,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CinemaTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Quality switch + Like / Save / Publish actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("720", "480").forEach { quality ->
                            val selected = selectedQuality == quality
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectQuality(quality) },
                                label = { Text("${quality}p") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaCrimson,
                                    selectedLabelColor = Color.White,
                                    containerColor = CinemaElevated,
                                    labelColor = CinemaTextSecondary
                                ),
                                border = null,
                                modifier = Modifier.testTag("player_quality_$quality")
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleLike,
                            modifier = Modifier.testTag("player_like_button")
                        ) {
                            Icon(
                                imageVector = if (recap.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "ကြိုက်နှစ်သက်",
                                tint = if (recap.isLiked) CinemaCrimson else CinemaTextSecondary
                            )
                        }
                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier.testTag("player_save_button")
                        ) {
                            Icon(
                                imageVector = if (recap.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "သိမ်းဆည်း",
                                tint = if (recap.isSaved) CinemaGold else CinemaTextSecondary
                            )
                        }
                    }
                }
            }

            if (recap.isDraft && !recap.isPublished) {
                item {
                    Button(
                        onClick = onPublishDraft,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp)
                            .testTag("player_publish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Publish,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Home နှင့် Profile သို့ တင်မည်", color = Color.Black)
                    }
                }
            }

            if (recap.scenes.isNotEmpty()) {
                item {
                    Text(
                        text = "အခန်းလိုက် ဇာတ်ညွှန်းများ (${recap.scenes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = CinemaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                itemsIndexed(recap.scenes, key = { _, s -> s.sceneNumber }) { index, scene ->
                    val isCurrent = index == activeSceneIdx
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) CinemaCrimson else CinemaBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                val targetMs = resolveSceneStartMs(
                                    scenes = recap.scenes,
                                    index = index,
                                    totalDurationMs = totalDurationMs
                                )
                                exoPlayer.seekTo(targetMs)
                            }
                            .testTag("player_scene_item_${scene.sceneNumber}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) CinemaElevated else CinemaCard
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "အခန်း ${scene.sceneNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isCurrent) CinemaCrimson else CinemaGold
                                )
                                Text(
                                    text = "Source: ${formatDurationMmSs(scene.sourceStartSec.toDouble())} - ${formatDurationMmSs(scene.sourceEndSec.toDouble())}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = scene.narrationMy,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun resolveActiveSceneIndex(
    scenes: List<RecapScene>,
    positionSec: Double,
    totalDurationSec: Double
): Int {
    if (scenes.isEmpty()) return 0
    val explicitIdx = scenes.indexOfFirst { scene ->
        val start = scene.recapStartSec
        val end = scene.recapEndSec
        start != null && end != null && positionSec >= start && positionSec <= end
    }
    if (explicitIdx >= 0) return explicitIdx

    val safeDuration = totalDurationSec.coerceAtLeast(1.0)
    val fraction = (positionSec / safeDuration).coerceIn(0.0, 0.9999)
    return (fraction * scenes.size).toInt().coerceIn(0, scenes.lastIndex)
}

private fun resolveSceneStartMs(
    scenes: List<RecapScene>,
    index: Int,
    totalDurationMs: Long
): Long {
    val scene = scenes.getOrNull(index) ?: return 0L
    val explicitStart = scene.recapStartSec
    if (explicitStart != null) {
        return (explicitStart * 1000.0).toLong().coerceAtLeast(0L)
    }
    if (scenes.isEmpty()) return 0L
    return ((index.toDouble() / scenes.size.toDouble()) * totalDurationMs).toLong().coerceAtLeast(0L)
}
