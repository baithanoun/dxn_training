package com.dxn.training.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dxn.training.R
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.viewmodel.HomeViewModel
import com.dxn.training.ui.viewmodel.ProfileViewModel
import com.dxn.training.ui.viewmodel.TeamViewModel

private enum class MainTab { HOME, LEARN, TEAM, PROFILE }

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    teamViewModel: TeamViewModel,
    profileViewModel: ProfileViewModel,
    onLevelClick: (String) -> Unit,
    onAdminClick: () -> Unit,
    onCertificateClick: (String) -> Unit,
    onLogout: () -> Unit,
    isLeader: Boolean = false
) {
    // تبويب الفريق للقادة فقط (السيرفر يمنع المتدربين من api/leader/team).
    val tabs = buildList {
        add(MainTab.HOME)
        add(MainTab.LEARN)
        if (isLeader) add(MainTab.TEAM)
        add(MainTab.PROFILE)
    }
    var selectedTab by remember(isLeader) { mutableIntStateOf(0) }
    val titles = tabs.map { tab ->
        when (tab) {
            MainTab.HOME -> stringResource(R.string.title_home)
            MainTab.LEARN -> stringResource(R.string.title_learn)
            MainTab.TEAM -> stringResource(R.string.title_team)
            MainTab.PROFILE -> stringResource(R.string.title_profile)
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets(0),
                containerColor = Surface,
                tonalElevation = 4.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val title = titles[index]
                    val icon = when (tab) {
                        MainTab.HOME -> Icons.Default.Home
                        MainTab.LEARN -> Icons.Default.School
                        MainTab.TEAM -> Icons.Default.Groups
                        MainTab.PROFILE -> Icons.Default.Person
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Surface,
                            selectedTextColor = BrandPlum,
                            indicatorColor = BrandPlum,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        },
        containerColor = BrandIvory
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tabs.getOrNull(selectedTab)) {
                MainTab.HOME -> HomeScreen(
                    viewModel = homeViewModel,
                    onLevelClick = onLevelClick
                )
                MainTab.LEARN -> LearnScreen(
                    viewModel = homeViewModel,
                    onLevelClick = onLevelClick
                )
                MainTab.TEAM -> TeamScreen(viewModel = teamViewModel)
                MainTab.PROFILE -> ProfileScreen(
                    viewModel = profileViewModel,
                    onAdminClick = onAdminClick,
                    onCertificateClick = onCertificateClick,
                    onLogout = onLogout
                )
                null -> Unit
            }
        }
    }
}
