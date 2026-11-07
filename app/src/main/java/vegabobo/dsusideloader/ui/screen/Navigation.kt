package vegabobo.dsusideloader.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.LocalFloatingDockPadding
import vegabobo.dsusideloader.ui.screen.about.AboutScreen
import vegabobo.dsusideloader.ui.screen.adb.AdbScreen
import vegabobo.dsusideloader.ui.screen.home.Home
import vegabobo.dsusideloader.ui.screen.images.ImageSelectionScreen
import vegabobo.dsusideloader.ui.screen.libraries.LibrariesScreen
import vegabobo.dsusideloader.ui.screen.logs.LogsScreen
import vegabobo.dsusideloader.ui.screen.settings.Settings
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isModalOpen by navigationViewModel.session.isModalOpen.collectAsStateWithLifecycle()

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dockPadding = bottomInset + 104.dp

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalFloatingDockPadding provides dockPadding) {
            NavHost(
                navController = navController,
                startDestination = Destinations.Homepage,
                modifier = Modifier.fillMaxSize(),
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
                        onImageSelected = { navigateToTab(Destinations.Homepage) },
                    )
                }
                composable(Destinations.Logs) { LogsScreen() }
                composable(Destinations.Preferences) { Settings(navigate = { navigate(it) }) }
                composable(Destinations.ADBInstallation) { AdbScreen(navigate = { navigate(it) }) }
                composable(Destinations.About) { AboutScreen(navigate = { navigate(it) }) }
                composable(Destinations.Libraries) { LibrariesScreen(navigate = { navigate(it) }) }
            }
        }

        if (!isModalOpen) {
            FloatingNavigationBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                currentRoute = currentRoute,
                onTabClick = ::navigateToTab,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingNavigationBar(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    onTabClick: (String) -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        NavigationBar(
            modifier = Modifier.clip(shape),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            bottomTabs.forEach { tab ->
                NavigationBarItem(
                    selected = isTabSelected(tab.route, currentRoute),
                    onClick = { onTabClick(tab.route) },
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
    }
}
