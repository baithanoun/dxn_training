package com.dxn.training.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding/{page}") {
        fun createRoute(page: Int) = "onboarding/$page"
    }
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Main : Screen("main")
    data object Lesson : Screen("lesson/{levelId}") {
        fun createRoute(levelId: String) = "lesson/$levelId"
    }
    data object Quiz : Screen("quiz/{levelId}") {
        fun createRoute(levelId: String) = "quiz/$levelId"
    }
    data object ChatBattle : Screen("chat/{levelId}") {
        fun createRoute(levelId: String) = "chat/$levelId"
    }
    data object MatchPairs : Screen("match/{levelId}") {
        fun createRoute(levelId: String) = "match/$levelId"
    }
    data object OrderSequence : Screen("order/{levelId}") {
        fun createRoute(levelId: String) = "order/$levelId"
    }
    data object Completion : Screen("completion/{levelId}") {
        fun createRoute(levelId: String) = "completion/$levelId"
    }
    data object Certificate : Screen("certificate/{sectionId}") {
        fun createRoute(sectionId: String) = "certificate/$sectionId"
    }
    data object AdminOverview : Screen("admin/overview")
}
