package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaElevated
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.MyanmarMovieRecapTheme
import com.example.ui.viewmodel.MainViewModel

private sealed class AppTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : AppTab("home", "ပင်မ", Icons.Filled.Home, Icons.Outlined.Home)
    data object Search : AppTab("search", "ရှာဖွေရန်", Icons.Filled.Search, Icons.Outlined.Search)
    data object Create : AppTab("create", "ဖန်တီးရန်", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline)
    data object Library : AppTab("library", "စာကြည့်တိုက်", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary)
    data object Profile : AppTab("profile", "ပရိုဖိုင်", Icons.Filled.Person, Icons.Outlined.Person)
}

private val ALL_TABS = listOf(
    AppTab.Home,
    AppTab.Search,
    AppTab.Create,
    AppTab.Library,
    AppTab.Profile
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyanmarMovieRecapTheme {
                MyanmarMovieRecapApp()
            }
        }
    }
}

@Composable
fun MyanmarMovieRecapApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppTab.Home.route

    val navigateToTab: (AppTab) -> Unit = { tab ->
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val activePlayerRecap = uiState.activePlayerRecap
    if (activePlayerRecap != null) {
        val playbackUrl = viewModel.resolveVideoPlaybackUrl(
            recap = activePlayerRecap,
            quality = uiState.activePlayerQuality
        )
        PlayerScreen(
            recap = activePlayerRecap,
            videoUrl = playbackUrl,
            selectedQuality = uiState.activePlayerQuality,
            onSelectQuality = viewModel::setPlayerQuality,
            onToggleSave = { viewModel.toggleSaved(activePlayerRecap) },
            onToggleLike = { viewModel.toggleLiked(activePlayerRecap) },
            onPublishDraft = { viewModel.publishFromLibrary(activePlayerRecap.jobId) },
            onClose = viewModel::closePlayer
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 840.dp

        Scaffold(
            containerColor = CinemaObsidian,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = CinemaSurface) {
                        ALL_TABS.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaCrimson,
                                    selectedTextColor = CinemaTextPrimary,
                                    indicatorColor = CinemaElevated,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = CinemaSurface) {
                        ALL_TABS.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CinemaCrimson,
                                    selectedTextColor = CinemaTextPrimary,
                                    indicatorColor = CinemaElevated,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_rail_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = AppTab.Home.route
                    ) {
                        composable(AppTab.Home.route) {
                            HomeScreen(
                                uiState = uiState,
                                onSelectCategory = viewModel::selectHomeCategory,
                                onRefresh = viewModel::refreshFromBackend,
                                onOpenCreate = { navigateToTab(AppTab.Create) },
                                onPlayRecap = viewModel::openPlayer,
                                onToggleSave = viewModel::toggleSaved,
                                onToggleLike = viewModel::toggleLiked
                            )
                        }
                        composable(AppTab.Search.route) {
                            SearchScreen(
                                uiState = uiState,
                                onQueryChange = viewModel::updateSearchQuery,
                                onPlayRecap = viewModel::openPlayer,
                                onToggleSave = viewModel::toggleSaved,
                                onToggleLike = viewModel::toggleLiked
                            )
                        }
                        composable(AppTab.Create.route) {
                            CreateScreen(
                                uiState = uiState,
                                onVideoPicked = viewModel::onVideoPicked,
                                onSelectCategory = viewModel::selectCreateCategory,
                                onSelectLength = viewModel::selectCreateLength,
                                onUploadVideo = viewModel::uploadPickedVideoAndRequestScript,
                                onUpdateTitle = viewModel::updateApprovalTitle,
                                onUpdateSceneNarration = viewModel::updateSceneNarration,
                                onUpdateSceneTimestamps = viewModel::updateSceneTimestamps,
                                onApproveScript = viewModel::approveScript,
                                onGenerateRecap = viewModel::generateRecap,
                                onPreviewRecap = viewModel::openPlayer,
                                onPublishRecap = viewModel::publishCompletedRecap,
                                onSaveDraft = viewModel::saveCompletedAsDraft,
                                onResetFlow = viewModel::resetCreateFlow,
                                onOpenSettings = { navigateToTab(AppTab.Profile) }
                            )
                        }
                        composable(AppTab.Library.route) {
                            LibraryScreen(
                                uiState = uiState,
                                onPlayRecap = viewModel::openPlayer,
                                onToggleSave = viewModel::toggleSaved,
                                onToggleLike = viewModel::toggleLiked,
                                onPublishDraft = viewModel::publishFromLibrary,
                                onDeleteRecap = viewModel::deleteRecap
                            )
                        }
                        composable(AppTab.Profile.route) {
                            ProfileScreen(
                                uiState = uiState,
                                onSaveBackendUrl = viewModel::saveBackendUrl,
                                onClearBackendUrl = viewModel::clearBackendUrl,
                                onResetBackendUrlToDefault = viewModel::resetBackendUrlToDefault,
                                onSaveCreatorName = viewModel::saveCreatorName,
                                onSaveDefaultQuality = viewModel::saveDefaultQuality,
                                onToggleCompressBeforeUpload = viewModel::setCompressBeforeUpload,
                                onPlayRecap = viewModel::openPlayer,
                                onToggleSave = viewModel::toggleSaved,
                                onToggleLike = viewModel::toggleLiked
                            )
                        }
                    }
                }
            }
        }
    }
}
