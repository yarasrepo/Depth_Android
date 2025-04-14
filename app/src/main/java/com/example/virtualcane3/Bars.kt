package com.example.virtualcane3

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.virtualcane3.ui.theme.Black

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Bars(navController: NavController) {
    val items = listOf(
        BottomNavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        BottomNavigationItem(
            title = "Location",
            selectedIcon = Icons.Filled.LocationOn,
            unselectedIcon = Icons.Outlined.LocationOn,
        ),
        BottomNavigationItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        )
    )

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, items = items)
        }
    ) {
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Virtual Cane",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF334155),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun BottomNavigationBar(navController: NavController, items: List<BottomNavigationItem>) {
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar(
        containerColor = Color(0xFF334155),
        contentColor = Color.White
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.title.lowercase(),
                onClick = {
                    navController.navigate(item.title.lowercase()) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    ttsHelper.speak("${item.title} selected")
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.title.lowercase()) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF3EB6FF),
                    unselectedTextColor = Color.White,
                    indicatorColor = Color(0xFF3EB6FF)
                )
            )
        }
    }
}
