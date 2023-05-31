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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.ui.compose.setContentUI
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
data class BottomItemData(var title: String, var painter: Painter)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContentView() {

    val titles = listOf(
        BottomItemData(
            stringResource(id = R.string.page_function),
            painter = painterResource(id = R.drawable.ic_nav_function)
        ),
        BottomItemData(
            stringResource(id = R.string.page_device),
            painter = painterResource(id = R.drawable.ic_home)
        ),
        BottomItemData(
            stringResource(id = R.string.page_mine),
            painter = painterResource(id = R.drawable.icon_mine_fill)
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

    NavigationBar {
        titles.onEachIndexed { index, bottomItemData ->
            val selected = index == pagerState.currentPage

            //动画时长
            val durationMillis = 400
            //插值器
            val animationSpec =
                TweenSpec<Color>(durationMillis = durationMillis, easing = FastOutLinearInEasing)

            val tint by animateColorAsState(
                targetValue = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = .5f),
                animationSpec = animationSpec,
                label = "tint"
            )

            NavigationBarItem(
                alwaysShowLabel = false,
                selected = selected,
                onClick = {
                    scope.launch {
                        pagerState.scrollToPage(index)
                    }
                },
                icon = {
                    Icon(
                        painter = bottomItemData.painter,
                        contentDescription = bottomItemData.title,
                        modifier = Modifier.size(
                            Icons.Filled.Home.defaultWidth,
                            Icons.Filled.Home.defaultHeight
                        ),
                        tint = tint
                    )
                },
                label = {
                    Text(
                        text = (bottomItemData.title)
                    )
                }
            )
        }
    }
}
