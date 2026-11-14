package com.example

import android.net.Uri
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.BirthdayRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOut
import com.example.ui.screens.CardStudioScreen
import com.example.ui.screens.CardViewerScreen
import com.example.ui.screens.CelebrateScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.BirthdayBlissTheme
import com.example.ui.viewmodel.CardStudioViewModel
import com.example.ui.viewmodel.GamesViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.StudioSection
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object Routes {
    const val HOME = "home"
    const val STUDIO_TAB = "studio_tab"
    const val GAMES = "games"
    const val CELEBRATE = "celebrate"
    const val STUDIO_EDITOR = "studio_editor?cardId={cardId}&theme={theme}&section={section}"
    const val CARD_VIEWER = "card_viewer/{cardId}"

    fun studioEditor(
        cardId: String = "",
        theme: String? = null,
        section: StudioSection = StudioSection.CARD_PHOTO,
    ): String {
        val idParam = Uri.encode(cardId)
        val themeParam = Uri.encode(theme ?: "")
        return "studio_editor?cardId=$idParam&theme=$themeParam&section=${section.name}"
    }

    fun cardViewer(cardId: String): String = "card_viewer/${Uri.encode(cardId)}"
}

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = Routes.HOME,
        labelRes = R.string.nav_cards,
        selectedIcon = Icons.Filled.Collections,
        unselectedIcon = Icons.Outlined.Collections,
        testTag = "nav_tab_cards",
    ),
    TopLevelDestination(
        route = Routes.STUDIO_TAB,
        labelRes = R.string.nav_studio,
        selectedIcon = Icons.Filled.Brush,
        unselectedIcon = Icons.Outlined.Brush,
        testTag = "nav_tab_studio",
    ),
    TopLevelDestination(
        route = Routes.GAMES,
        labelRes = R.string.nav_games,
        selectedIcon = Icons.Filled.SportsEsports,
        unselectedIcon = Icons.Outlined.SportsEsports,
        testTag = "nav_tab_games",
    ),
    TopLevelDestination(
        route = Routes.CELEBRATE,
        labelRes = R.string.nav_celebrate,
        selectedIcon = Icons.Filled.Celebration,
        unselectedIcon = Icons.Outlined.Celebration,
        testTag = "nav_tab_celebrate",
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BirthdayBlissTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = {},
                scope = scope,
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
        )
    } else {
        val uid = user.uid
        val displayName = user.displayName ?: user.email ?: ""
        key(uid) {
            BirthdayBlissApp(
                currentUserId = uid,
                userDisplayName = displayName,
                onSignOut = {
                    signOut(
                        credentialManager = credentialManager,
                        scope = scope,
                    )
                },
            )
        }
    }
}

@Composable
fun BirthdayBlissApp(
    currentUserId: String,
    userDisplayName: String,
    onSignOut: () -> Unit,
) {
    val factory = remember(currentUserId) {
        viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repo = BirthdayRepository(db = db, appContext = app.applicationContext)
                HomeViewModel(repo, currentUserId)
            }
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repo = BirthdayRepository(db = db, appContext = app.applicationContext)
                CardStudioViewModel(repo, currentUserId)
            }
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repo = BirthdayRepository(db = db, appContext = app.applicationContext)
                GamesViewModel(repo, currentUserId)
            }
        }
    }

    val homeViewModel: HomeViewModel = viewModel(key = "home_$currentUserId", factory = factory)
    val gamesViewModel: GamesViewModel = viewModel(key = "games_$currentUserId", factory = factory)

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showTopLevelNav = currentRoute in topLevelDestinations.map { it.route }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showTopLevelNav && !useNavRail) {
                    NavigationBar {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes),
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (showTopLevelNav && useNavRail) {
                    NavigationRail {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes),
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                    ) {
                        composable(Routes.HOME) {
                            val cardsState by homeViewModel.cardsState.collectAsStateWithLifecycle()
                            HomeScreen(
                                cardsState = cardsState,
                                userDisplayName = userDisplayName,
                                onCreateCard = { theme, section ->
                                    navController.navigate(
                                        Routes.studioEditor(
                                            cardId = "",
                                            theme = theme?.name,
                                            section = section,
                                        )
                                    )
                                },
                                onOpenCard = { id ->
                                    navController.navigate(Routes.cardViewer(id))
                                },
                                onEditCard = { id ->
                                    navController.navigate(Routes.studioEditor(cardId = id))
                                },
                                onDeleteCard = { card ->
                                    homeViewModel.deleteCard(card)
                                },
                                onOpenGames = { navigateTopLevel(Routes.GAMES) },
                                onSignOut = onSignOut,
                            )
                        }

                        composable(Routes.STUDIO_TAB) {
                            val studioViewModel: CardStudioViewModel = viewModel(
                                key = "studio_tab_$currentUserId",
                                factory = factory,
                            )
                            CardStudioScreen(
                                viewModel = studioViewModel,
                                cardId = null,
                                initialTheme = null,
                                initialSection = StudioSection.CARD_PHOTO,
                                onBack = null,
                                onCardSavedNavigate = { savedId ->
                                    navController.navigate(Routes.cardViewer(savedId))
                                },
                            )
                        }

                        composable(
                            route = Routes.STUDIO_EDITOR,
                            arguments = listOf(
                                navArgument("cardId") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("theme") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("section") {
                                    type = NavType.StringType
                                    defaultValue = StudioSection.CARD_PHOTO.name
                                },
                            ),
                        ) { backStackEntry ->
                            val cardIdArg = backStackEntry.arguments?.getString("cardId")?.takeIf { it.isNotBlank() }
                            val themeArg = backStackEntry.arguments?.getString("theme")?.takeIf { it.isNotBlank() }
                            val sectionArg = backStackEntry.arguments?.getString("section")
                                ?.let { runCatching { StudioSection.valueOf(it) }.getOrNull() }
                                ?: StudioSection.CARD_PHOTO
                            val studioViewModel: CardStudioViewModel = viewModel(
                                key = "studio_edit_${currentUserId}_${cardIdArg ?: "new"}",
                                factory = factory,
                            )
                            CardStudioScreen(
                                viewModel = studioViewModel,
                                cardId = cardIdArg,
                                initialTheme = themeArg,
                                initialSection = sectionArg,
                                onBack = { navController.popBackStack() },
                                onCardSavedNavigate = { savedId ->
                                    navController.navigate(Routes.cardViewer(savedId)) {
                                        popUpTo(Routes.HOME)
                                    }
                                },
                            )
                        }

                        composable(
                            route = Routes.CARD_VIEWER,
                            arguments = listOf(
                                navArgument("cardId") { type = NavType.StringType },
                            ),
                        ) { backStackEntry ->
                            val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
                            CardViewerScreen(
                                cardFlow = remember(currentUserId, cardId) {
                                    homeViewModel.observeCard(cardId)
                                },
                                onBack = { navController.popBackStack() },
                                onEdit = { id, section ->
                                    navController.navigate(
                                        Routes.studioEditor(cardId = id, section = section)
                                    )
                                },
                            )
                        }

                        composable(Routes.GAMES) {
                            GamesScreen(viewModel = gamesViewModel)
                        }

                        composable(Routes.CELEBRATE) {
                            CelebrateScreen()
                        }
                    }
                }
            }
        }
    }
}
