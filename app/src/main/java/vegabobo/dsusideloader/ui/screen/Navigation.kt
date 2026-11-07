package vegabobo.dsusideloader.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.screen.about.AboutScreen
import vegabobo.dsusideloader.ui.screen.adb.AdbScreen
import vegabobo.dsusideloader.ui.screen.home.Home
import vegabobo.dsusideloader.ui.screen.images.ImageSelectionScreen
import vegabobo.dsusideloader.ui.screen.libraries.LibrariesScreen
import vegabobo.dsusideloader.ui.screen.logs.LogsScreen
import vegabobo.dsusideloader.ui.screen.settings.Settings

object Destinations {
    const val Homepage = "home"
    const val Preferences = "preferences"
    const val Images = "images"
    const val Logs = "logs"
    const val ADBInstallation = "adb_installation"
    const val About = "about"
    const val Libraries = "libraries"
    const val Up = "up"
}

private data class BottomTab(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
)

private val bottomTabs = listOf(
    BottomTab(Destinations.Homepage, R.string.nav_home, Icons.Outlined.Home),
    BottomTab(Destinations.Images, R.string.nav_images, Icons.Outlined.Image),
    BottomTab(Destinations.Logs, R.string.nav_logs, Icons.Outlined.List),
    BottomTab(Destinations.Preferences, R.string.nav_settings, Icons.Outlined.Settings),
)

private fun isTabSelected(tabRoute: String, currentRoute: String?): Boolean {
    return when (tabRoute) {
        Destinations.Homepage ->
            currentRoute == null ||
                currentRoute == Destinations.Homepage ||
                currentRoute == Destinations.ADBInstallation

        Destinations.Preferences ->
            currentRoute == Destinations.Preferences ||
                currentRoute == Destinations.About ||
                currentRoute == Destinations.Libraries

        else -> currentRoute == tabRoute
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = isTabSelected(tab.route, currentRoute),
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(id = tab.label),
                            )
                        },
                        label = { Text(text = stringResource(id = tab.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Homepage,
            modifier = Modifier.padding(innerPadding),
        ) {
            fun navigate(destination: String) {
                if (destination == Destinations.Up) {
                    navController.navigateUp()
                } else {
                    navController.navigate(destination)
                }
            }

            composable(Destinations.Homepage) { Home(navigate = { navigate(it) }) }
            composable(Destinations.Images) {
                ImageSelectionScreen(
                    navigate = { navigate(it) },
                    onImageSelected = {
                        navController.navigate(Destinations.Homepage) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Destinations.Logs) { LogsScreen(navigate = { navigate(it) }) }
            composable(Destinations.Preferences) { Settings(navigate = { navigate(it) }) }
            composable(Destinations.ADBInstallation) { AdbScreen(navigate = { navigate(it) }) }
            composable(Destinations.About) { AboutScreen(navigate = { navigate(it) }) }
            composable(Destinations.Libraries) { LibrariesScreen(navigate = { navigate(it) }) }
        }
    }
}
