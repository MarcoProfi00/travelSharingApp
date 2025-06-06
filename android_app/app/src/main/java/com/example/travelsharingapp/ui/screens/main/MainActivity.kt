package com.example.travelsharingapp.ui.screens.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.travelsharingapp.data.repository.ChatRepository
import com.example.travelsharingapp.data.repository.NotificationRepository
import com.example.travelsharingapp.data.repository.ThemeRepository
import com.example.travelsharingapp.data.repository.TravelApplicationRepository
import com.example.travelsharingapp.data.repository.TravelProposalRepository
import com.example.travelsharingapp.data.repository.TravelReviewRepository
import com.example.travelsharingapp.data.repository.UserRepository
import com.example.travelsharingapp.data.repository.UserReviewRepository
import com.example.travelsharingapp.ui.screens.account_management.ChangePasswordScreen
import com.example.travelsharingapp.ui.screens.account_management.DeleteAccountScreen
import com.example.travelsharingapp.ui.screens.account_management.ManagePasskeyScreen
import com.example.travelsharingapp.ui.screens.authentication.AuthLoginScreen
import com.example.travelsharingapp.ui.screens.authentication.AuthResetPasswordScreen
import com.example.travelsharingapp.ui.screens.authentication.AuthSignupScreen
import com.example.travelsharingapp.ui.screens.authentication.AuthState
import com.example.travelsharingapp.ui.screens.authentication.AuthViewModel
import com.example.travelsharingapp.ui.screens.authentication.AuthViewModelFactory
import com.example.travelsharingapp.ui.screens.chat.ChatListScreen
import com.example.travelsharingapp.ui.screens.chat.ChatRoomScreen
import com.example.travelsharingapp.ui.screens.chat.ChatViewModel
import com.example.travelsharingapp.ui.screens.chat.ChatViewModelFactory
import com.example.travelsharingapp.ui.screens.notification.NotificationScreen
import com.example.travelsharingapp.ui.screens.notification.NotificationViewModel
import com.example.travelsharingapp.ui.screens.notification.NotificationViewModelFactory
import com.example.travelsharingapp.ui.screens.settings.SettingsScreen
import com.example.travelsharingapp.ui.screens.settings.SplashViewModel
import com.example.travelsharingapp.ui.screens.settings.ThemeViewModel
import com.example.travelsharingapp.ui.screens.settings.ThemeViewModelFactory
import com.example.travelsharingapp.ui.screens.travel_application.ApplicationAddNewScreen
import com.example.travelsharingapp.ui.screens.travel_application.ApplicationManageAllScreen
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModelFactory
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalInfoScreen
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalJoinedScreen
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalListScreen
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalManageScreen
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalOwnedListScreen
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModelFactory
import com.example.travelsharingapp.ui.screens.travel_review.TravelReviewAddNewScreen
import com.example.travelsharingapp.ui.screens.travel_review.TravelReviewViewAllScreen
import com.example.travelsharingapp.ui.screens.travel_review.TravelReviewViewModel
import com.example.travelsharingapp.ui.screens.travel_review.TravelReviewViewModelFactory
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileEditScreen
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileInitialSetupFactory
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileInitialSetupScreen
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileInitialSetupViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileScreen
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModelFactory
import com.example.travelsharingapp.ui.screens.user_review.UserReviewAllScreen
import com.example.travelsharingapp.ui.screens.user_review.UserReviewListScreen
import com.example.travelsharingapp.ui.screens.user_review.UserReviewViewModel
import com.example.travelsharingapp.ui.screens.user_review.UserReviewViewModelFactory
import com.example.travelsharingapp.ui.theme.TravelProposalTheme
import com.example.travelsharingapp.utils.LockScreenOrientation
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.listOf

enum class BottomTab {
    Explore,
    MyTrips,
    Create,
    Joined,
    Profile
}

object AppRoutes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val SETTINGS = "settings"
    const val INITIAL_PROFILE_SETUP = "initialProfileSetup"

    const val CHANGE_PASSWORD = "changePassword"
    const val RESET_PASSWORD = "resetPassword"
    const val MANAGE_PASSKEYS = "managePasskeys"
    const val DELETE_ACCOUNT = "deleteAccount"

    const val TRAVEL_PROPOSAL_LIST = "travelProposalList"
    const val TRAVEL_PROPOSAL_INFO = "travelProposalInfo/{proposalId}"
    const val TRAVEL_PROPOSAL_EDIT = "travelProposalEdit/{proposalId}"
    const val TRAVEL_PROPOSAL_DUPLICATE = "travelProposalDuplicate/{proposalId}"
    const val TRAVEL_PROPOSAL_NEW = "travelProposalNew"
    const val TRAVEL_PROPOSAL_OWN = "travelProposalOwn"
    const val TRAVEL_PROPOSAL_JOINED = "joinedProposals"
    const val TRAVEL_PROPOSAL_APPLY = "travelProposalApply/{proposalId}"
    const val TRAVEL_PROPOSAL_USER_REVIEWS = "travelProposalUserReviews/{proposalId}"

    const val USER_PROFILE = "userProfile/{userId}/{isOwnProfile}"
    const val EDIT_PROFILE = "editProfile"

    const val MANAGE_APPLICATIONS = "manageApplications/{proposalId}"
    const val ADD_REVIEW = "add_review/{proposalId}/{editing}"
    const val REVIEW_VIEW_ALL = "reviewViewAll/{proposalId}"
    const val USER_REVIEWS_VIEW_ALL = "userReviews/{userId}"

    const val NOTIFICATIONS = "notifications"

    const val CHAT_LIST = "chatList"
    const val CHAT_ROOM = "chat_room"

    fun initialProfileSetup(userId: String, email: String) = "initialProfileSetup/$userId/$email"
    fun userProfile(userId: String, isOwnProfile: Boolean) = "userProfile/$userId/$isOwnProfile"
    fun userReviewsViewAllScreen(userId: String) = "userReviews/$userId"
    fun travelProposalInfo(proposalId: String) = "travelProposalInfo/$proposalId"
    fun travelProposalEdit(proposalId: String) = "travelProposalEdit/$proposalId"
    fun travelProposalDuplicate(proposalId: String) = "travelProposalDuplicate/$proposalId"
    fun manageApplications(proposalId: String) = "manageApplications/$proposalId"
    fun addReview(proposalId: String, editing: Boolean = false) = "add_review/$proposalId/$editing"
    fun travelProposalApply(proposalId: String) = "travelProposalApply/$proposalId"
    fun reviewViewAllScreen(proposalId: String) = "reviewViewAll/$proposalId"
    fun travelProposalUserReviews(proposalId: String) = "travelProposalUserReviews/$proposalId"
}

val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

class MainActivity : ComponentActivity() {
    private val splashScreenviewModel: SplashViewModel by viewModels()

    private var showNotificationRationaleDialog by mutableStateOf(false)
    private var showPermissionDeniedPermanentlyDialog by mutableStateOf(false)

    private var currentActivityIntent by mutableStateOf<Intent?>(null)

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_DELAY_MS = 1500L
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // okay
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionDeniedPermanentlyDialog = true
            } else {
                Toast.makeText(this, "Notifications are disabled. You might miss important updates.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationRationaleDialog = true
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentActivityIntent = intent

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashScreenviewModel.isLoading.value
            }
        }

        val placesClient = Places.createClient(this)

        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(ThemeRepository(context))
            )
            val currentThemeSetting by themeViewModel.themeSetting.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)
            val capturedIntent = currentActivityIntent
            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                TravelProposalTheme(
                    themeSetting = currentThemeSetting,
                    dynamicColor = false
                ) {
                    AppContent(
                        themeViewModel = themeViewModel,
                        activityIntent = capturedIntent,
                        placesClient = placesClient,
                        onAuthDeterminationComplete = {
                            splashScreenviewModel.setLoadingCompleted()
                        },
                        onUserAuthenticatedAndNavigatedToMainHub = {
                            lifecycleScope.launch {
                                delay(NOTIFICATION_PERMISSION_REQUEST_DELAY_MS)
                                askNotificationPermission()
                            }
                        }
                    )

                    if (showNotificationRationaleDialog) {
                        NotificationRationaleDialog(
                            onDismiss = {
                                showNotificationRationaleDialog = false
                                Toast.makeText(this, "Notifications remain disabled.", Toast.LENGTH_SHORT).show()
                            },
                            onConfirm = {
                                showNotificationRationaleDialog = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }

                    if (showPermissionDeniedPermanentlyDialog) {
                        PermissionDeniedPermanentlyDialog(
                            onDismiss = {
                                showPermissionDeniedPermanentlyDialog = false
                            },
                            onOpenSettings = {
                                showPermissionDeniedPermanentlyDialog = false
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                    startActivity(this)
                                }
                            }
                        )
                    }

                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentActivityIntent = intent
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    themeViewModel: ThemeViewModel,
    activityIntent: Intent?,
    placesClient: PlacesClient,
    onAuthDeterminationComplete: () -> Unit,
    onUserAuthenticatedAndNavigatedToMainHub: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Lock screen on non-tablet devices
    if(!shouldUseTabletLayout())
        LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val userProfileRepo = UserRepository()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userProfileRepo, context)
    )
    val authState by authViewModel.authState.collectAsState()

    var startDestination by remember { mutableStateOf<String?>(null) }
    var authDeterminationLatch by remember { mutableStateOf(false) }

    val travelProposalRepo = TravelProposalRepository(context)
    val travelApplicationRepo = TravelApplicationRepository()
    val travelReviewRepo = TravelReviewRepository()
    val userReviewRepo = UserReviewRepository()
    val notificationRepo = NotificationRepository()
    val chatRepo = ChatRepository()

    val travelProposalViewModel: TravelProposalViewModel = viewModel(
        factory = TravelProposalViewModelFactory(travelProposalRepo)
    )

    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(userProfileRepo)
    )

    val travelApplicationViewModel: TravelApplicationViewModel = viewModel(
        factory = TravelApplicationViewModelFactory(travelApplicationRepo, travelProposalRepo)
    )

    val notificationsViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(notificationRepo)
    )

    val travelReviewViewModel: TravelReviewViewModel = viewModel(
        factory = TravelReviewViewModelFactory(travelReviewRepo)
    )

    val userReviewViewModel: UserReviewViewModel = viewModel(
        factory = UserReviewViewModelFactory(userReviewRepo, userProfileRepo)
    )

    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatRepo)
    )

    val topBarViewModel: TopBarViewModel = viewModel()
    val currentTopBarConfig by topBarViewModel.config.collectAsState()

    var currentTab by rememberSaveable { mutableStateOf(BottomTab.Explore) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry) {
        val route = backStackEntry?.destination?.route
        getTabForMainRoute(route)?.let { mainScreenTab ->
            currentTab = mainScreenTab
        }
    }

    LaunchedEffect(authState) {
        if (!authDeterminationLatch) {
            val currentAuthState = authState
            if (currentAuthState !is AuthState.Initializing && currentAuthState !is AuthState.Loading) {
                startDestination = when (currentAuthState) {
                    is AuthState.Authenticated -> AppRoutes.TRAVEL_PROPOSAL_LIST
                    else -> AppRoutes.LOGIN
                }
                onAuthDeterminationComplete()
                authDeterminationLatch = true
            }
        }
    }

    val currentCollectedAuthState = authState
    val currentUser = (currentCollectedAuthState as? AuthState.Authenticated)?.user

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            userProfileViewModel.selectUserProfile(currentUser.uid)
        }
    }

    if (startDestination != null) {
        val firebaseAuth = FirebaseAuth.getInstance()

        DisposableEffect(firebaseAuth, authViewModel) {
            val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                if (auth.currentUser == null && authViewModel.authState.value != AuthState.Unauthenticated) {
                    authViewModel.resetLoginState()
                } else if (auth.currentUser != null &&
                    (authViewModel.authState.value == AuthState.Unauthenticated ||
                            authViewModel.authState.value is AuthState.Error ||
                            authViewModel.authState.value == AuthState.Initializing)) {
                    authViewModel.checkIfUserIsAuthenticated()
                }
            }
            firebaseAuth.addAuthStateListener(authStateListener)
            onDispose {
                firebaseAuth.removeAuthStateListener(authStateListener)
            }
        }

        LaunchedEffect(authState, navController, startDestination) {
            if (startDestination == null) return@LaunchedEffect

            when (val state = authState) {
                is AuthState.Authenticated -> {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    if (currentRoute == AppRoutes.LOGIN ||
                        currentRoute == AppRoutes.SIGNUP ||
                        currentRoute?.startsWith(AppRoutes.INITIAL_PROFILE_SETUP) == true) {
                        navController.navigate(AppRoutes.TRAVEL_PROPOSAL_LIST) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (currentRoute == null && navController.graph.startDestinationRoute == AppRoutes.LOGIN) {
                        navController.navigate(AppRoutes.TRAVEL_PROPOSAL_LIST) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    }
                }
                is AuthState.ProfileSetupRequired -> {
                    navController.navigate(AppRoutes.initialProfileSetup(state.user.uid, state.user.email ?: "unknown@example.com")) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is AuthState.EmailVerificationRequired, AuthState.Unauthenticated, is AuthState.LoggedOut -> {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    if (currentRoute != AppRoutes.LOGIN &&
                        currentRoute != AppRoutes.SIGNUP &&
                        currentRoute != AppRoutes.RESET_PASSWORD) {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                is AuthState.AccountCollisionDetected -> { }
                is AuthState.Loading, is AuthState.Initializing -> { /* Splash screen */ }
                is AuthState.Error -> { /* Error state */ }
            }
        }

        var showAccountCollisionDialog by rememberSaveable { mutableStateOf(false) }
        var collisionDetails by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }

        LaunchedEffect(authState) {
            if (authState is AuthState.AccountCollisionDetected) {
                collisionDetails = Pair((authState as AuthState.AccountCollisionDetected).email, (authState as AuthState.AccountCollisionDetected).attemptedPasswordForLinking)
                showAccountCollisionDialog = true
            } else {
                if(showAccountCollisionDialog && authState !is AuthState.Loading) {
                    showAccountCollisionDialog = false
                }
            }
        }

        if (showAccountCollisionDialog && collisionDetails != null) {
            AlertDialog(
                onDismissRequest = {
                    showAccountCollisionDialog = false
                    authViewModel.resetLoginState()
                    collisionDetails = null
                },
                title = { Text("Account Exists") },
                text = {
                    Text(
                        "An account with the email '${collisionDetails!!.first}' already exists. " +
                                "Would you like to sign in with Google to link " +
                                "the password you entered to that Google account?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (collisionDetails != null) {
                                authViewModel.initiateGoogleSignInForLinking(
                                    context as Activity,
                                    collisionDetails!!.first,
                                    collisionDetails!!.second
                                )
                            }
                            showAccountCollisionDialog = false
                            collisionDetails = null
                        }
                    ) {
                        Text("Sign in with Google")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showAccountCollisionDialog = false
                            authViewModel.resetLoginState()
                            if (navController.currentBackStackEntry?.destination?.route == AppRoutes.SIGNUP) {
                                navController.navigate(AppRoutes.LOGIN) { popUpTo(AppRoutes.SIGNUP) { inclusive = true } }
                            }
                            collisionDetails = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        LaunchedEffect(activityIntent, navController) {
            activityIntent?.let { intent ->
                if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
                    Log.d("AppContent", "Handling deep link from activityIntent: ${intent.dataString}")
                    navController.handleDeepLink(intent)
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = remember { SnackbarHostState() }) },
            topBar = {
                if (currentTopBarConfig.isVisible && authState is AuthState.Authenticated) {
                    Column {
                        CenterAlignedTopAppBar(
                            title = { Text(currentTopBarConfig.title) },
                            navigationIcon = currentTopBarConfig.navigationIcon ?: {},
                            actions = { currentTopBarConfig.actions?.invoke(this) },
                            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }
                }
            },
            floatingActionButton = {
                if (authState is AuthState.Authenticated) {
                    currentTopBarConfig.floatingActionButton?.invoke()
                }
            },
            bottomBar = {
                if (!WindowInsets.isImeVisible && authState is AuthState.Authenticated &&
                    currentUser != null &&
                    (navController.currentDestination?.route?.startsWith(AppRoutes.CHAT_ROOM.substringBefore("/{")) == false)) {
                    BottomNavigationBar(
                        selectedTab = currentTab,
                        onTabSelected = { currentTab = it },
                        navController = navController,
                        currentUserId = currentUser.uid
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = startDestination!!
            ) {
                // Login route
                composable(AppRoutes.LOGIN) {
                    AuthLoginScreen(
                        authViewModel = authViewModel,
                        onNavigateToResetPassword = {
                            navController.navigate(AppRoutes.RESET_PASSWORD){
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = {
                            navController.navigate(AppRoutes.SIGNUP) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                // Signup route
                composable(AppRoutes.SIGNUP) {
                    AuthSignupScreen(
                        authViewModel = authViewModel,
                        onNavigateToLogin = {
                            navController.navigate(AppRoutes.LOGIN) {
                                popUpTo(AppRoutes.SIGNUP) { inclusive = true }
                            }
                        }
                    )
                }

                composable(AppRoutes.RESET_PASSWORD) {
                    AuthResetPasswordScreen(
                        authViewModel = authViewModel,
                        onNavigateToLogin = {
                            navController.navigate(AppRoutes.LOGIN) {
                                popUpTo(AppRoutes.RESET_PASSWORD) { inclusive = true }
                            }
                        }
                    )
                }

                // Initial setup profile route
                composable(
                    route = AppRoutes.INITIAL_PROFILE_SETUP + "/{userId}/{email}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("email") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    val email = backStackEntry.arguments?.getString("email") ?: ""

                    if (userId.isNotEmpty()) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val initialProfileSetupViewModel: UserProfileInitialSetupViewModel = viewModel(
                            factory = UserProfileInitialSetupFactory(userProfileRepo, userId, email)
                        )
                        UserProfileInitialSetupScreen(
                            viewModel = initialProfileSetupViewModel,
                            onProfileSavedSuccessfully = {
                                if (currentUser != null && currentUser.uid == userId) {
                                    authViewModel.handleProfileSetupCompleted(currentUser)
                                } else {
                                    val authStateValue = authViewModel.authState.value
                                    if (authStateValue is AuthState.ProfileSetupRequired && authStateValue.user.uid == userId) {
                                        authViewModel.handleProfileSetupCompleted(authStateValue.user)
                                    } else {
                                        authViewModel.refreshAuthState()
                                    }
                                }
                            },
                            onSetupCancelled = {
                                if (navController.currentDestination?.route?.startsWith(AppRoutes.INITIAL_PROFILE_SETUP.substringBefore("/{")) == true) {
                                    navController.navigate(AppRoutes.LOGIN) {
                                        popUpTo(navController.graph.findNode(AppRoutes.INITIAL_PROFILE_SETUP + "/{userId}/{email}")!!.id) { inclusive = true }
                                    }
                                }
                                authViewModel.signOut(userProfileViewModel, travelProposalViewModel, context)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.navigate(AppRoutes.LOGIN) { popUpTo(0) } }
                    }
                }

                composable(AppRoutes.NOTIFICATIONS) {
                    currentUser?.let {
                        NotificationScreen(
                            modifier = Modifier.padding(innerPadding),
                            currentUserId = currentUser.uid,
                            topBarViewModel = topBarViewModel,
                            notificationsViewModel = notificationsViewModel,
                            onNavigateToProposal = { proposalId ->
                                navController.navigate(AppRoutes.travelProposalInfo(proposalId))
                            },
                            onNavigateToTravelReviews = { travelId ->
                                navController.navigate(AppRoutes.reviewViewAllScreen(travelId))
                            },
                            onNavigateToUserReviewsList = { userId ->
                                navController.navigate(AppRoutes.userReviewsViewAllScreen(userId))
                            },
                            onNavigateToManageTravelApplications = { travelId ->
                                navController.navigate(AppRoutes.manageApplications(travelId))
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(route = AppRoutes.SETTINGS) {
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        topBarViewModel = topBarViewModel,
                        themeViewModel = themeViewModel,
                        authViewModel = authViewModel,
                        onNavigateToChangePassword = { navController.navigate(AppRoutes.CHANGE_PASSWORD) },
                        onNavigateToDeleteAccount = { navController.navigate(AppRoutes.DELETE_ACCOUNT) },
                        onNavigateToEditAccount = { navController.navigate(AppRoutes.EDIT_PROFILE) },
                        onLogout = { authViewModel.signOut(userProfileViewModel, travelProposalViewModel, context) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(route = AppRoutes.CHANGE_PASSWORD) {
                    ChangePasswordScreen(
                        modifier = Modifier.padding(innerPadding),
                        topBarViewModel = topBarViewModel,
                        authViewModel = authViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(route = AppRoutes.DELETE_ACCOUNT) {
                    DeleteAccountScreen(
                        modifier = Modifier.padding(innerPadding),
                        topBarViewModel = topBarViewModel,
                        authViewModel = authViewModel,
                        userProfileViewModel = userProfileViewModel,
                        travelProposalViewModel = travelProposalViewModel,
                        onAccountDeletedSuccessfully = { navController.navigate(AppRoutes.LOGIN) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(route = AppRoutes.MANAGE_PASSKEYS) {
                    ManagePasskeyScreen(
                        modifier = Modifier.padding(innerPadding),
                        topBarViewModel = topBarViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = AppRoutes.REVIEW_VIEW_ALL,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "myapp://travelsharingapp.example.com/reviewViewAll/{proposalId}" })
                ) { backStackEntry ->
                    val proposalId = backStackEntry.arguments?.getString("proposalId") ?: return@composable
                    currentUser?.let {
                        TravelReviewViewAllScreen(
                            modifier = Modifier.padding(innerPadding),
                            userId = currentUser.uid,
                            proposalId = proposalId,
                            travelProposalViewModel = travelProposalViewModel,
                            reviewViewModel = travelReviewViewModel,
                            topBarViewModel = topBarViewModel,
                            onBack = { navController.popBackStack() },
                            onEditReview = {
                                navController.navigate(
                                    AppRoutes.addReview(proposalId, true)
                                )
                            },
                            onAddReview = {
                                navController.navigate(AppRoutes.addReview(proposalId))
                            },
                            onDeleteReview = { reviewId ->
                                travelReviewViewModel.deleteReview(reviewId)
                            },
                            userProfileViewModel = userProfileViewModel,
                            onNavigateToUserProfileInfo = { userId ->
                                navController.navigate(AppRoutes.userProfile(userId, isOwnProfile = false))
                            }
                        )
                    }
                }

                // Add Review Screen
                composable(
                    AppRoutes.ADD_REVIEW,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                ) {
                    val proposalId = it.arguments?.getString("proposalId") ?: return@composable

                    TravelReviewAddNewScreen(
                        modifier = Modifier.padding(innerPadding),
                        proposalId = proposalId,
                        userProfileViewModel = userProfileViewModel,
                        reviewViewModel = travelReviewViewModel,
                        topBarViewModel = topBarViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 1. Travel Proposal List (Home)
                composable(AppRoutes.TRAVEL_PROPOSAL_LIST) {
                    currentUser?.let {
                        LaunchedEffect(Unit) {
                            onUserAuthenticatedAndNavigatedToMainHub()
                        }

                        LaunchedEffect(currentUser.uid) {
                            travelApplicationViewModel.startRealtimeUpdatesForUser(currentUser.uid)
                        }

                        TravelProposalListScreen(
                            modifier = Modifier.padding(innerPadding),
                            userId = currentUser.uid,
                            applicationViewModel = travelApplicationViewModel,
                            userViewModel = userProfileViewModel,
                            proposalViewModel = travelProposalViewModel,
                            topBarViewModel = topBarViewModel,
                            onNavigateToChat = {
                                navController.navigate(AppRoutes.CHAT_LIST)
                            },
                            onNavigateToTravelProposalInfo = { proposalId ->
                                navController.navigate(AppRoutes.travelProposalInfo(proposalId))
                            },
                            onNavigateToTravelProposalEdit = { proposalId ->
                                navController.navigate(AppRoutes.travelProposalEdit(proposalId))
                            }
                        )
                    }
                }

                //Travel Proposal Info
                composable(
                    AppRoutes.TRAVEL_PROPOSAL_INFO,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "myapp://travelsharingapp.example.com/travelProposalInfo/{proposalId}" })
                ) {
                    val proposalId = it.arguments?.getString("proposalId") ?: ""
                    currentUser?.let {
                        TravelProposalInfoScreen(
                            modifier = Modifier.padding(innerPadding),
                            proposalId = proposalId,
                            userId = currentUser.uid,
                            userViewModel = userProfileViewModel,
                            applicationViewModel = travelApplicationViewModel,
                            proposalViewModel = travelProposalViewModel,
                            reviewViewModel = travelReviewViewModel,
                            topBarViewModel = topBarViewModel,
                            onNavigateToTravelProposalEdit = {
                                navController.navigate(AppRoutes.travelProposalEdit(proposalId))
                            },
                            onNavigateToTravelProposalDuplicate = {
                                navController.navigate(AppRoutes.travelProposalDuplicate(proposalId))
                            },
                            onNavigateToCompanionsReview = {
                                navController.navigate((AppRoutes.travelProposalUserReviews(proposalId)))
                            },
                            onNavigateToManageApplications = {
                                navController.navigate(AppRoutes.manageApplications(proposalId))
                            },
                            onNavigateToTravelProposalApply = {
                                navController.navigate(AppRoutes.travelProposalApply(proposalId))
                            },
                            onNavigateToUserProfile = { userId ->
                                navController.navigate(AppRoutes.userProfile(userId, isOwnProfile = false))
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                //Travel Proposal Edit
                composable(
                    AppRoutes.TRAVEL_PROPOSAL_EDIT,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                ) {
                    val proposalId = it.arguments?.getString("proposalId") ?: ""
                    currentUser?.let {
                        TravelProposalManageScreen(
                            modifier = Modifier.padding(innerPadding),
                            organizerId = currentUser.uid,
                            isEditingProposal = true,
                            isDuplicatingProposal = false,
                            placesClient = placesClient,
                            proposalId = proposalId,
                            userViewModel = userProfileViewModel,
                            proposalViewModel = travelProposalViewModel,
                            topBarViewModel = topBarViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                //Travel Proposal Duplicate
                composable(
                    AppRoutes.TRAVEL_PROPOSAL_DUPLICATE,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                ) {
                    val proposalId = it.arguments?.getString("proposalId") ?: ""
                    currentUser?.let {
                        TravelProposalManageScreen(
                            modifier = Modifier.padding(innerPadding),
                            isEditingProposal = false,
                            isDuplicatingProposal = true,
                            placesClient = placesClient,
                            organizerId = currentUser.uid,
                            proposalId = proposalId,
                            userViewModel = userProfileViewModel,
                            proposalViewModel = travelProposalViewModel,
                            topBarViewModel = topBarViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                //Travel Proposal New
                composable(AppRoutes.TRAVEL_PROPOSAL_NEW) {
                    currentUser?.let {
                        TravelProposalManageScreen(
                            modifier = Modifier.padding(innerPadding),
                            isEditingProposal = false,
                            isDuplicatingProposal = false,
                            placesClient = placesClient,
                            organizerId = currentUser.uid,
                            userViewModel = userProfileViewModel,
                            proposalViewModel = travelProposalViewModel,
                            topBarViewModel = topBarViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                //Owned Travel Proposals
                composable(AppRoutes.TRAVEL_PROPOSAL_OWN) {
                    currentUser?.let {
                        TravelProposalOwnedListScreen(
                            modifier = Modifier.padding(innerPadding),
                            userId = currentUser.uid,
                            viewModel = travelProposalViewModel,
                            topBarViewModel = topBarViewModel,
                            navController = navController
                        )
                    }
                }

                //Manage Applications
                composable(
                    AppRoutes.MANAGE_APPLICATIONS,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "myapp://travelsharingapp.example.com/manageApplications/{proposalId}" })
                ) {
                    val proposalId = it.arguments?.getString("proposalId") ?: ""
                    ApplicationManageAllScreen(
                        modifier = Modifier.padding(innerPadding),
                        proposalId = proposalId,
                        userProfileViewModel = userProfileViewModel,
                        proposalViewModel = travelProposalViewModel,
                        applicationViewModel = travelApplicationViewModel,
                        topBarViewModel = topBarViewModel,
                        onNavigateToUserProfileInfo = { userId ->
                            navController.navigate(AppRoutes.userProfile(userId, isOwnProfile = false))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                //User Profile
                composable(
                    AppRoutes.USER_PROFILE,
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("isOwnProfile") { type = NavType.BoolType }
                    )
                ) {
                    val userId = it.arguments?.getString("userId") ?: ""
                    val isOwnProfile = it.arguments?.getBoolean("isOwnProfile") != false
                    UserProfileScreen(
                        modifier = Modifier.padding(innerPadding),
                        userId = userId,
                        isOwnProfile = isOwnProfile,
                        userViewModel = userProfileViewModel,
                        userReviewViewModel = userReviewViewModel,
                        topBarViewModel = topBarViewModel,
                        onNavigateToAllUserReviews = { navController.navigate(AppRoutes.userReviewsViewAllScreen(userId)) },
                        onNavigateToUserProfileInfo = { userId ->
                            navController.navigate(AppRoutes.userProfile(userId, isOwnProfile = false))
                        },
                        onNavigateToNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                        onNavigateToSettings = { navController.navigate(AppRoutes.SETTINGS) }
                    )
                }

                //Edit Profile
                composable(AppRoutes.EDIT_PROFILE) {
                    currentUser?.let {
                        UserProfileEditScreen(
                            modifier = Modifier.padding(innerPadding),
                            userViewModel = userProfileViewModel,
                            topBarViewModel = topBarViewModel,
                            onBack = { if (userProfileViewModel.saveProfile()) navController.popBackStack() }
                        )
                    }
                }

                //Joined Travel Proposals
                composable(AppRoutes.TRAVEL_PROPOSAL_JOINED) {
                    currentUser?.let {
                        TravelProposalJoinedScreen(
                            modifier = Modifier.padding(innerPadding),
                            userId = currentUser.uid,
                            travelProposalViewModel = travelProposalViewModel,
                            travelApplicationViewModel = travelApplicationViewModel,
                            topBarViewModel = topBarViewModel,
                            onNavigateToReviewPage = { proposalId ->
                                navController.navigate(
                                    AppRoutes.reviewViewAllScreen(proposalId)
                                )
                            },
                            onNavigateToProposalInfo = { proposalId ->
                                navController.navigate(
                                    AppRoutes.travelProposalInfo(proposalId)
                                )
                            },
                            onNavigateToChat = {
                                navController.navigate(AppRoutes.CHAT_LIST)
                            }
                        )
                    }
                }

                // Apply Screen for a Travel Proposal
                composable(
                    AppRoutes.TRAVEL_PROPOSAL_APPLY,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                ) {
                    val proposalId = it.arguments?.getString("proposalId") ?: ""
                    ApplicationAddNewScreen(
                        modifier = Modifier.padding(innerPadding),
                        proposalId = proposalId,
                        userViewModel = userProfileViewModel,
                        travelProposalViewModel = travelProposalViewModel,
                        applicationViewModel = travelApplicationViewModel,
                        topBarViewModel = topBarViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = AppRoutes.USER_REVIEWS_VIEW_ALL,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "myapp://travelsharingapp.example.com/userReviews/{userId}" })
                ) {
                    val userId = it.arguments?.getString("userId") ?: ""
                    UserReviewListScreen(
                        modifier = Modifier.padding(innerPadding),
                        userId = userId,
                        userProfileViewModel = userProfileViewModel,
                        userReviewViewModel = userReviewViewModel,
                        topBarViewModel = topBarViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToUserProfileInfo = { userId ->
                            navController.navigate(AppRoutes.userProfile(userId, isOwnProfile = false))
                        }
                    )
                }

                composable(
                    route = AppRoutes.TRAVEL_PROPOSAL_USER_REVIEWS,
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                ) {
                    val proposalId = it.arguments?.getString("proposalId") ?: return@composable
                    currentUser?.let {
                        UserReviewAllScreen(
                            modifier = Modifier.padding(innerPadding),
                            userId = currentUser.uid,
                            proposalId = proposalId,
                            travelProposalViewModel = travelProposalViewModel,
                            applicationViewModel = travelApplicationViewModel,
                            userProfileViewModel = userProfileViewModel,
                            userReviewViewModel = userReviewViewModel,
                            topBarViewModel = topBarViewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToUserProfileInfo = { userId ->
                                navController.navigate(AppRoutes.userProfile(userId, isOwnProfile = false))
                            }
                        )
                    }
                }

                //Chat
                composable(AppRoutes.CHAT_LIST) {
                    if (currentUser != null) {
                        ChatListScreen(
                            modifier = Modifier.padding(innerPadding),
                            userId = currentUser.uid,
                            travelProposalViewModel = travelProposalViewModel,
                            travelApplicationViewModel = travelApplicationViewModel,
                            onNavigateToChat = { proposalId ->
                                navController.navigate("${AppRoutes.CHAT_ROOM}/$proposalId")
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            topBarViewModel = topBarViewModel
                        )
                    }
                }

                composable("${AppRoutes.CHAT_ROOM}/{proposalId}",
                    arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val proposalId = backStackEntry.arguments?.getString("proposalId") ?: return@composable

                    if (currentUser != null) {
                        ChatRoomScreen(
                            modifier = Modifier.padding(innerPadding),
                            proposalId = proposalId,
                            userId = currentUser.uid,
                            userName = currentUser.displayName ?: "Anonymous",
                            chatViewModel = chatViewModel,
                            topBarViewModel = topBarViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

            }
        }
    } else {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background))
    }
}

fun getTabForMainRoute(route: String?): BottomTab? {
    return when {
        route == AppRoutes.TRAVEL_PROPOSAL_LIST -> BottomTab.Explore
        route == AppRoutes.TRAVEL_PROPOSAL_OWN -> BottomTab.MyTrips
        route == AppRoutes.TRAVEL_PROPOSAL_NEW -> BottomTab.Create
        route == AppRoutes.TRAVEL_PROPOSAL_JOINED -> BottomTab.Joined
        route?.substringBefore("/") == "userProfile" -> BottomTab.Profile
        else -> null
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    navController: NavHostController,
    currentUserId: String
) {
    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        BottomTab.entries.forEach { tab ->

            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = {
                    onTabSelected(tab)

                    val route = when (tab) {
                        BottomTab.Explore -> AppRoutes.TRAVEL_PROPOSAL_LIST
                        BottomTab.MyTrips -> AppRoutes.TRAVEL_PROPOSAL_OWN
                        BottomTab.Create -> AppRoutes.TRAVEL_PROPOSAL_NEW
                        BottomTab.Joined -> AppRoutes.TRAVEL_PROPOSAL_JOINED
                        BottomTab.Profile -> AppRoutes.userProfile(currentUserId, isOwnProfile = true)
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            BottomTab.Explore -> Icons.Default.Search
                            BottomTab.MyTrips -> Icons.AutoMirrored.Filled.List
                            BottomTab.Create -> Icons.Default.Add
                            BottomTab.Joined -> Icons.Default.CheckCircle
                            BottomTab.Profile -> Icons.Default.Person
                        },
                        contentDescription = tab.name
                    )
                },
                label = { Text(tab.name) }
            )
        }
    }
}