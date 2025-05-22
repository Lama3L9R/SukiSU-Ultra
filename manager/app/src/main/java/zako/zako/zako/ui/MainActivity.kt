package zako.zako.zako.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import com.ramcosta.composedestinations.spec.RouteOrDirection
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import io.zako.zako.UltraToolInstall
import zako.zako.zako.Natives
import zako.zako.zako.ksuApp
import zako.zako.zako.ui.screen.BottomBarDestination
import zako.zako.zako.ui.theme.*
import zako.zako.zako.ui.theme.CardConfig.cardAlpha
import zako.zako.zako.ui.util.*
import androidx.core.content.edit
import zako.zako.zako.ui.theme.CardConfig.cardElevation
import zako.zako.zako.ui.webui.initPlatform
import java.util.Locale

class MainActivity : ComponentActivity() {
    private inner class ThemeChangeContentObserver(
        handler: Handler,
        private val onThemeChanged: () -> Unit
    ) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            onThemeChanged()
        }
    }

    // 应用保存的语言设置
    @SuppressLint("ObsoleteSdkInt")
    private fun applyLanguageSetting() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "") ?: ""

        if (languageCode.isNotEmpty()) {
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)

            val resources = resources
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "") ?: ""

        var context = newBase
        if (languageCode.isNotEmpty()) {
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            context = newBase.createConfigurationContext(config)
        }

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 确保应用正确的语言设置
        applyLanguageSetting()

        applyCustomDpi()

        // Enable edge to edge
        enableEdgeToEdge()

        window.isNavigationBarContrastEnforced = false

        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (isFirstRun) {
            ThemeConfig.preventBackgroundRefresh = false
            getSharedPreferences("theme_prefs", MODE_PRIVATE).edit {
                putBoolean("prevent_background_refresh", false)
            }
            prefs.edit { putBoolean("is_first_run", false) }
        }

        // 加载保存的背景设置
        loadThemeMode()
        loadThemeColors()
        loadDynamicColorState()
        CardConfig.load(applicationContext)

        val contentObserver = ThemeChangeContentObserver(Handler(mainLooper)) {
            runOnUiThread {
                if (!ThemeConfig.preventBackgroundRefresh) {
                    ThemeConfig.backgroundImageLoaded = false
                    loadCustomBackground()
                }
            }
        }

        contentResolver.registerContentObserver(
            android.provider.Settings.System.getUriFor("ui_night_mode"),
            false,
            contentObserver
        )

        val destroyListeners = mutableListOf<() -> Unit>()
        destroyListeners.add {
            contentResolver.unregisterContentObserver(contentObserver)
        }

        val isManager = Natives.becomeManager(ksuApp.packageName)
        if (isManager) {
            install()
            UltraToolInstall.tryToInstall()
        }

        setContent {
            KernelSUTheme {
                val navController = rememberNavController()
                val snackBarHostState = remember { SnackbarHostState() }

                // pre-init platform to faster start WebUI X activities
                LaunchedEffect(Unit) {
                    initPlatform()
                }

                Scaffold(
                    bottomBar = { BottomBar(navController) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    CompositionLocalProvider(
                        LocalSnackbarHost provides snackBarHostState
                    ) {
                        DestinationsNavHost(
                            modifier = Modifier.padding(innerPadding),
                            navGraph = NavGraphs.root as NavHostGraphSpec,
                            navController = navController,
                            defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                                override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
                                    get() = { fadeIn(animationSpec = tween(340)) }
                                override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
                                    get() = { fadeOut(animationSpec = tween(340)) }
                            }
                        )
                    }
                }
            }
        }
    }

    // 应用自定义DPI设置
    private fun applyCustomDpi() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val customDpi = prefs.getInt("app_dpi", 0)

        if (customDpi > 0) {
            try {
                val resources = resources
                val metrics = resources.displayMetrics
                metrics.density = customDpi / 160f
                @Suppress("DEPRECATION")
                metrics.scaledDensity = customDpi / 160f
                metrics.densityDpi = customDpi
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        CardConfig.save(applicationContext)
        getSharedPreferences("theme_prefs", MODE_PRIVATE).edit {
            putBoolean("prevent_background_refresh", true)
        }
        ThemeConfig.preventBackgroundRefresh = true
    }

    override fun onResume() {
        super.onResume()
        applyLanguageSetting()

        if (!ThemeConfig.backgroundImageLoaded && !ThemeConfig.preventBackgroundRefresh) {
            loadCustomBackground()
        }
    }

    private val destroyListeners = mutableListOf<() -> Unit>()

    override fun onDestroy() {
        destroyListeners.forEach { it() }
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyLanguageSetting()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(navController: NavHostController) {
    val navigator = navController.rememberDestinationsNavigator()
    val isManager = Natives.becomeManager(ksuApp.packageName)
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    val kpmVersion = getKpmVersion()
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val cardColor = MaterialTheme.colorScheme.surfaceVariant

    // 检查是否显示KPM
    val showKpmInfo = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getBoolean("show_kpm_info", true)

    NavigationBar(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
        ),
        containerColor = TopAppBarDefaults.topAppBarColors(
            containerColor = cardColor.copy(alpha = cardAlpha),
            scrolledContainerColor = containerColor.copy(alpha = cardAlpha)
        ).containerColor,
        tonalElevation = cardElevation
    ) {
        BottomBarDestination.entries.forEach { destination ->
            if (destination == BottomBarDestination.Kpm) {
                if (kpmVersion.isNotEmpty() && !kpmVersion.startsWith("Error") && showKpmInfo) {
                    if (!fullFeatured && destination.rootRequired) return@forEach
                    val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)
                    NavigationBarItem(
                        selected = isCurrentDestOnBackStack,
                        onClick = {
                            if (!isCurrentDestOnBackStack) {
                                navigator.navigate(destination.direction) {
                                    popUpTo(NavGraphs.root as RouteOrDirection) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isCurrentDestOnBackStack) {
                                    destination.iconSelected
                                } else {
                                    destination.iconNotSelected
                                },
                                contentDescription = stringResource(destination.label),
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(destination.label),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            } else {
                if (!fullFeatured && destination.rootRequired) return@forEach
                val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)
                NavigationBarItem(
                    selected = isCurrentDestOnBackStack,
                    onClick = {
                        if (!isCurrentDestOnBackStack) {
                            navigator.navigate(destination.direction) {
                                popUpTo(NavGraphs.root as RouteOrDirection) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (isCurrentDestOnBackStack) {
                                destination.iconSelected
                            } else {
                                destination.iconNotSelected
                            },
                            contentDescription = stringResource(destination.label),
                            tint = if (isCurrentDestOnBackStack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(destination.label),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
    }
}