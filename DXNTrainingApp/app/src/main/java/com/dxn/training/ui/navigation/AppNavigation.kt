package com.dxn.training.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dxn.training.data.local.SessionStore
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import com.dxn.training.data.remote.AnswerDto
import com.dxn.training.data.remote.AppConfigResponse
import com.dxn.training.data.remote.EventSender
import com.dxn.training.data.repository.AppConfigRepository
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.data.repository.UserRepository
import com.dxn.training.ui.components.UpdatePrompt
import com.dxn.training.ui.screens.*
import com.dxn.training.ui.text.LocalGender
import com.dxn.training.ui.text.toGender
import com.dxn.training.ui.theme.BrandPlum
import com.dxn.training.ui.viewmodel.*
import kotlinx.coroutines.flow.first

class AuthViewModelFactory(
    private val api: ApiService,
    private val sessionStore: SessionStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(api = api, sessionStore = sessionStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HomeViewModelFactory(
    private val contentRepository: ContentRepository,
    private val userRepository: UserRepository,
    private val sessionStore: SessionStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                contentRepository = contentRepository,
                userRepository = userRepository,
                sessionStore = sessionStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val sessionStore: SessionStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                userRepository = userRepository,
                sessionStore = sessionStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CompletionViewModelFactory(
    private val contentRepository: ContentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompletionViewModel::class.java)) {
            return CompletionViewModel(contentRepository = contentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    pendingLeaderDxnId: String? = null,
    onLeaderDxnIdConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val api = ApiClient.api
    val sessionStore = remember { ApiClient.getSessionStore() ?: SessionStore(context.applicationContext) }
    val userRepository = remember { UserRepository(api) }
    val contentRepository = remember { ContentRepository.getInstance(api, sessionStore, userRepository) }
    val appConfigRepository = remember { AppConfigRepository(ApiClient.api, context.applicationContext) }
    var appConfig by remember { mutableStateOf<AppConfigResponse?>(null) }
    val completionViewModel: CompletionViewModel = viewModel(
        factory = CompletionViewModelFactory(contentRepository)
    )
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(api, sessionStore))
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(contentRepository, userRepository, sessionStore)
    )
    val teamViewModel: TeamViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(userRepository, sessionStore)
    )

    LaunchedEffect(Unit) {
        appConfig = appConfigRepository.load()
    }

    val session by sessionStore.session.collectAsState(initial = null)
    val profile by userRepository.user.collectAsState()
    var sessionChecked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val currentSession = sessionStore.session.first()
        sessionChecked = true
        // حدث فتح التطبيق مرة واحدة عند بدء التطبيق بحالة تسجيل دخول صالحة.
        if (currentSession != null) {
            EventSender.send("app_opened")
        }
    }
    LaunchedEffect(Unit) {
        ApiClient.logoutEvents.collect {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (!sessionChecked) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrandPlum)
        }
        return
    }

    // الملف الشخصي المحدث أولاً، ثم الجنس المحفوظ في الجلسة (الحسابات القديمة بلا جنس → المذكر).
    val profileGender = profile.gender.takeIf { profile.id == session?.userId }
    val currentGender = (profileGender ?: session?.gender).toGender()
    CompositionLocalProvider(LocalGender provides currentGender) {
        UpdatePrompt(appConfig)
        NavHost(
            navController = navController,
            startDestination = if (session != null) Screen.Main.route else Screen.Splash.route
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigate = {
                        navController.navigate(Screen.Onboarding.createRoute(1)) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Onboarding.route,
                arguments = listOf(navArgument("page") { type = NavType.IntType })
            ) { backStackEntry ->
                val page = backStackEntry.arguments?.getInt("page") ?: 1
                val config = appConfig
                if (config == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPlum)
                    }
                } else {
                    OnboardingScreen(
                        page = page,
                        config = config,
                        onNext = {
                            if (page < 3) {
                                navController.navigate(Screen.Onboarding.createRoute(page + 1))
                            } else {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLogin = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    initialLeaderDxnId = pendingLeaderDxnId,
                    onLeaderDxnIdConsumed = onLeaderDxnIdConsumed,
                    onRegister = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Main.route) {
                MainScreen(
                    homeViewModel = homeViewModel,
                    teamViewModel = teamViewModel,
                    profileViewModel = profileViewModel,
                    isLeader = session?.role.equals("LEADER", ignoreCase = true),
                    onLevelClick = { levelId ->
                        navController.navigate(Screen.Lesson.createRoute(levelId))
                    },
                    onAdminClick = { navController.navigate(Screen.AdminOverview.route) },
                    onCertificateClick = { sectionId ->
                        navController.navigate(Screen.Certificate.createRoute(sectionId))
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Lesson.route,
                arguments = listOf(navArgument("levelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                LessonScreen(
                    levelId = levelId,
                    onStartQuiz = {
                        val level = contentRepository.getLevel(levelId)
                        when (level?.type) {
                            "chat_battle" -> navController.navigate(Screen.ChatBattle.createRoute(levelId))
                            "match_pairs" -> navController.navigate(Screen.MatchPairs.createRoute(levelId))
                            "order_sequence" -> navController.navigate(Screen.OrderSequence.createRoute(levelId))
                            else -> navController.navigate(Screen.Quiz.createRoute(levelId))
                        }
                    },
                    onFinishLesson = {
                        completionViewModel.submit(levelId, emptyList())
                        navController.navigate(Screen.Completion.createRoute(levelId)) {
                            popUpTo(Screen.Lesson.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    contentRepository = contentRepository
                )
            }

            composable(
                route = Screen.Quiz.route,
                arguments = listOf(navArgument("levelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                QuizScreen(
                    levelId = levelId,
                    onComplete = { answers ->
                        completionViewModel.submit(levelId, answers)
                        navController.navigate(Screen.Completion.createRoute(levelId)) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    contentRepository = contentRepository
                )
            }

            composable(
                route = Screen.ChatBattle.route,
                arguments = listOf(navArgument("levelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                ChatBattleScreen(
                    levelId = levelId,
                    onComplete = { answers ->
                        completionViewModel.submit(levelId, answers)
                        navController.navigate(Screen.Completion.createRoute(levelId)) {
                            popUpTo(Screen.ChatBattle.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    contentRepository = contentRepository
                )
            }

            composable(
                route = Screen.MatchPairs.route,
                arguments = listOf(navArgument("levelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                MatchPairsScreen(
                    levelId = levelId,
                    onComplete = { answers ->
                        completionViewModel.submit(levelId, answers)
                        navController.navigate(Screen.Completion.createRoute(levelId)) {
                            popUpTo(Screen.MatchPairs.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    contentRepository = contentRepository
                )
            }

            composable(
                route = Screen.OrderSequence.route,
                arguments = listOf(navArgument("levelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                OrderSequenceScreen(
                    levelId = levelId,
                    onComplete = { answers ->
                        completionViewModel.submit(levelId, answers)
                        navController.navigate(Screen.Completion.createRoute(levelId)) {
                            popUpTo(Screen.OrderSequence.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    contentRepository = contentRepository
                )
            }

            composable(
                route = Screen.Completion.route,
                arguments = listOf(navArgument("levelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                CompletionScreen(
                    levelId = levelId,
                    completionViewModel = completionViewModel,
                    onNextLevel = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                    onReview = { navController.popBackStack(Screen.Lesson.route, false) },
                    onShowCertificate = { sectionId ->
                        navController.navigate(Screen.Certificate.createRoute(sectionId))
                    },
                    contentRepository = contentRepository,
                    appDownloadUrl = appConfig?.appDownloadUrl,
                    userName = profile.fullName
                )
            }

            composable(
                route = Screen.Certificate.route,
                arguments = listOf(navArgument("sectionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getString("sectionId") ?: return@composable
                CertificateScreen(
                    sectionId = sectionId,
                    profileViewModel = profileViewModel,
                    contentRepository = contentRepository,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminOverview.route) {
                AdminOverviewScreen(onBack = { navController.popBackStack() })
            }
        }
        }
}