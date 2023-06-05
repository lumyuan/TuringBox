package io.github.lumyuan.turingbox.windows.main

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.ui.compose.setContentUI
import io.github.lumyuan.turingbox.ui.icon.NiaIcons
import io.github.lumyuan.turingbox.ui.theme.AppTheme
import io.github.lumyuan.turingbox.ui.theme.DarkTheme
import io.github.lumyuan.turingbox.ui.theme.appTheme
import io.github.lumyuan.turingbox.ui.theme.getAppTheme
import io.github.lumyuan.turingbox.ui.theme.getDarkTheme
import io.github.lumyuan.turingbox.ui.theme.setAppTheme
import io.github.lumyuan.turingbox.ui.theme.setDarkTheme
import io.github.lumyuan.turingbox.ui.widget.NiaNavigationBar
import io.github.lumyuan.turingbox.ui.widget.NiaNavigationBarItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentUI {
            ContentView()
        }
    }
}

@Stable
data class BottomItemData(
    var title: String,
    var painter: ImageVector,
    var selectPainter: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContentView() {

    val icons = listOf(
        NiaIcons.UpcomingBorder,
        NiaIcons.BookmarksBorder,
        NiaIcons.Grid3x3,
    )
    val selectedIcons = listOf(
        NiaIcons.Upcoming,
        NiaIcons.Bookmarks,
        NiaIcons.Grid3x3,
    )

    val titles = listOf(
        BottomItemData(
            stringResource(id = R.string.page_function),
            painter = icons[2],
            selectPainter = selectedIcons[2]
        ),
        BottomItemData(
            stringResource(id = R.string.page_device),
            painter = icons[0],
            selectPainter = selectedIcons[0]
        ),
        BottomItemData(
            stringResource(id = R.string.page_mine),
            painter = icons[1],
            selectPainter = selectedIcons[1]
        )
    )

    //Pager状态
    val pagerState = rememberPagerState(1)

    val isShowSettingsDialog = remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopBar(pagerState, isShowSettingsDialog)
        },
        content = {
            ViewPager(it, pagerState, titles)
        },
        bottomBar = {
            NavigationView(pagerState, titles)
        }
    )

    if (isShowSettingsDialog.value) {
        SettingsDialog(onDismiss = { isShowSettingsDialog.value = false })
    }
}

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val configuration = LocalConfiguration.current
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.widthIn(max = configuration.screenWidthDp.dp - 80.dp),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.settings_tip),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Divider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsDialogSectionTitle(text = stringResource(id = R.string.text_theme))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.values().onEach {
                        val tint = appTheme(themeType = it).primary
                        val selected = it == getAppTheme()
                        FilterChip(
                            selected = selected,
                            onClick = { setAppTheme(it) },
                            label = {
                                Text(
                                    text = it.getRealName(),
                                    color = tint
                                )
                            },
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        imageVector = NiaIcons.Check,
                                        contentDescription = null,
                                        tint = tint
                                    )
                                }
                            },
                            shape = RoundedCornerShape(28.dp),
                            enabled = if (it == AppTheme.DYNAMIC_COLOR) Build.VERSION.SDK_INT >= Build.VERSION_CODES.S else true
                        )
                    }
                }
                SettingsDialogSectionTitle(text = stringResource(id = R.string.text_theme_drak))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DarkTheme.values().onEach {
                        val selected = it == getDarkTheme()
                        FilterChip(
                            selected = selected,
                            onClick = { setDarkTheme(it) },
                            label = {
                                Text(
                                    text = when (it) {
                                        DarkTheme.LIGHT -> stringResource(id = R.string.theme_light)
                                        DarkTheme.NIGHT -> stringResource(id = R.string.theme_night)
                                        DarkTheme.SYSTEM -> stringResource(id = R.string.theme_system)
                                    },
                                )
                            },
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        imageVector = NiaIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            shape = RoundedCornerShape(28.dp)
                        )
                    }
                }
                Divider(Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(
                    text = stringResource(R.string.definite)
                )
            }
        },
    )
}

@Composable
private fun SettingsDialogSectionTitle(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopBar(pagerState: PagerState, isShowSettingsDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.app_name))
        },
        actions = {
            Actions(pagerState = pagerState)
            IconButton(
                onClick = {
                    isShowSettingsDialog.value = true
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(id = R.string.settings_tip),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.Actions(pagerState: PagerState) {
    val context = LocalContext.current
    val isShow by remember {
        derivedStateOf {
            pagerState.currentPage == 1
        }
    }
    AnimatedVisibility(visible = isShow) {
        Row {
            IconButton(
                onClick = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.float_monitor_tip),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_graph),
                    contentDescription = stringResource(id = R.string.float_monitor_tip),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(
                onClick = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.power_radio_tip),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_power),
                    contentDescription = stringResource(id = R.string.power_radio_tip),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewPager(
    paddingValues: PaddingValues,
    pagerState: PagerState,
    titles: List<BottomItemData>
) {
    HorizontalPager(
        beyondBoundsPageCount = titles.size,
        pageCount = titles.size,
        state = pagerState,
        modifier = Modifier.padding(paddingValues)
    ) {
        when (it) {
            0 -> FunctionPage()
            1 -> DevicePage()
            2 -> MinePage()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavigationView(pagerState: PagerState, titles: List<BottomItemData>) {
    val scope = rememberCoroutineScope()
    NiaNavigationBar(
        modifier = Modifier.testTag("NiaBottomBar")
    ) {
        titles.forEachIndexed { index, item ->
            NavigationItem(index, item, pagerState, scope)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.NavigationItem(
    index: Int,
    item: BottomItemData,
    pagerState: PagerState,
    scope: CoroutineScope
) {
    val selected by remember {
        derivedStateOf { pagerState.currentPage == index }
    }
    NiaNavigationBarItem(
        icon = {
            Icon(
                imageVector = item.painter,
                contentDescription = item.title,
            )
        },
        selectedIcon = {
            Icon(
                imageVector = item.selectPainter,
                contentDescription = item.title,
            )
        },
        label = {
            Text(text = item.title)
        },
        selected = selected,
        onClick = {
            scope.launch {
                pagerState.scrollToPage(index)
            }
        },
    )
}

private fun Modifier.notificationDot(): Modifier =
    composed {
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        drawWithContent {
            drawContent()
            drawCircle(
                tertiaryColor,
                radius = 5.dp.toPx(),
                // This is based on the dimensions of the NavigationBar's "indicator pill";
                // however, its parameters are private, so we must depend on them implicitly
                // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
                center = center + Offset(
                    64.dp.toPx() * .45f,
                    32.dp.toPx() * -.45f - 6.dp.toPx(),
                ),
            )
        }
    }

@Preview(showBackground = true)
@Composable
fun PreviewContentUI() {
    ContentView()
}
