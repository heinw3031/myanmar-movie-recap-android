package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettings
import com.example.data.local.RecapDatabase
import com.example.data.local.SettingsRepository
import com.example.data.media.PickedVideoInfo
import com.example.data.media.VideoCompressor
import com.example.data.model.RECAP_CATEGORIES
import com.example.data.model.RecapItem
import com.example.data.model.RecapScene
import com.example.data.model.toDomain
import com.example.data.remote.BackendApiClient
import com.example.data.repository.RecapScriptRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface CreateStage {
    data object Idle : CreateStage
    data class Compressing(val progressPercent: Int) : CreateStage
    data class Uploading(
        val progressPercent: Int,
        val uploadedBytes: Long,
        val totalBytes: Long
    ) : CreateStage

    data class PollingScript(
        val jobId: String,
        val serverStatus: String,
        val serverProgress: Int,
        val elapsedSeconds: Int
    ) : CreateStage

    data class AwaitingApproval(
        val jobId: String,
        val title: String,
        val scenes: List<RecapScene>,
        val isSubmittingApproval: Boolean = false,
        val isApproved: Boolean = false,
        val approvalError: String? = null
    ) : CreateStage

    data class PollingGenerate(
        val jobId: String,
        val serverStatus: String,
        val serverProgress: Int,
        val elapsedSeconds: Int
    ) : CreateStage

    data class Done(
        val recap: RecapItem,
        val actionMessage: String? = null
    ) : CreateStage

    data class Failed(
        val jobId: String?,
        val errorMessage: String
    ) : CreateStage
}

data class CreateUiState(
    val pickedVideo: PickedVideoInfo? = null,
    val selectedCategory: String = RECAP_CATEGORIES.first(),
    val selectedLengthMinutes: Int = 10,
    val stage: CreateStage = CreateStage.Idle
)

data class MainUiState(
    val settings: AppSettings = AppSettings(
        backendUrl = "",
        creatorName = "Recap ဖန်တီးသူ",
        defaultQuality = "720",
        compressBeforeUpload = true
    ),
    val allRecaps: List<RecapItem> = emptyList(),
    val selectedHomeCategory: String = "အားလုံး",
    val searchQuery: String = "",
    val createState: CreateUiState = CreateUiState(),
    val activePlayerRecapId: String? = null,
    val activePlayerQuality: String = "720",
    val isRefreshingFeed: Boolean = false
) {
    val isBackendConfigured: Boolean
        get() = settings.backendUrl.trim().isNotEmpty()

    val publishedRecaps: List<RecapItem>
        get() = allRecaps.filter { it.isPublished }

    val filteredHomeRecaps: List<RecapItem>
        get() = if (selectedHomeCategory == "အားလုံး") {
            publishedRecaps
        } else {
            publishedRecaps.filter { it.category.equals(selectedHomeCategory, ignoreCase = true) }
        }

    val savedRecaps: List<RecapItem>
        get() = allRecaps.filter { it.isSaved }

    val likedRecaps: List<RecapItem>
        get() = allRecaps.filter { it.isLiked }

    val draftRecaps: List<RecapItem>
        get() = allRecaps.filter { it.isDraft }

    val historyRecaps: List<RecapItem>
        get() = allRecaps
            .filter { it.lastWatchedAt != null }
            .sortedByDescending { it.lastWatchedAt }

    val activePlayerRecap: RecapItem?
        get() = activePlayerRecapId?.let { id -> allRecaps.find { it.jobId == id } }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val apiClient = BackendApiClient()
    private val database = RecapDatabase.getInstance(application)
    private val repository = RecapScriptRepository(database.recapDao(), apiClient)
    private val videoCompressor = VideoCompressor(application)

    private val _selectedCategory = MutableStateFlow("အားလုံး")
    private val _searchQuery = MutableStateFlow("")
    private val _createState = MutableStateFlow(CreateUiState())
    private val _activePlayerRecapId = MutableStateFlow<String?>(null)
    private val _activePlayerQuality = MutableStateFlow("720")
    private val _isRefreshingFeed = MutableStateFlow(false)

    private var activeJobRunner: Job? = null
    private var lastSubmittedTitle: String = ""
    private var lastSubmittedScenes: List<RecapScene> = emptyList()

    private val _auxState = combine(
        _selectedCategory,
        _searchQuery,
        _createState,
        _activePlayerRecapId,
        _activePlayerQuality
    ) { homeCat, query, create, playerId, quality ->
        AuxUiBundle(homeCat, query, create, playerId, quality)
    }

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepo.settingsFlow,
        repository.allRecapsFlow,
        _auxState,
        _isRefreshingFeed
    ) { settings, recaps, aux, refreshing ->
        MainUiState(
            settings = settings,
            allRecaps = recaps,
            selectedHomeCategory = aux.homeCat,
            searchQuery = aux.query,
            createState = aux.create,
            activePlayerRecapId = aux.playerId,
            activePlayerQuality = aux.quality,
            isRefreshingFeed = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { settings ->
                _activePlayerQuality.value = settings.defaultQuality
                if (settings.backendUrl.isNotBlank()) {
                    repository.syncCompletedJobsFromBackend(settings.backendUrl)
                }
            }
        }
    }

    fun refreshFromBackend() {
        val baseUrl = uiState.value.settings.backendUrl
        if (baseUrl.isBlank()) return
        viewModelScope.launch {
            _isRefreshingFeed.value = true
            repository.syncCompletedJobsFromBackend(baseUrl)
            _isRefreshingFeed.value = false
        }
    }

    fun selectHomeCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onVideoPicked(uri: Uri) {
        val info = videoCompressor.inspectPickedVideo(uri)
        _createState.update {
            it.copy(
                pickedVideo = info,
                stage = CreateStage.Idle
            )
        }
    }

    fun selectCreateCategory(category: String) {
        _createState.update { it.copy(selectedCategory = category) }
    }

    fun selectCreateLength(minutes: Int) {
        _createState.update { it.copy(selectedLengthMinutes = minutes) }
    }

    fun uploadPickedVideoAndRequestScript() {
        val currentSettings = uiState.value.settings
        val baseUrl = currentSettings.backendUrl.trim()
        if (baseUrl.isBlank()) {
            _createState.update {
                it.copy(
                    stage = CreateStage.Failed(
                        jobId = null,
                        errorMessage = "Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ"
                    )
                )
            }
            return
        }

        val picked = _createState.value.pickedVideo ?: return
        val category = _createState.value.selectedCategory
        val lengthMinutes = _createState.value.selectedLengthMinutes

        activeJobRunner?.cancel()
        activeJobRunner = viewModelScope.launch {
            var stagedFile: java.io.File? = null
            try {
                _createState.update {
                    it.copy(stage = CreateStage.Compressing(0))
                }
                val prepared = videoCompressor.prepareForUpload(
                    picked = picked,
                    compress = currentSettings.compressBeforeUpload,
                    onCompressionProgress = { pct ->
                        _createState.update {
                            it.copy(stage = CreateStage.Compressing(pct))
                        }
                    }
                )
                stagedFile = prepared.file

                val totalBytes = prepared.file.length().coerceAtLeast(1L)
                _createState.update {
                    it.copy(
                        stage = CreateStage.Uploading(
                            progressPercent = 0,
                            uploadedBytes = 0L,
                            totalBytes = totalBytes
                        )
                    )
                }

                val uploadResult = repository.uploadVideoJob(
                    baseUrl = baseUrl,
                    videoFile = prepared.file,
                    displayName = picked.displayName,
                    mimeType = prepared.mimeType,
                    lengthMinutes = lengthMinutes,
                    category = category,
                    onUploadProgress = { written, total ->
                        val safeTotal = total.coerceAtLeast(1L)
                        val pct = ((written * 100L) / safeTotal).toInt().coerceIn(0, 100)
                        _createState.update {
                            it.copy(
                                stage = CreateStage.Uploading(
                                    progressPercent = pct,
                                    uploadedBytes = written,
                                    totalBytes = safeTotal
                                )
                            )
                        }
                    }
                )

                stagedFile.delete()
                stagedFile = null

                uploadResult.fold(
                    onSuccess = { jobId ->
                        pollUntilAwaitingApproval(baseUrl, jobId)
                    },
                    onFailure = { err ->
                        _createState.update {
                            it.copy(
                                stage = CreateStage.Failed(
                                    jobId = null,
                                    errorMessage = err.message ?: "ဗီဒီယို upload မအောင်မြင်ပါ"
                                )
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                stagedFile?.delete()
                _createState.update {
                    it.copy(
                        stage = CreateStage.Failed(
                            jobId = null,
                            errorMessage = e.message ?: "ဗီဒီယိုပြင်ဆင်မှု အမှား ဖြစ်ပွားပါသည်"
                        )
                    )
                }
            }
        }
    }

    private suspend fun pollUntilAwaitingApproval(baseUrl: String, jobId: String) {
        val startMs = System.currentTimeMillis()
        while (viewModelScope.isActive) {
            val elapsedSec = ((System.currentTimeMillis() - startMs) / 1000L).toInt()
            val statusResult = repository.fetchJobStatus(baseUrl, jobId)

            var shouldStop = false
            statusResult.fold(
                onSuccess = { job ->
                    val status = job.status.lowercase()
                    when (status) {
                        "awaiting-approval", "awaiting_approval" -> {
                            val scenes = job.scenes?.map { it.toDomain() }.orEmpty()
                            val defaultTitle = job.title?.takeIf { it.isNotBlank() }
                                ?: _createState.value.pickedVideo?.displayName?.substringBeforeLast('.')
                                ?: "ရုပ်ရှင် Recap"
                            _createState.update {
                                it.copy(
                                    stage = CreateStage.AwaitingApproval(
                                        jobId = jobId,
                                        title = defaultTitle,
                                        scenes = scenes,
                                        isSubmittingApproval = false,
                                        isApproved = false
                                    )
                                )
                            }
                            shouldStop = true
                        }
                        "failed", "error" -> {
                            val serverError = job.error?.takeIf { it.isNotBlank() }
                                ?: job.message?.takeIf { it.isNotBlank() }
                                ?: "Server မှ ဇာတ်ညွှန်းထုတ်လုပ်မှု မအောင်မြင်ပါ"
                            _createState.update {
                                it.copy(
                                    stage = CreateStage.Failed(
                                        jobId = jobId,
                                        errorMessage = serverError
                                    )
                                )
                            }
                            shouldStop = true
                        }
                        "done" -> {
                            val saved = repository.saveCompletedRecap(
                                baseUrl = baseUrl,
                                job = job,
                                fallbackTitle = job.title ?: "ရုပ်ရှင် Recap",
                                fallbackCategory = _createState.value.selectedCategory,
                                fallbackLengthMinutes = _createState.value.selectedLengthMinutes,
                                fallbackScenes = job.scenes?.map { it.toDomain() }.orEmpty(),
                                asPublished = false
                            )
                            _createState.update {
                                it.copy(stage = CreateStage.Done(recap = saved))
                            }
                            shouldStop = true
                        }
                        else -> {
                            _createState.update {
                                it.copy(
                                    stage = CreateStage.PollingScript(
                                        jobId = jobId,
                                        serverStatus = job.status.ifBlank { "queued" },
                                        serverProgress = job.resolvedProgressInt(),
                                        elapsedSeconds = elapsedSec
                                    )
                                )
                            }
                        }
                    }
                },
                onFailure = { err ->
                    _createState.update {
                        it.copy(
                            stage = CreateStage.Failed(
                                jobId = jobId,
                                errorMessage = err.message ?: "Server အခြေအနေ စစ်ဆေးမရပါ"
                            )
                        )
                    }
                    shouldStop = true
                }
            )
            if (shouldStop) break
            delay(POLL_INTERVAL_MS)
        }
    }

    fun updateApprovalTitle(title: String) {
        _createState.update { state ->
            val current = state.stage as? CreateStage.AwaitingApproval ?: return@update state
            state.copy(
                stage = current.copy(
                    title = title,
                    isApproved = false,
                    approvalError = null
                )
            )
        }
    }

    fun updateSceneNarration(sceneNumber: Int, newNarration: String) {
        _createState.update { state ->
            val current = state.stage as? CreateStage.AwaitingApproval ?: return@update state
            val updated = current.scenes.map { scene ->
                if (scene.sceneNumber == sceneNumber) {
                    scene.copy(narrationMy = newNarration)
                } else {
                    scene
                }
            }
            state.copy(
                stage = current.copy(
                    scenes = updated,
                    isApproved = false,
                    approvalError = null
                )
            )
        }
    }

    fun updateSceneTimestamps(sceneNumber: Int, startSec: Int, endSec: Int) {
        _createState.update { state ->
            val current = state.stage as? CreateStage.AwaitingApproval ?: return@update state
            val updated = current.scenes.map { scene ->
                if (scene.sceneNumber == sceneNumber) {
                    val safeStart = startSec.coerceAtLeast(0)
                    val safeEnd = endSec.coerceAtLeast(safeStart)
                    scene.copy(sourceStartSec = safeStart, sourceEndSec = safeEnd)
                } else {
                    scene
                }
            }
            state.copy(
                stage = current.copy(
                    scenes = updated,
                    isApproved = false,
                    approvalError = null
                )
            )
        }
    }

    fun approveScript() {
        val baseUrl = uiState.value.settings.backendUrl.trim()
        if (baseUrl.isBlank()) return
        val current = _createState.value.stage as? CreateStage.AwaitingApproval ?: return
        if (current.isSubmittingApproval) return

        viewModelScope.launch {
            _createState.update {
                it.copy(
                    stage = current.copy(
                        isSubmittingApproval = true,
                        approvalError = null
                    )
                )
            }

            val result = repository.approveScript(
                baseUrl = baseUrl,
                jobId = current.jobId,
                title = current.title,
                scenes = current.scenes
            )

            result.fold(
                onSuccess = {
                    lastSubmittedTitle = current.title
                    lastSubmittedScenes = current.scenes
                    _createState.update {
                        val latest = (it.stage as? CreateStage.AwaitingApproval) ?: current
                        it.copy(
                            stage = latest.copy(
                                isSubmittingApproval = false,
                                isApproved = true,
                                approvalError = null
                            )
                        )
                    }
                },
                onFailure = { err ->
                    _createState.update {
                        val latest = (it.stage as? CreateStage.AwaitingApproval) ?: current
                        it.copy(
                            stage = latest.copy(
                                isSubmittingApproval = false,
                                isApproved = false,
                                approvalError = err.message ?: "ဇာတ်ညွှန်း အတည်ပြုမှု မအောင်မြင်ပါ"
                            )
                        )
                    }
                }
            )
        }
    }

    fun generateRecap() {
        val baseUrl = uiState.value.settings.backendUrl.trim()
        if (baseUrl.isBlank()) {
            _createState.update {
                it.copy(
                    stage = CreateStage.Failed(
                        jobId = null,
                        errorMessage = "Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ"
                    )
                )
            }
            return
        }

        val current = _createState.value.stage as? CreateStage.AwaitingApproval ?: return
        if (!current.isApproved) return

        lastSubmittedTitle = current.title
        lastSubmittedScenes = current.scenes

        activeJobRunner?.cancel()
        activeJobRunner = viewModelScope.launch {
            _createState.update {
                it.copy(
                    stage = CreateStage.PollingGenerate(
                        jobId = current.jobId,
                        serverStatus = "tts",
                        serverProgress = 0,
                        elapsedSeconds = 0
                    )
                )
            }

            val startResult = repository.startGenerate(baseUrl, current.jobId)
            startResult.fold(
                onSuccess = {
                    pollUntilGenerateFinished(baseUrl, current.jobId)
                },
                onFailure = { err ->
                    _createState.update {
                        it.copy(
                            stage = CreateStage.Failed(
                                jobId = current.jobId,
                                errorMessage = err.message ?: "Recap ထုတ်လုပ်မှု စတင်မရပါ"
                            )
                        )
                    }
                }
            )
        }
    }

    private suspend fun pollUntilGenerateFinished(baseUrl: String, jobId: String) {
        val startMs = System.currentTimeMillis()
        while (viewModelScope.isActive) {
            val elapsedSec = ((System.currentTimeMillis() - startMs) / 1000L).toInt()
            val statusResult = repository.fetchJobStatus(baseUrl, jobId)

            var shouldStop = false
            statusResult.fold(
                onSuccess = { job ->
                    val status = job.status.lowercase()
                    when (status) {
                        "done", "completed" -> {
                            val recap = repository.saveCompletedRecap(
                                baseUrl = baseUrl,
                                job = job,
                                fallbackTitle = lastSubmittedTitle.ifBlank { "ရုပ်ရှင် Recap" },
                                fallbackCategory = _createState.value.selectedCategory,
                                fallbackLengthMinutes = _createState.value.selectedLengthMinutes,
                                fallbackScenes = lastSubmittedScenes,
                                asPublished = false
                            )
                            _createState.update {
                                it.copy(stage = CreateStage.Done(recap = recap))
                            }
                            shouldStop = true
                        }
                        "failed", "error" -> {
                            val serverError = job.error?.takeIf { it.isNotBlank() }
                                ?: job.message?.takeIf { it.isNotBlank() }
                                ?: "Server မှ ဗီဒီယိုထုတ်လုပ်မှု မအောင်မြင်ပါ"
                            _createState.update {
                                it.copy(
                                    stage = CreateStage.Failed(
                                        jobId = jobId,
                                        errorMessage = serverError
                                    )
                                )
                            }
                            shouldStop = true
                        }
                        else -> {
                            _createState.update {
                                it.copy(
                                    stage = CreateStage.PollingGenerate(
                                        jobId = jobId,
                                        serverStatus = job.status.ifBlank { "tts" },
                                        serverProgress = job.resolvedProgressInt(),
                                        elapsedSeconds = elapsedSec
                                    )
                                )
                            }
                        }
                    }
                },
                onFailure = { err ->
                    _createState.update {
                        it.copy(
                            stage = CreateStage.Failed(
                                jobId = jobId,
                                errorMessage = err.message ?: "Server မှ အခြေအနေ ဖတ်မရပါ"
                            )
                        )
                    }
                    shouldStop = true
                }
            )
            if (shouldStop) break
            delay(POLL_INTERVAL_MS)
        }
    }

    fun publishCompletedRecap(jobId: String) {
        viewModelScope.launch {
            repository.publishRecap(jobId)
            _createState.update { state ->
                val done = state.stage as? CreateStage.Done ?: return@update state
                if (done.recap.jobId == jobId) {
                    state.copy(
                        stage = done.copy(
                            recap = done.recap.copy(isPublished = true, isDraft = false),
                            actionMessage = "ပင်မစာမျက်နှာနှင့် ပရိုဖိုင်သို့ တင်ပြီးပါပြီ"
                        )
                    )
                } else state
            }
        }
    }

    fun saveCompletedAsDraft(jobId: String) {
        viewModelScope.launch {
            repository.saveDraft(jobId)
            _createState.update { state ->
                val done = state.stage as? CreateStage.Done ?: return@update state
                if (done.recap.jobId == jobId) {
                    state.copy(
                        stage = done.copy(
                            recap = done.recap.copy(isDraft = true, isPublished = false),
                            actionMessage = "စာကြည့်တိုက် > မူကြမ်းများ တွင် သိမ်းဆည်းပြီးပါပြီ"
                        )
                    )
                } else state
            }
        }
    }

    fun resetCreateFlow() {
        activeJobRunner?.cancel()
        _createState.update {
            CreateUiState(
                selectedCategory = it.selectedCategory,
                selectedLengthMinutes = it.selectedLengthMinutes
            )
        }
    }

    fun openPlayer(recap: RecapItem) {
        _activePlayerRecapId.value = recap.jobId
        viewModelScope.launch {
            repository.recordWatch(recap.jobId)
        }
    }

    fun closePlayer() {
        _activePlayerRecapId.value = null
    }

    fun setPlayerQuality(quality: String) {
        _activePlayerQuality.value = if (quality == "480") "480" else "720"
    }

    fun resolveVideoPlaybackUrl(recap: RecapItem, quality: String): String {
        return repository.buildPlaybackUrl(
            baseUrl = uiState.value.settings.backendUrl,
            recap = recap,
            quality = quality
        )
    }

    fun toggleSaved(recap: RecapItem) {
        viewModelScope.launch {
            repository.toggleSaved(recap.jobId, !recap.isSaved)
        }
    }

    fun toggleLiked(recap: RecapItem) {
        viewModelScope.launch {
            repository.toggleLiked(recap.jobId, !recap.isLiked)
        }
    }

    fun publishFromLibrary(jobId: String) {
        viewModelScope.launch {
            repository.publishRecap(jobId)
        }
    }

    fun deleteRecap(jobId: String) {
        viewModelScope.launch {
            repository.deleteRecap(jobId)
            if (_activePlayerRecapId.value == jobId) {
                _activePlayerRecapId.value = null
            }
        }
    }

    fun saveBackendUrl(url: String) {
        viewModelScope.launch {
            settingsRepo.setBackendUrl(url)
        }
    }

    fun clearBackendUrl() {
        viewModelScope.launch {
            settingsRepo.clearBackendUrl()
        }
    }

    fun resetBackendUrlToDefault() {
        viewModelScope.launch {
            settingsRepo.resetBackendUrlToDefault()
        }
    }

    fun saveCreatorName(name: String) {
        viewModelScope.launch {
            settingsRepo.setCreatorName(name)
        }
    }

    fun saveDefaultQuality(quality: String) {
        viewModelScope.launch {
            settingsRepo.setDefaultQuality(quality)
        }
    }

    fun setCompressBeforeUpload(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setCompressBeforeUpload(enabled)
        }
    }

    private data class AuxUiBundle(
        val homeCat: String,
        val query: String,
        val create: CreateUiState,
        val playerId: String?,
        val quality: String
    )

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
    }
}
