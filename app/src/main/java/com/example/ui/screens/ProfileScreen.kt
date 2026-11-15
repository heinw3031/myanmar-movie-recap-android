package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RecapItem
import com.example.ui.components.RecapCard
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
fun ProfileScreen(
    uiState: MainUiState,
    onSaveBackendUrl: (String) -> Unit,
    onClearBackendUrl: () -> Unit,
    onResetBackendUrlToDefault: () -> Unit,
    onSaveCreatorName: (String) -> Unit,
    onSaveDefaultQuality: (String) -> Unit,
    onToggleCompressBeforeUpload: (Boolean) -> Unit,
    onPlayRecap: (RecapItem) -> Unit,
    onToggleSave: (RecapItem) -> Unit,
    onToggleLike: (RecapItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf(uiState.settings.backendUrl) }
    var nameInput by remember { mutableStateOf(uiState.settings.creatorName) }
    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.settings.backendUrl) {
        urlInput = uiState.settings.backendUrl
    }
    LaunchedEffect(uiState.settings.creatorName) {
        nameInput = uiState.settings.creatorName
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaObsidian),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Text(
                    text = "ပရိုဖိုင်နှင့် ဆက်တင်",
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CinemaBorder, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(CinemaCrimson.copy(alpha = 0.2f))
                                    .border(1.5.dp, CinemaCrimson, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = CinemaCrimson,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.settings.creatorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CinemaTextPrimary
                                )
                                Text(
                                    text = "တင်ပြီး ${uiState.publishedRecaps.size} • မူကြမ်း ${uiState.draftRecaps.size} • သိမ်းထား ${uiState.savedRecaps.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CinemaTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("ဖန်တီးသူ အမည်") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CinemaCrimson,
                                    unfocusedBorderColor = CinemaBorder,
                                    focusedTextColor = CinemaTextPrimary,
                                    unfocusedTextColor = CinemaTextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_name_input")
                            )
                            Button(
                                onClick = {
                                    onSaveCreatorName(nameInput)
                                    feedbackMsg = "အမည် သိမ်းဆည်းပြီးပါပြီ"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaElevated),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("profile_name_save_button")
                            ) {
                                Text("သိမ်းမည်", color = CinemaTextPrimary)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .border(
                        1.dp,
                        if (uiState.isBackendConfigured) CinemaEmerald.copy(alpha = 0.4f) else CinemaError.copy(alpha = 0.5f),
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.isBackendConfigured) Icons.Filled.CheckCircle else Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = if (uiState.isBackendConfigured) CinemaEmerald else CinemaError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backend Server URL (BASE_URL)",
                            style = MaterialTheme.typography.titleMedium,
                            color = CinemaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (uiState.isBackendConfigured) {
                            "လက်ရှိ ချိတ်ဆက်ထားသည်: ${uiState.settings.backendUrl}"
                        } else {
                            "Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.isBackendConfigured) CinemaEmerald else CinemaError
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Backend BASE_URL") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaCrimson,
                            unfocusedBorderColor = CinemaBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backend_url_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveBackendUrl(urlInput)
                                feedbackMsg = if (urlInput.isBlank()) {
                                    "Backend URL ကို ရှင်းလင်းပြီးပါပြီ"
                                } else {
                                    "Backend URL သိမ်းဆည်းပြီးပါပြီ"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaCrimson),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_backend_url_button")
                        ) {
                            Text("သိမ်းဆည်းမည်", color = Color.White)
                        }
                        OutlinedButton(
                            onClick = {
                                urlInput = ""
                                onClearBackendUrl()
                                feedbackMsg = "Backend URL ဖယ်ရှားပြီးပါပြီ"
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("clear_backend_url_button")
                        ) {
                            Text("ရှင်းလင်းမည်", color = CinemaError)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onResetBackendUrlToDefault()
                            feedbackMsg = "Railway Backend URL ပြန်လည်ချိတ်ဆက်ပြီးပါပြီ"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_backend_url_button")
                    ) {
                        Text("Railway Backend URL ပြန်သုံးမည်", color = CinemaGold)
                    }

                    if (!feedbackMsg.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = feedbackMsg.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGold
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .border(1.dp, CinemaBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = CinemaGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ဗီဒီယို ကြည့်ရှုမှုနှင့် Upload ဆက်တင်",
                            style = MaterialTheme.typography.titleMedium,
                            color = CinemaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "မူလ Streaming အရည်အသွေး",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("720", "480").forEach { q ->
                            val selected = uiState.settings.defaultQuality == q
                            FilterChip(
                                selected = selected,
                                onClick = { onSaveDefaultQuality(q) },
                                label = { Text("${q}p") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaCrimson,
                                    selectedLabelColor = Color.White,
                                    containerColor = CinemaElevated,
                                    labelColor = CinemaTextSecondary
                                ),
                                border = null,
                                modifier = Modifier.testTag("default_quality_chip_$q")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Upload မတိုင်မီ ဗီဒီယို ချုံ့မည်",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CinemaTextPrimary
                            )
                            Text(
                                text = "15 MB ထက်ကြီးသော ဗီဒီယိုများကို 720p H.264 သို့ ချုံ့ပေးပါမည်",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaTextSecondary
                            )
                        }
                        Switch(
                            checked = uiState.settings.compressBeforeUpload,
                            onCheckedChange = onToggleCompressBeforeUpload,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CinemaCrimson
                            ),
                            modifier = Modifier.testTag("compress_upload_switch")
                        )
                    }
                }
            }
        }

        if (uiState.publishedRecaps.isNotEmpty()) {
            item {
                Text(
                    text = "တင်ပြီးသား Recap များ (${uiState.publishedRecaps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaTextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                )
            }
            items(uiState.publishedRecaps, key = { it.jobId }) { recap ->
                RecapCard(
                    recap = recap,
                    onPlay = { onPlayRecap(recap) },
                    onToggleSave = { onToggleSave(recap) },
                    onToggleLike = { onToggleLike(recap) },
                    modifier = Modifier.widthIn(max = 680.dp)
                )
            }
        }
    }
}
