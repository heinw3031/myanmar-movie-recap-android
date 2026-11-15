package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.RECAP_CATEGORIES
import com.example.data.model.RECAP_LENGTH_OPTIONS
import com.example.data.model.RecapItem
import com.example.data.model.RecapScene
import com.example.ui.components.formatDurationMmSs
import com.example.ui.components.formatFileSizeMb
import com.example.ui.theme.CinemaAmber
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaCard
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaElevated
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaError
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaTextMuted
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.viewmodel.CreateStage
import com.example.ui.viewmodel.MainUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(
    uiState: MainUiState,
    onVideoPicked: (android.net.Uri) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSelectLength: (Int) -> Unit,
    onUploadVideo: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateSceneNarration: (Int, String) -> Unit,
    onUpdateSceneTimestamps: (Int, Int, Int) -> Unit,
    onApproveScript: () -> Unit,
    onGenerateRecap: () -> Unit,
    onPreviewRecap: (RecapItem) -> Unit,
    onPublishRecap: (String) -> Unit,
    onSaveDraft: (String) -> Unit,
    onResetFlow: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onVideoPicked(uri)
        }
    }

    val createState = uiState.createState
    val stage = createState.stage
    val isBackendReady = uiState.isBackendConfigured

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaObsidian),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Text(
                    text = "Recap အသစ် ဖန်တီးရန်",
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Honest free-tier note
                Surface(
                    color = CinemaGold.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CinemaGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Free test version — တစ်နေ့ recap အနည်းငယ်သာ ထုတ်နိုင်သည်",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaGold
                        )
                    }
                }
            }
        }

        if (!isBackendReady) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .border(1.5.dp, CinemaError, RoundedCornerShape(16.dp))
                        .testTag("backend_missing_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaError.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = CinemaError
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaError
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("create_open_settings_button")
                        ) {
                            Text("Profile ဆက်တင်တွင် BASE_URL ထည့်ရန်", color = CinemaTextPrimary)
                        }
                    }
                }
            }
        }

        when (stage) {
            is CreateStage.Idle -> {
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
                            Surface(
                                color = CinemaAmber.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = CinemaAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ရုပ်ရှင်အရှည် ၂ နာရီခန့်သည် 1–2 GB ရှိတတ်ပြီး upload တင်ရန် ကြာနိုင်ပါသည်။ Free test version အတွက် ၂–၃ မိနစ်ခန့် ဗီဒီယိုတိုကို ဦးစားပေး ရွေးချယ်ပါ။",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CinemaTextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("pick_video_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VideoFile,
                                    contentDescription = null,
                                    tint = CinemaCrimson
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (createState.pickedVideo == null) {
                                        "ဖုန်းထဲမှ ဗီဒီယို ရွေးချယ်မည်"
                                    } else {
                                        "အခြား ဗီဒီယို ပြောင်းရွေးမည်"
                                    },
                                    color = CinemaTextPrimary
                                )
                            }

                            if (createState.pickedVideo != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = CinemaElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("picked_video_info")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = createState.pickedVideo.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = CinemaTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ဖိုင်အရွယ်အစား: ${formatFileSizeMb(createState.pickedVideo.sizeBytes)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CinemaGold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Recap အရှည် (မိနစ်)",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RECAP_LENGTH_OPTIONS.forEach { mins ->
                                    val selected = createState.selectedLengthMinutes == mins
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectLength(mins) },
                                        label = { Text("$mins မိနစ်") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CinemaCrimson,
                                            selectedLabelColor = Color.White,
                                            containerColor = CinemaElevated,
                                            labelColor = CinemaTextSecondary
                                        ),
                                        border = null,
                                        modifier = Modifier.testTag("length_chip_$mins")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "ရုပ်ရှင် အမျိုးအစား",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RECAP_CATEGORIES.forEach { cat ->
                                    val selected = createState.selectedCategory == cat
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectCategory(cat) },
                                        label = { Text(cat) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CinemaCrimson,
                                            selectedLabelColor = Color.White,
                                            containerColor = CinemaElevated,
                                            labelColor = CinemaTextSecondary
                                        ),
                                        border = null,
                                        modifier = Modifier.testTag("create_category_chip_$cat")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onUploadVideo,
                                enabled = isBackendReady && createState.pickedVideo != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CinemaCrimson,
                                    contentColor = Color.White,
                                    disabledContainerColor = CinemaElevated,
                                    disabledContentColor = CinemaTextMuted
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("upload_video_button")
                            ) {
                                Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ဗီဒီယို တင်ပြီး ဇာတ်ညွှန်းထုတ်မည်")
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = CinemaElevated,
                                    disabledContentColor = CinemaTextMuted
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("generate_recap_button")
                            ) {
                                Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recap ထုတ်ပေး (ဇာတ်ညွှန်းအတည်ပြုပြီးမှ)")
                            }
                        }
                    }
                }
            }

            is CreateStage.Compressing -> {
                item {
                    PipelineProgressCard(
                        title = "Upload မတိုင်မီ ဗီဒီယို ချုံ့နေပါသည်...",
                        statusLabel = "On-device compression (720p)",
                        progressPercent = stage.progressPercent,
                        subtitle = "ဖိုင်အရွယ်အစား ကြီးပါက upload မြန်စေရန် ချုံ့နေပါသည်။",
                        onCancel = onResetFlow
                    )
                }
            }

            is CreateStage.Uploading -> {
                item {
                    PipelineProgressCard(
                        title = "Backend သို့ ဗီဒီယို Upload တင်နေပါသည်...",
                        statusLabel = "${formatFileSizeMb(stage.uploadedBytes)} / ${formatFileSizeMb(stage.totalBytes)}",
                        progressPercent = stage.progressPercent,
                        subtitle = "Upload ပြီးဆုံးသည်အထိ စောင့်ဆိုင်းပေးပါ။",
                        onCancel = onResetFlow
                    )
                }
            }

            is CreateStage.PollingScript -> {
                item {
                    val burmesePhase = when (stage.serverStatus.lowercase()) {
                        "queued" -> "တန်းစီစောင့်ဆိုင်းနေသည် (queued)"
                        "analysing", "analyzing" -> "Gemini မှ ဗီဒီယို Frame များကို ကြည့်ရှုစစ်ဆေးနေသည် (analysing)"
                        "scripting" -> "မြန်မာ ဇာတ်ညွှန်း ရေးသားနေသည် (scripting)"
                        else -> "Server အခြေအနေ: ${stage.serverStatus}"
                    }
                    PipelineProgressCard(
                        title = "ဇာတ်ညွှန်း ထုတ်လုပ်နေပါသည်",
                        statusLabel = "$burmesePhase • ${formatDurationMmSs(stage.elapsedSeconds.toDouble())}",
                        progressPercent = stage.serverProgress,
                        subtitle = "Job ID: ${stage.jobId} • ၅ စက္ကန့်တိုင်း Server မှ အခြေအနေ စစ်ဆေးနေပါသည်။",
                        onCancel = onResetFlow
                    )
                }
            }

            is CreateStage.AwaitingApproval -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .border(1.dp, CinemaGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .testTag("script_approval_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ဇာတ်ညွှန်း စစ်ဆေးပြင်ဆင်ရန် (${stage.scenes.size} ခန်း)",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "မြန်မာစာသားနှင့် အချိန်ပိုင်းများကို လိုအပ်သလို ပြင်ဆင်ပြီး အတည်ပြုပါ။",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = stage.title,
                                onValueChange = onUpdateTitle,
                                label = { Text("Recap ခေါင်းစဉ်") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CinemaCrimson,
                                    unfocusedBorderColor = CinemaBorder,
                                    focusedTextColor = CinemaTextPrimary,
                                    unfocusedTextColor = CinemaTextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("approval_title_input")
                            )
                        }
                    }
                }

                items(stage.scenes, key = { it.sceneNumber }) { scene ->
                    EditableSceneCard(
                        scene = scene,
                        onUpdateNarration = { onUpdateSceneNarration(scene.sceneNumber, it) },
                        onUpdateTimestamps = { s, e -> onUpdateSceneTimestamps(scene.sceneNumber, s, e) },
                        modifier = Modifier.widthIn(max = 680.dp)
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!stage.approvalError.isNullOrBlank()) {
                            Surface(
                                color = CinemaError.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stage.approvalError,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CinemaError,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (stage.isApproved) {
                            Surface(
                                color = CinemaEmerald.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = CinemaEmerald
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ဇာတ်ညွှန်း အတည်ပြုပြီးပါပြီ — ယခု Recap ထုတ်ပေး ခလုတ်ကို နှိပ်နိုင်ပါပြီ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CinemaEmerald
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onApproveScript,
                            enabled = isBackendReady && !stage.isSubmittingApproval,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaElevated,
                                contentColor = CinemaTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("approve_script_button")
                        ) {
                            if (stage.isSubmittingApproval) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = CinemaGold,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("ဇာတ်ညွှန်း အတည်ပြုမည်")
                        }

                        Button(
                            onClick = onGenerateRecap,
                            enabled = isBackendReady && stage.isApproved,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaCrimson,
                                contentColor = Color.White,
                                disabledContainerColor = CinemaElevated,
                                disabledContentColor = CinemaTextMuted
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("generate_recap_button")
                        ) {
                            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recap ထုတ်ပေး")
                        }

                        OutlinedButton(
                            onClick = onResetFlow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ပယ်ဖျက်ပြီး အသစ်စမည်", color = CinemaTextSecondary)
                        }
                    }
                }
            }

            is CreateStage.PollingGenerate -> {
                item {
                    val phaseLabel = when (stage.serverStatus.lowercase()) {
                        "tts" -> "Gemini TTS ဖြင့် မြန်မာအသံ သွင်းနေသည် (tts)"
                        "rendering" -> "FFmpeg ဖြင့် ဗီဒီယိုနှင့် အသံ ချုပ်လုပ်နေသည် (rendering)"
                        else -> "Server အခြေအနေ: ${stage.serverStatus}"
                    }
                    PipelineProgressCard(
                        title = "အသံနှင့် Recap ဗီဒီယို အစစ် ထုတ်လုပ်နေပါသည်",
                        statusLabel = "$phaseLabel • ကြာချိန် ${formatDurationMmSs(stage.elapsedSeconds.toDouble())}",
                        progressPercent = stage.serverProgress,
                        subtitle = "Server တွင် Gemini TTS နှင့် FFmpeg render လုပ်နေပါသည်။ ၅–၁၅ မိနစ်ခန့် ကြာနိုင်ပါသည်။",
                        onCancel = onResetFlow
                    )
                }
            }

            is CreateStage.Done -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .border(1.5.dp, CinemaEmerald.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .testTag("create_done_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaCard)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = CinemaEmerald,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Recap ဗီဒီယို ထုတ်လုပ်ပြီးပါပြီ",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CinemaTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${stage.recap.title} (${stage.recap.category} • ${stage.recap.scenes.size} ခန်း)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CinemaGold
                            )

                            if (!stage.actionMessage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = CinemaEmerald.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stage.actionMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CinemaEmerald,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onPreviewRecap(stage.recap) },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaCrimson),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("preview_generated_recap_button")
                            ) {
                                Icon(imageVector = Icons.Filled.PlayCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ကြည့်ရှုစစ်ဆေးမည် (Preview)", color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onPublishRecap(stage.recap.jobId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CinemaEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("publish_recap_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Publish,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("တင်မည် (Publish)", color = Color.Black)
                                }

                                OutlinedButton(
                                    onClick = { onSaveDraft(stage.recap.jobId) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("save_draft_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Drafts,
                                        contentDescription = null,
                                        tint = CinemaGold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("မူကြမ်းသိမ်းမည်", color = CinemaGold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onResetFlow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Recap အသစ်တစ်ခု ထပ်ဖန်တီးမည်", color = CinemaTextSecondary)
                            }
                        }
                    }
                }
            }

            is CreateStage.Failed -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .border(1.5.dp, CinemaError, RoundedCornerShape(18.dp))
                            .testTag("create_failed_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaError.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = CinemaError,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "လုပ်ဆောင်မှု မအောင်မြင်ပါ",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CinemaError
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = CinemaObsidian.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stage.errorMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CinemaTextPrimary,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .testTag("create_error_message_text")
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onResetFlow,
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaCrimson),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("retry_create_button")
                            ) {
                                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ပြန်လည်ကြိုးစားမည်", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineProgressCard(
    title: String,
    statusLabel: String,
    progressPercent: Int,
    subtitle: String,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .border(1.dp, CinemaCrimson.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .testTag("pipeline_progress_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaCrimson
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (progressPercent.coerceIn(0, 100)) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = CinemaCrimson,
                trackColor = CinemaElevated
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaTextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_pipeline_button")
            ) {
                Text("ရပ်တန့်မည်", color = CinemaTextSecondary)
            }
        }
    }
}

@Composable
private fun EditableSceneCard(
    scene: RecapScene,
    onUpdateNarration: (String) -> Unit,
    onUpdateTimestamps: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "အခန်း ${scene.sceneNumber}",
                style = MaterialTheme.typography.titleMedium,
                color = CinemaGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = scene.sourceStartSec.toString(),
                    onValueChange = { txt ->
                        val parsed = txt.filter { it.isDigit() }.toIntOrNull() ?: 0
                        onUpdateTimestamps(parsed, scene.sourceEndSec)
                    },
                    label = { Text("စတင်စက္ကန့်") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaCrimson,
                        unfocusedBorderColor = CinemaBorder,
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scene_start_${scene.sceneNumber}")
                )
                OutlinedTextField(
                    value = scene.sourceEndSec.toString(),
                    onValueChange = { txt ->
                        val parsed = txt.filter { it.isDigit() }.toIntOrNull() ?: scene.sourceStartSec
                        onUpdateTimestamps(scene.sourceStartSec, parsed)
                    },
                    label = { Text("ပြီးဆုံးစက္ကန့်") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaCrimson,
                        unfocusedBorderColor = CinemaBorder,
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scene_end_${scene.sceneNumber}")
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = scene.narrationMy,
                onValueChange = onUpdateNarration,
                label = { Text("မြန်မာ ဇာတ်ညွှန်းစာသား") },
                minLines = 2,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaCrimson,
                    unfocusedBorderColor = CinemaBorder,
                    focusedTextColor = CinemaTextPrimary,
                    unfocusedTextColor = CinemaTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scene_narration_${scene.sceneNumber}")
            )
        }
    }
}
