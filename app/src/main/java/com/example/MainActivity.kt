package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.database.WeddingDatabase
import com.example.data.repository.WeddingRepository
import com.example.ui.common.AppLanguage
import com.example.ui.common.AppStrings
import com.example.ui.common.LocalAppLanguage
import com.example.ui.common.SampleDataGenerator
import com.example.ui.common.WeddingTopBar
import com.example.ui.screens.EventGalleryScreen
import com.example.ui.screens.FaceSearchScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhotographerAdminScreen
import com.example.ui.screens.SelectionCartScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SelectionHighlight
import com.example.ui.viewmodel.WeddingViewModel
import com.example.ui.viewmodel.WeddingViewModelFactory
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME,
    GALLERY,
    FACE_SEARCH,
    SELECTIONS,
    ADMIN
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: WeddingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = WeddingDatabase.getDatabase(this)
        val repository = WeddingRepository(database.weddingDao())
        val factory = WeddingViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WeddingViewModel::class.java]

        // Seed initial rich wedding data in background
        lifecycleScope.launch {
            SampleDataGenerator.populateIfEmpty(database.weddingDao())
        }

        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalAppLanguage provides language) {
                MyApplicationTheme(darkTheme = true) {
                    WeddingAppMain(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun WeddingAppMain(viewModel: WeddingViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var isAdminMode by remember { mutableStateOf(false) }

    val language by viewModel.language.collectAsStateWithLifecycle()
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()
    val photos by viewModel.filteredPhotos.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPhotos by viewModel.selectedAlbumPhotos.collectAsStateWithLifecycle()
    val faceSearchState by viewModel.faceSearchState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val watermarkEnabled by viewModel.watermarkEnabled.collectAsStateWithLifecycle()
    val submissions by viewModel.allSubmissions.collectAsStateWithLifecycle()

    val activeEvent = events.firstOrNull { it.id == selectedEventId }
    val selectionQuota = activeEvent?.selectionQuota ?: 40

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Toast / SnackBar notifications on download
    LaunchedEffect(downloadState.toastMessage) {
        downloadState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.dismissToast()
        }
    }

    // Android System Back Navigation handling
    if (currentTab != ScreenTab.HOME) {
        BackHandler {
            currentTab = ScreenTab.HOME
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120F11)),
        topBar = {
            WeddingTopBar(
                currentLanguage = language,
                selectedCount = selectedPhotos.size,
                isAdminMode = currentTab == ScreenTab.ADMIN,
                onToggleLanguage = {
                    val nextLang = if (language == AppLanguage.BENGALI) AppLanguage.ENGLISH else AppLanguage.BENGALI
                    viewModel.setLanguage(nextLang)
                },
                onToggleAdminMode = {
                    currentTab = if (currentTab == ScreenTab.ADMIN) ScreenTab.HOME else ScreenTab.ADMIN
                },
                onNavigateToSelections = {
                    currentTab = ScreenTab.SELECTIONS
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                containerColor = Color(0xFF141113),
                contentColor = Color.White
            ) {
                // Tab 1: Home / Albums
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = { currentTab = ScreenTab.HOME },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.homeTab(language),
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color(0xFF8F8077),
                        unselectedTextColor = Color(0xFF8F8077)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // Tab 2: Gallery
                NavigationBarItem(
                    selected = currentTab == ScreenTab.GALLERY,
                    onClick = { currentTab = ScreenTab.GALLERY },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Gallery"
                        )
                    },
                    label = {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "গ্যালারি" else "Gallery",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color(0xFF8F8077),
                        unselectedTextColor = Color(0xFF8F8077)
                    ),
                    modifier = Modifier.testTag("nav_gallery")
                )

                // Tab 3: Face Search
                NavigationBarItem(
                    selected = currentTab == ScreenTab.FACE_SEARCH,
                    onClick = { currentTab = ScreenTab.FACE_SEARCH },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Face Search"
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.faceSearchTab(language),
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color(0xFF8F8077),
                        unselectedTextColor = Color(0xFF8F8077)
                    ),
                    modifier = Modifier.testTag("nav_face_search")
                )

                // Tab 4: Selections
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SELECTIONS,
                    onClick = { currentTab = ScreenTab.SELECTIONS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (selectedPhotos.isNotEmpty()) {
                                    Badge(
                                        containerColor = SelectionHighlight,
                                        contentColor = Color.Black
                                    ) {
                                        Text("${selectedPhotos.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Selections"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = AppStrings.selectionTab(language),
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color(0xFF8F8077),
                        unselectedTextColor = Color(0xFF8F8077)
                    ),
                    modifier = Modifier.testTag("nav_selections")
                )

                // Tab 5: Photographer Studio
                NavigationBarItem(
                    selected = currentTab == ScreenTab.ADMIN,
                    onClick = { currentTab = ScreenTab.ADMIN },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Studio Admin"
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.adminTab(language),
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color(0xFF8F8077),
                        unselectedTextColor = Color(0xFF8F8077)
                    ),
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        events = events,
                        selectedEventId = selectedEventId,
                        selectedPhotoCount = selectedPhotos.size,
                        language = language,
                        onSelectEvent = { id -> viewModel.selectEvent(id) },
                        onNavigateToGallery = { currentTab = ScreenTab.GALLERY },
                        onNavigateToFaceSearch = { currentTab = ScreenTab.FACE_SEARCH },
                        onNavigateToSelections = { currentTab = ScreenTab.SELECTIONS }
                    )
                }

                ScreenTab.GALLERY -> {
                    EventGalleryScreen(
                        photos = photos,
                        selectedCategory = selectedCategory,
                        selectedCount = selectedPhotos.size,
                        selectionQuota = selectionQuota,
                        language = language,
                        onCategorySelected = { cat -> viewModel.setCategory(cat) },
                        onToggleSelect = { photo, notes -> viewModel.togglePhotoSelection(photo, notes) },
                        onDownloadPhoto = { photo -> viewModel.downloadSinglePhoto(context, photo) },
                        onNavigateToFaceSearch = { currentTab = ScreenTab.FACE_SEARCH },
                        onNavigateToSelections = { currentTab = ScreenTab.SELECTIONS }
                    )
                }

                ScreenTab.FACE_SEARCH -> {
                    FaceSearchScreen(
                        state = faceSearchState,
                        language = language,
                        onPerformSearch = { bitmap -> viewModel.performFaceSearch(bitmap) },
                        onDownloadAll = { matched -> viewModel.downloadBatch(context, matched) },
                        onSelectAllMatched = { viewModel.selectAllMatchedForAlbum() },
                        onToggleSelectPhoto = { photo, notes -> viewModel.togglePhotoSelection(photo, notes) },
                        onDownloadSinglePhoto = { photo -> viewModel.downloadSinglePhoto(context, photo) }
                    )
                }

                ScreenTab.SELECTIONS -> {
                    SelectionCartScreen(
                        selectedPhotos = selectedPhotos,
                        selectionQuota = selectionQuota,
                        language = language,
                        onRemoveFromSelection = { photo -> viewModel.togglePhotoSelection(photo) },
                        onUpdateNotes = { photoId, notes -> viewModel.updatePhotoNotes(photoId, notes) },
                        onDownloadSelectedAll = { list -> viewModel.downloadBatch(context, list) },
                        onSubmitSelection = { name, phone, type, notes ->
                            viewModel.submitSelection(context, name, phone, type, notes)
                        }
                    )
                }

                ScreenTab.ADMIN -> {
                    PhotographerAdminScreen(
                        events = events,
                        selectedEventId = selectedEventId,
                        submissions = submissions,
                        watermarkEnabled = watermarkEnabled,
                        language = language,
                        onToggleWatermark = { en -> viewModel.toggleWatermark(en) },
                        onCreateEvent = { title, couple, date, venue, quota ->
                            viewModel.createNewEvent(title, couple, date, venue, quota)
                        },
                        onUploadPhotos = { uris, cat, prefix ->
                            viewModel.addPhotosToCurrentEvent(uris, cat, prefix)
                        }
                    )
                }
            }

            // Global Download Progress Dialog / Overlay
            if (downloadState.isDownloading) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF1E171B),
                    shadowElevation = 16.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI)
                                "ছবি ডাউনলোড হচ্ছে..."
                            else
                                "Downloading Photos...",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (downloadState.totalItems > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${downloadState.currentItem} / ${downloadState.totalItems}",
                                color = GoldPrimary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SelectionHighlight
                            )
                        }
                    }
                }
            }
        }
    }
}
