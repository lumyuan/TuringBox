package io.github.lumyuan.turingbox.windows.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.ui.compose.setContentUI
import io.github.lumyuan.turingbox.ui.icon.NiaIcons
import io.github.lumyuan.turingbox.ui.widget.NiaNavigationBar
import io.github.lumyuan.turingbox.ui.widget.NiaNavigationBarItem
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
data class BottomItemData(var title: String, var painter: ImageVector, var selectPainter: ImageVector)

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

    Scaffold(
        topBar = {
            TopBar(pagerState)
        },
        content = {
            ViewPager(it, pagerState, titles)
        },
        bottomBar = {
            NavigationView(pagerState, titles)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopBar(pagerState: PagerState) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.app_name))
        },
        actions = {
            AnimatedVisibility(visible = pagerState.currentPage == 1) {
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
            IconButton(
                onClick = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_tip),
                        Toast.LENGTH_SHORT
                    ).show()
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
                selected = index == pagerState.currentPage,
                onClick = {
                    scope.launch {
                        pagerState.scrollToPage(index)
                    }
                },
            )
        }
    }
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
