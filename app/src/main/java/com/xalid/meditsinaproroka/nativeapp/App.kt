package com.xalid.meditsinaproroka.nativeapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MedicinaApp(book: BookData, store: AppStore) {
    var current by remember { mutableStateOf<Route>(Route.Home) }
    var navigatingBack by remember { mutableStateOf(false) }
    val backStack = remember { mutableStateListOf<Route>() }
    val screenStateHolder = rememberSaveableStateHolder()
    var searchQuery by remember { mutableStateOf("") }
    var searchFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var bookmarkFolder by remember { mutableStateOf("Все") }
    val context = LocalContext.current
    val activity = context as? Activity

    fun navigate(route: Route, push: Boolean = true) {
        if (route == current) return
        navigatingBack = false
        if (push) backStack.add(current)
        current = route
    }

    fun goBack() {
        if (backStack.isNotEmpty()) {
            navigatingBack = true
            current = backStack.removeAt(backStack.lastIndex)
        }
    }

    fun root(route: Route) {
        if (route == current && backStack.isEmpty()) return
        navigatingBack = route == Route.Home && current != Route.Home
        backStack.clear()
        current = route
    }

    BackHandler(enabled = true) {
        when {
            backStack.isNotEmpty() -> goBack()
            current != Route.Home -> root(Route.Home)
            else -> activity?.finish()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            StandaloneBottomNav(
                current = current,
                onHome = { root(Route.Home) },
                onTopics = { root(Route.Topics) },
                onSearch = { root(Route.Search) },
                onBookmarks = { root(Route.Bookmarks) },
                onMore = { root(Route.More) },
            )
        },
    ) { insets ->
        val modifier = Modifier.padding(insets)
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                // A modest directional slide + fade is clearer than an almost
                // invisible scale. No animated content-size remeasurement: it
                // can stall on a reader containing many selectable text views.
                val enter = fadeIn(animationSpec = tween(180)) +
                    slideInHorizontally(
                        initialOffsetX = { width -> if (navigatingBack) -width / 12 else width / 12 },
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    )
                val exit = fadeOut(animationSpec = tween(155)) +
                    slideOutHorizontally(
                        targetOffsetX = { width -> if (navigatingBack) width / 18 else -width / 18 },
                        animationSpec = tween(190, easing = FastOutSlowInEasing)
                    )
                enter togetherWith exit
            },
            label = "sectionTransition",
        ) { route ->
            screenStateHolder.SaveableStateProvider(routeStateKey(route)) {
                when (route) {
                Route.Home -> WebHomeScreen(
                    book = book,
                    store = store,
                    modifier = modifier,
                    navigate = ::navigate,
                    onGlobalSearch = { navigate(Route.Search) },
                    onToggleTheme = store::toggleSharedTheme,
                )
                Route.Book -> BookScreen(book, modifier, ::goBack) { navigate(Route.Reader(it)) }
                Route.Topics -> TopicsScreen(book, modifier) { navigate(Route.TopicDetail(it)) }
                Route.Search -> SearchScreen(
                    book = book,
                    query = searchQuery,
                    onQuery = { searchQuery = it },
                    filter = searchFilter,
                    onFilter = { searchFilter = it },
                    modifier = modifier,
                    onOpen = { chapterId, anchor -> navigate(Route.Reader(chapterId, anchor)) },
                )
                Route.Bookmarks -> BookmarksScreen(
                    book = book,
                    store = store,
                    folder = bookmarkFolder,
                    onFolder = { bookmarkFolder = it },
                    modifier = modifier,
                    onOpen = { id, anchor -> navigate(Route.Reader(id, anchor)) },
                )
                Route.More -> WebMoreScreen(modifier, ::navigate)
                Route.Remedies -> RemediesScreen(book, modifier, ::goBack) { navigate(Route.RemedyDetail(it)) }
                Route.Treatments -> TreatmentsScreen(book, modifier, ::goBack) { navigate(Route.Reader(it)) }
                Route.Notes -> NotesScreen(book, store, modifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.Settings -> WebSettingsScreen(store, modifier, ::goBack)
                Route.Hadiths -> HadithsScreen(book, modifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.History -> HistoryScreen(book, store, modifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.Offline -> OfflineScreen(book, modifier, ::goBack)
                Route.About -> AboutScreen(book, modifier, ::goBack)
                Route.Collections -> CollectionsScreen(book, modifier, ::goBack) { navigate(Route.CollectionDetail(it)) }
                Route.Glossary -> GlossaryScreen(book, modifier, ::goBack) { navigate(Route.GlossaryDetail(it)) }
                Route.Source -> SourceScreen(book, modifier, ::goBack)
                is Route.GlossaryDetail -> GlossaryDetailScreen(book, route.id, modifier, ::goBack) { id, anchor ->
                    navigate(Route.Reader(id, anchor))
                }
                is Route.TopicDetail -> TopicDetailScreen(book, route.id, modifier, ::goBack) {
                    navigate(Route.Reader(it))
                }
                is Route.RemedyDetail -> RemedyDetailScreen(book, route.id, modifier, ::goBack) { id, anchor ->
                    navigate(Route.Reader(id, anchor))
                }
                is Route.CollectionDetail -> CollectionDetailScreen(book, route.id, modifier, ::goBack) {
                    navigate(Route.Reader(it))
                }
                is Route.Reader -> ReaderScreen(book, store, route, modifier, ::goBack) { next ->
                    navigate(next)
                }
                }
            }
        }
    }
}


private fun routeStateKey(route: Route): String = when (route) {
    Route.Home -> "home"
    Route.Book -> "book"
    Route.Topics -> "topics"
    Route.Search -> "search"
    Route.Bookmarks -> "bookmarks"
    Route.More -> "more"
    Route.Remedies -> "remedies"
    Route.Treatments -> "treatments"
    Route.Notes -> "notes"
    Route.Settings -> "settings"
    Route.Hadiths -> "hadiths"
    Route.History -> "history"
    Route.Offline -> "offline"
    Route.About -> "about"
    Route.Collections -> "collections"
    Route.Glossary -> "glossary"
    Route.Source -> "source"
    is Route.TopicDetail -> "topic:${route.id}"
    is Route.RemedyDetail -> "remedy:${route.id}"
    is Route.CollectionDetail -> "collection:${route.id}"
    is Route.GlossaryDetail -> "glossary:${route.id}"
    is Route.Reader -> "reader:${route.chapterId}:${route.anchor.orEmpty()}:${route.resume}"
}
@Composable
private fun StandaloneBottomNav(
    current: Route,
    onHome: () -> Unit,
    onTopics: () -> Unit,
    onSearch: () -> Unit,
    onBookmarks: () -> Unit,
    onMore: () -> Unit,
) {
    data class NavItem(
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val selected: Boolean,
        val action: () -> Unit,
    )
    val items = listOf(
        NavItem("Главная", androidx.compose.material.icons.Icons.Default.Home, current == Route.Home, onHome),
        NavItem("Темы", androidx.compose.material.icons.Icons.Default.GridView, current == Route.Topics, onTopics),
        NavItem("Поиск", androidx.compose.material.icons.Icons.Default.Search, current == Route.Search, onSearch),
        NavItem("Закладки", androidx.compose.material.icons.Icons.Default.BookmarkBorder, current == Route.Bookmarks, onBookmarks),
        NavItem("Ещё", androidx.compose.material.icons.Icons.Default.MoreHoriz, current == Route.More, onMore),
    )
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    val background = if (dark) Color(0xFF1F2522) else Color(0xFFFBF7F0)
    val muted = if (dark) Color(0xFFAAB3AD) else Color(0xFF55524C)
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .height(69.dp)
            .shadow(2.dp, shape)
            .clip(shape)
            .background(background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val color = if (item.selected) Color(0xFF356D57) else muted
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().clickable { item.action() }
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = item.icon, contentDescription = item.label,
                    modifier = Modifier.size(24.dp), tint = color
                )
                Spacer(Modifier.height(4.dp))
                Text(item.label, fontSize = 11.sp, color = color, maxLines = 1)
            }
        }
    }
}
