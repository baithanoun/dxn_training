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

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    teamViewModel: TeamViewModel,
    profileViewModel: ProfileViewModel,
    onLevelClick: (String) -> Unit,
    onAdminClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.title_home),
        stringResource(R.string.title_learn),
        stringResource(R.string.title_team),
        stringResource(R.string.title_profile)
    )
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.School,
        Icons.Default.Groups,
        Icons.Default.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardSurface,
                tonalElevation = 4.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CardSurface,
                            selectedTextColor = DXNRed,
                            indicatorColor = DXNRed,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onLevelClick = onLevelClick
                )
                1 -> LearnScreen(
                    viewModel = homeViewModel,
                    onLevelClick = onLevelClick
                )
                2 -> TeamScreen(viewModel = teamViewModel)
                3 -> ProfileScreen(
                    viewModel = profileViewModel,
                    onAdminClick = onAdminClick
                )
            }
        }
    }
}
