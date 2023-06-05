package io.github.lumyuan.turingbox.ui.theme

import android.content.Context
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.gyf.immersionbar.ImmersionBar
import io.github.lumyuan.turingbox.common.Settings
import io.github.lumyuan.turingbox.common.util.SharedPreferencesUtil

/**
 * 获取当前主题
 */
@Composable
fun appTheme(
    themeType: AppTheme,
    context: Context = LocalContext.current
): ColorScheme =
    when (themeType) {
        AppTheme.DEFAULT -> defaultTheme()
        AppTheme.DYNAMIC_COLOR -> dynamicColorTheme(context)
        AppTheme.GREEN -> greenTheme()
        AppTheme.RED -> redTheme()
        AppTheme.PINK -> pinkTheme()
        AppTheme.BLUE -> blueTheme()
        AppTheme.CYAN -> cyanTheme()
        AppTheme.ORANGE -> orangeTheme()
        AppTheme.PURPLE -> purpleTheme()
        AppTheme.BROWN -> brownTheme()
        AppTheme.GRAY -> grayTheme()
    }

//全局主题状态
private val themeTypeState: MutableState<AppTheme> by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf(AppTheme.DEFAULT)
}

private val isDarkTheme by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf(DarkTheme.SYSTEM)
}

@Composable
fun isDark(): Boolean {
    return when (getDarkTheme()) {
        DarkTheme.LIGHT -> false
        DarkTheme.NIGHT -> true
        DarkTheme.SYSTEM -> isSystemInDarkTheme()
    }
}


@Composable
private fun InitTheme() {
    val theme = try {
        AppTheme.valueOf(
            SharedPreferencesUtil.load(Settings.APP_THEME) ?: AppTheme.DEFAULT.toString()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        AppTheme.DEFAULT
    }
    val darkTheme = try {
        DarkTheme.valueOf(SharedPreferencesUtil.load(Settings.DARK_THEME) ?: DarkTheme.SYSTEM.toString())
    }catch (e: Exception) {
        e.printStackTrace()
        DarkTheme.SYSTEM
    }
    setAppTheme(themeType = theme)
    setDarkTheme(darkTheme)
}

/**
 * 设置主题
 */
fun setAppTheme(themeType: AppTheme) {
    themeTypeState.value = themeType
    SharedPreferencesUtil.save(Settings.APP_THEME, themeType.toString())
}

fun setDarkTheme(themeType: DarkTheme) {
    isDarkTheme.value = themeType
    SharedPreferencesUtil.save(Settings.DARK_THEME, themeType.toString())
}

/**
 * 获取当前主题
 */
fun getAppTheme(): AppTheme = themeTypeState.value

fun getDarkTheme(): DarkTheme = isDarkTheme.value

/**
 * 根Context
 */
@Composable
fun TuringBoxTheme(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    //初始化主题
    InitTheme()

    //获取当前主题

    println("TuringBoxTheme")
    val targetTheme = appTheme(themeType = themeTypeState.value)

    //沉浸式状态栏

    TranslationBarComponent()

    MaterialTheme(
        colorScheme = themeAnimation(targetTheme = targetTheme),
        typography = Typography
    ) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}

@Composable
fun TranslationBarComponent() {
    val activity = LocalView.current.context as ComponentActivity
    translationBar(activity, isDark())
}

fun translationBar(activity: ComponentActivity, darkTheme: Boolean) {
    ImmersionBar.with(activity)
        .transparentStatusBar()
        .transparentNavigationBar()
        .statusBarDarkFont(!darkTheme)
        .navigationBarDarkIcon(!darkTheme)
        .keyboardEnable(true)
        .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        .init()
}
