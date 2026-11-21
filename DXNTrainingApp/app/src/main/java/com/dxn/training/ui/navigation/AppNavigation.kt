package com.dxn.training.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.data.repository.UserRepository
import com.dxn.training.ui.screens.*
import com.dxn.training.ui.viewmodel.*

class HomeViewModelFactory(
    private val contentRepository: ContentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(contentRepository = contentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModelFactory(
    private val api: ApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository = UserRepository(api)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val api = remember { ApiClient.create() }
    val contentRepository = remember { ContentRepository.getInstance(api) }
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(contentRepository))
    val teamViewModel: TeamViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(api))

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
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
            OnboardingScreen(
                page = page,
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

        composable(Screen.Login.route) {
            LoginScreen(
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
                onLevelClick = { levelId ->
                    navController.navigate(Screen.Lesson.createRoute(levelId))
                },
                onAdminClick = { navController.navigate(Screen.AdminOverview.route) }
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
                onComplete = { score, xp ->
                    navController.navigate(Screen.Completion.createRoute(levelId, score, xp)) {
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
                onComplete = { score, xp ->
                    navController.navigate(Screen.Completion.createRoute(levelId, score, xp))
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
                onComplete = { score, xp ->
                    navController.navigate(Screen.Completion.createRoute(levelId, score, xp))
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
                onComplete = { score, xp ->
                    navController.navigate(Screen.Completion.createRoute(levelId, score, xp))
                },
                onBack = { navController.popBackStack() },
                contentRepository = contentRepository
            )
        }

        composable(
            route = Screen.Completion.route,
            arguments = listOf(
                navArgument("levelId") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
                navArgument("xp") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val xp = backStackEntry.arguments?.getInt("xp") ?: 0
            CompletionScreen(
                levelId = levelId,
                score = score,
                xp = xp,
                onNextLevel = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onReview = { navController.popBackStack(Screen.Lesson.route, false) },
                contentRepository = contentRepository
            )
        }

        composable(Screen.AdminOverview.route) {
            AdminOverviewScreen(onBack = { navController.popBackStack() })
        }
    }
}
