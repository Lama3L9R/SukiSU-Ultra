package zako.zako.zako.ui.webui

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import android.os.Build
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import zako.zako.zako.ui.theme.ThemeConfig
import zako.zako.zako.ui.theme.Typography
import zako.zako.zako.ui.theme.loadCustomBackground

// 提供界面类型的本地组合
val LocalIsSecondaryScreen = staticCompositionLocalOf { false }

/**
 * WebUI专用主题配置
 */
@Composable
fun WebUIXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    isSecondaryScreen: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!ThemeConfig.backgroundImageLoaded && !ThemeConfig.preventBackgroundRefresh) {
            context.loadCustomBackground()
            ThemeConfig.backgroundImageLoaded = false
        }
    }

    // 更新二级界面状态
    LaunchedEffect(isSecondaryScreen) {
        WebViewInterface.updateSecondaryScreenState(isSecondaryScreen)
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context).let { scheme ->
                    if (isSecondaryScreen) {
                        scheme.copy(
                            background = scheme.surfaceContainerHighest,
                            surface = scheme.surfaceContainerHighest
                        )
                    } else {
                        scheme.copy(
                            background = Color.Transparent,
                            surface = Color.Transparent
                        )
                    }
                }
            } else {
                dynamicLightColorScheme(context).let { scheme ->
                    if (isSecondaryScreen) {
                        scheme.copy(
                            background = scheme.surfaceContainerHighest,
                            surface = scheme.surfaceContainerHighest
                        )
                    } else {
                        scheme.copy(
                            background = Color.Transparent,
                            surface = Color.Transparent
                        )
                    }
                }
            }
        }
        darkTheme -> {
            if (isSecondaryScreen) {
                darkColorScheme().copy(
                    background = MaterialTheme.colorScheme.surfaceContainerHighest,
                    surface = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            } else {
                darkColorScheme().copy(
                    background = Color.Transparent,
                    surface = Color.Transparent
                )
            }
        }
        else -> {
            if (isSecondaryScreen) {
                lightColorScheme().copy(
                    background = MaterialTheme.colorScheme.surfaceContainerHighest,
                    surface = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            } else {
                lightColorScheme().copy(
                    background = Color.Transparent,
                    surface = Color.Transparent
                )
            }
        }
    }

    ConfigureSystemBars(darkTheme)

    val backgroundUri = remember { mutableStateOf(ThemeConfig.customBackgroundUri) }

    LaunchedEffect(ThemeConfig.customBackgroundUri) {
        backgroundUri.value = ThemeConfig.customBackgroundUri
    }
    val bgImagePainter = backgroundUri.value?.let {
        rememberAsyncImagePainter(
            model = it,
            onError = {
                ThemeConfig.backgroundImageLoaded = false
            },
            onSuccess = {
                ThemeConfig.backgroundImageLoaded = true
                ThemeConfig.isThemeChanging = false
            }
        )
    }

    // 背景透明度动画
    val transition = updateTransition(
        targetState = ThemeConfig.backgroundImageLoaded,
        label = "bgTransition"
    )

    val bgAlpha by transition.animateFloat(
        label = "bgAlpha",
        transitionSpec = {
            spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            )
        }
    ) { loaded -> if (loaded) 1f else 0f }
    CompositionLocalProvider(LocalIsSecondaryScreen provides isSecondaryScreen) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
        ) {
            if (isSecondaryScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    content()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(-2f)
                            .background(if (darkTheme) Color.Black else Color.White)
                    )

                    backgroundUri.value?.let { uri ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(-1f)
                                .alpha(bgAlpha)
                        ) {
                            bgImagePainter?.let { painter ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .paint(
                                            painter = painter,
                                            contentScale = ContentScale.Crop
                                        )
                                        .graphicsLayer {
                                            alpha = (painter.state as? AsyncImagePainter.State.Success)?.let { 1f } ?: 0f
                                        }
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (darkTheme) Color.Black.copy(alpha = 0.6f)
                                        else Color.White.copy(alpha = 0.1f)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                if (darkTheme) Color.Black.copy(alpha = 0.5f)
                                                else Color.Black.copy(alpha = 0.2f)
                                            ),
                                            radius = 1200f
                                        )
                                    )
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * 配置WebUI的系统栏样式
 */
@Composable
private fun ConfigureSystemBars(
    darkMode: Boolean,
    statusBarScrim: Color = Color.Transparent,
    navigationBarScrim: Color = Color.Transparent
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                statusBarScrim.toArgb(),
                statusBarScrim.toArgb()
            ) { darkMode },
            navigationBarStyle = when {
                darkMode -> SystemBarStyle.dark(
                    navigationBarScrim.toArgb()
                )
                else -> SystemBarStyle.light(
                    navigationBarScrim.toArgb(),
                    navigationBarScrim.toArgb()
                )
            }
        )
    }
}