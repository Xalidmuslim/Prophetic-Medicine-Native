package com.xalid.meditsinaproroka.nativeapp

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF305F4A)
private val Beige = Color(0xFFF5F1E7)
private val DarkText = Color(0xFF242421)
private val Border = Color(0xFFD9D0BF)

private sealed interface Route {
    data object Home : Route
    data object Book : Route
    data object Topics : Route
    data object Remedies : Route
    data object Treatments : Route
    data object Search : Route
    data object Bookmarks : Route
    data object History : Route
    data object More : Route
    data object Settings : Route
    data object About : Route
    data class Reader(val chapterId: String) : Route
    data class TopicChapters(val topicId: String) : Route
    data class RemedyChapters(val remedyId: String) : Route
}

@Composable
fun MedicineApp() {
    val context = LocalContext.current
    val store = remember { AppStore(context) }
    var book by remember { mutableStateOf(BookData.load(context)) }
    var importing by remember { mutableStateOf(false) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    val importScope = rememberCoroutineScope()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importing = true
            importStatus = null
            importScope.launch {
                try {
                    val loaded = withContext(Dispatchers.IO) {
                        BookRepository.importBook(context, uri)
                    }
                    book = loaded
                    importStatus = "Книга успешно импортирована: ${loaded.chapters.size} глав. Доступна без интернета."
                } catch (error: Exception) {
                    importStatus = "Не удалось импортировать книгу: ${error.message ?: "неизвестная ошибка"}"
                } finally {
                    importing = false
                }
            }
        }
    }
    val selectBook: () -> Unit = {
        if (!importing) filePicker.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
    }
    var dark by remember { mutableStateOf(store.darkMode) }
    var fontSize by remember { mutableFloatStateOf(store.fontSize) }
    var bookmarks by remember { mutableStateOf(store.bookmarks()) }
    val stack = remember { mutableStateListOf<Route>() }
    var route by remember { mutableStateOf<Route>(Route.Home) }

    val navigate: (Route) -> Unit = { destination ->
        if (route != destination) {
            stack.add(route)
            route = destination
            if (destination is Route.Reader && book.hasFullText) store.rememberChapter(destination.chapterId)
        }
    }
    val back: () -> Unit = {
        if (stack.isNotEmpty()) route = stack.removeAt(stack.lastIndex)
        else route = Route.Home
    }
    BackHandler(enabled = route != Route.Home) { back() }

    val palette = if (dark) darkColorScheme(
        primary = Color(0xFFA5CDB4), background = Color(0xFF191D1B),
        surface = Color(0xFF272E2A), onSurface = Color(0xFFF4F2E8),
        onBackground = Color(0xFFF4F2E8), surfaceVariant = Color(0xFF333C35)
    ) else lightColorScheme(
        primary = Green, background = Beige, surface = Color(0xFFFCF9F1),
        onSurface = DarkText, onBackground = DarkText, surfaceVariant = Color(0xFFE8E7DD)
    )
    MaterialTheme(colorScheme = palette) {
        Column(Modifier.fillMaxSize().background(palette.background)) {
            AppHeader(
                subtitle = book.author,
                canBack = route != Route.Home,
                onBack = back,
                onSettings = { navigate(Route.Settings) }
            )
            HorizontalDivider(color = if (dark) palette.surfaceVariant else Border)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = palette.background,
                bottomBar = { AppNavigation(route, navigate) }
            ) { contentPadding ->
                AnimatedContent(
                    targetState = route,
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    transitionSpec = { fadeIn(tween(170)) togetherWith fadeOut(tween(115)) },
                    label = "nativeScreenTransition"
                ) { current ->
                    when (current) {
                        Route.Home -> HomeScreen(book, store.lastChapterId, store.readChapters().size, navigate, selectBook, importing, importStatus)
                        Route.Book -> ChapterListScreen(book, navigate)
                        Route.Topics -> TopicScreen(book, navigate)
                        Route.Remedies -> RemediesScreen(book, navigate)
                        Route.Treatments -> TreatmentsScreen(book, navigate)
                        Route.Search -> SearchScreen(book, navigate)
                        Route.Bookmarks -> BookmarksScreen(book, bookmarks, navigate)
                        Route.History -> HistoryScreen(book, store.history(), navigate)
                        Route.More -> MoreScreen(navigate)
                        Route.Settings -> SettingsScreen(dark, fontSize, onImport = selectBook,
                            importBusy = importing, importStatus = importStatus, hasFullText = book.hasFullText, onDark = {
                            dark = it; store.darkMode = it
                        }, onSize = {
                            fontSize = it; store.fontSize = it
                        })
                        Route.About -> AboutScreen(book)
                        is Route.Reader -> ReaderScreen(
                            book, current.chapterId, fontSize, bookmarks.contains(current.chapterId),
                            onBookmark = { bookmarks = store.toggleBookmark(current.chapterId) },
                            onNavigate = navigate
                        )
                        is Route.TopicChapters -> SelectionScreen(
                            title = book.topics.find { it.id == current.topicId }?.title ?: "Тема",
                            chapters = book.chapters.filter {
                                it.id in (book.topics.find { t -> t.id == current.topicId }?.chapterIds ?: emptyList())
                            }, book = book, navigate = navigate
                        )
                        is Route.RemedyChapters -> SelectionScreen(
                            title = book.remedies.find { it.id == current.remedyId }?.title ?: "Средство",
                            chapters = book.chapters.filter {
                                it.id in (book.remedies.find { t -> t.id == current.remedyId }?.chapterIds ?: emptyList())
                            }, book = book, navigate = navigate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHeader(subtitle: String, canBack: Boolean, onBack: () -> Unit, onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canBack) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
        } else {
            Image(
                painter = painterResource(R.drawable.medicine_launcher),
                contentDescription = "Медицина Пророка",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Медицина Пророка ﷺ", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = "Настройки") }
    }
}

@Composable
private fun AppNavigation(current: Route, navigate: (Route) -> Unit) {
    val tabs = listOf(
        Triple(Route.Home, "Главная", Icons.Default.Home),
        Triple(Route.Topics, "Темы", Icons.Default.FormatListBulleted),
        Triple(Route.Search, "Поиск", Icons.Default.Search),
        Triple(Route.Bookmarks, "Закладки", Icons.Default.BookmarkBorder),
        Triple(Route.More, "Ещё", Icons.Default.MoreHoriz)
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { (route, title, icon) ->
            NavigationBarItem(
                selected = current == route || (route == Route.Topics && current is Route.TopicChapters),
                onClick = { navigate(route) }, icon = { Icon(icon, contentDescription = title) },
                label = { Text(title, maxLines = 1, fontSize = 11.sp) },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
private fun Screen(title: String, content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 30.dp)
    ) {
        item {
            Text(
                title,
                fontSize = if (title.startsWith("Книга, разбитая")) 29.sp else 27.sp,
                fontFamily = if (title.startsWith("Книга, разбитая")) FontFamily.Serif else FontFamily.Default,
                fontWeight = FontWeight.Bold,
                lineHeight = if (title.startsWith("Книга, разбитая")) 36.sp else 34.sp
            )
        }
        content()
    }
}

@Composable
private fun HomeScreen(
    book: BookData, lastChapterId: String?, readCount: Int, navigate: (Route) -> Unit,
    onImport: () -> Unit, importBusy: Boolean, importStatus: String?
) {
    Screen("Книга, разбитая на главы, темы и средства") {
        item { Text("Полный русский текст с поиском, закладками, источниками и офлайн-доступом.", style = MaterialTheme.typography.bodyMedium) }
        if (!book.hasFullText) item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BookUnavailable()
                Button(onClick = onImport, enabled = !importBusy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (importBusy) "Проверка и загрузка…" else "Импортировать книгу из файла JSON")
                }
            }
        }
        if (importStatus != null) item { Text(importStatus, style = MaterialTheme.typography.bodyMedium) }
        item {
            val percent = if (book.chapters.isNotEmpty()) {
                (100f * readCount / book.chapters.size).toInt().coerceIn(0, 100)
            } else 0
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Border),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Продолжить чтение", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Text("$percent%", color = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        book.chapter(lastChapterId.orEmpty())?.title
                            ?: book.chapters.firstOrNull()?.title ?: "Первая глава",
                        fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp,
                        maxLines = 3, overflow = TextOverflow.Ellipsis
                    )
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Button(
                        onClick = { if (book.chapters.isNotEmpty()) navigate(Route.Reader(lastChapterId ?: book.chapters.first().id)) },
                        enabled = book.hasFullText && book.chapters.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Продолжить →")
                    }
                }
            }
        }
        item { FeatureCard("Поиск по всей книге", "По названиям и содержанию", Icons.Default.Search, onClick = { navigate(Route.Search) }) }
        item { FeatureCard("Как лечили / что применялось", "Состояние → средства → полный текст", Icons.Default.Healing, onClick = { navigate(Route.Treatments) }) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    FeatureCard("Читать книгу", "${book.expectedChapters} глав", Icons.Default.Book, onClick = { navigate(Route.Book) })
                }
                Box(Modifier.weight(1f)) {
                    FeatureCard("Темы", "${book.expectedTopics} разделов", Icons.Default.FormatListBulleted, onClick = { navigate(Route.Topics) })
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    FeatureCard("Средства", "${book.expectedRemedies} позиций", Icons.Default.LocalHospital, onClick = { navigate(Route.Remedies) })
                }
                Box(Modifier.weight(1f)) {
                    FeatureCard("Закладки", "Сохранённые главы", Icons.Default.Bookmark, onClick = { navigate(Route.Bookmarks) })
                }
            }
        }
    }
}

@Composable
private fun BookUnavailable() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Техническая версия: текст книги ещё не подключён", fontWeight = FontWeight.SemiBold)
            Text(
                "Это отдельная нативная тестовая сборка. Полный текст не размещён в публичном репозитории. Установленную версию 1.0.0 не удаляйте.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, if (MaterialTheme.colorScheme.background == Beige) Border else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SimpleRow(title: String, subtitle: String = "", onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, if (MaterialTheme.colorScheme.background == Beige) Border else MaterialTheme.colorScheme.surfaceVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun ChapterListScreen(book: BookData, navigate: (Route) -> Unit) {
    Screen("Читать книгу") {
        if (!book.hasFullText) item { BookUnavailable() }
        itemsIndexed(book.chapters, key = { _, ch -> ch.id }) { index, chapter ->
            SimpleRow("${index + 1}. ${chapter.title}", chapter.section) {
                navigate(Route.Reader(chapter.id))
            }
        }
    }
}

@Composable
private fun SelectionScreen(title: String, chapters: List<Chapter>, book: BookData, navigate: (Route) -> Unit) {
    Screen(title) {
        if (!book.hasFullText) item { BookUnavailable() }
        items(chapters, key = { it.id }) { chapter ->
            SimpleRow(chapter.title, chapter.section) { navigate(Route.Reader(chapter.id)) }
        }
    }
}

@Composable
private fun TopicScreen(book: BookData, navigate: (Route) -> Unit) {
    Screen("Темы") {
        if (!book.hasFullText) item { BookUnavailable() }
        items(book.topics, key = { it.id }) { topic ->
            SimpleRow(topic.title, "${topic.chapterIds.size} глав") { navigate(Route.TopicChapters(topic.id)) }
        }
    }
}

@Composable
private fun RemediesScreen(book: BookData, navigate: (Route) -> Unit) {
    Screen("Средства") {
        if (!book.hasFullText) item { BookUnavailable() }
        items(book.remedies, key = { it.id }) { remedy ->
            SimpleRow(remedy.title, "${remedy.chapterIds.size} упоминаний") { navigate(Route.RemedyChapters(remedy.id)) }
        }
    }
}

@Composable
private fun TreatmentsScreen(book: BookData, navigate: (Route) -> Unit) {
    Screen("Как лечили / что применялось") {
        item { Text("Исторические сведения из книги, а не медицинские назначения.", style = MaterialTheme.typography.bodySmall) }
        if (!book.hasFullText) item { BookUnavailable() }
        items(book.treatments, key = { it.id }) { tx ->
            SimpleRow(tx.condition, tx.category) { navigate(Route.Reader(tx.chapterId)) }
        }
    }
}

@Composable
private fun SearchScreen(book: BookData, navigate: (Route) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, book) { book.search(query) }
    Screen("Поиск по всей книге") {
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Поиск по тексту и главам") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (!book.hasFullText) item { BookUnavailable() }
        items(results, key = { it.id }) { chapter ->
            SimpleRow(chapter.title, chapter.section) { navigate(Route.Reader(chapter.id)) }
        }
        if (query.length > 1 && results.isEmpty() && book.hasFullText) {
            item { Text("Совпадений не найдено") }
        }
    }
}

@Composable
private fun BookmarksScreen(book: BookData, bookmarks: Set<String>, navigate: (Route) -> Unit) {
    Screen("Закладки") {
        if (!book.hasFullText) item { BookUnavailable() }
        items(book.chapters.filter { it.id in bookmarks }, key = { it.id }) { chapter ->
            SimpleRow(chapter.title) { navigate(Route.Reader(chapter.id)) }
        }
        if (bookmarks.isEmpty()) item { Text("Пока нет сохранённых глав") }
    }
}

@Composable
private fun HistoryScreen(book: BookData, history: List<String>, navigate: (Route) -> Unit) {
    Screen("Недавно читали") {
        if (!book.hasFullText) item { BookUnavailable() }
        items(history.mapNotNull { book.chapter(it) }, key = { it.id }) { chapter ->
            SimpleRow(chapter.title) { navigate(Route.Reader(chapter.id)) }
        }
    }
}

@Composable
private fun ReaderScreen(book: BookData, chapterId: String, fontSize: Float, bookmarked: Boolean, onBookmark: () -> Unit, onNavigate: (Route) -> Unit) {
    val chapter = book.chapter(chapterId)
    if (chapter == null) {
        Screen("Глава не найдена") { item { BookUnavailable() } }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { Text(chapter.section, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
        item { Text(chapter.title, fontSize = 27.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold) }
        item {
            TextButton(onClick = onBookmark) {
                Icon(if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (bookmarked) "Убрать закладку" else "Сохранить закладку")
            }
        }
        items(chapter.blocks, key = { it.id }) { block ->
            val isHeading = block.type == "subheading"
            val isHistorical = block.type == "historical_note"
            Surface(
                color = if (isHistorical) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            ) {
                Text(
                    text = block.text,
                    fontSize = (fontSize + if (isHeading) 2f else 0f).sp,
                    lineHeight = (fontSize * 1.48f).sp,
                    fontWeight = if (isHeading) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth().padding(if (isHistorical) 12.dp else 0.dp)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { chapter.previousId?.let { onNavigate(Route.Reader(it)) } },
                    enabled = chapter.previousId != null,
                    modifier = Modifier.weight(1f)
                ) { Icon(Icons.Default.ArrowBack, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Назад") }
                Button(
                    onClick = { chapter.nextId?.let { onNavigate(Route.Reader(it)) } },
                    enabled = chapter.nextId != null,
                    modifier = Modifier.weight(1f)
                ) { Text("Дальше"); Spacer(Modifier.width(4.dp)); Icon(Icons.Default.ArrowForward, contentDescription = null) }
            }
        }
    }
}

@Composable
private fun MoreScreen(navigate: (Route) -> Unit) {
    Screen("Ещё") {
        item { SimpleRow("Читать книгу") { navigate(Route.Book) } }
        item { SimpleRow("Как лечили / что применялось") { navigate(Route.Treatments) } }
        item { SimpleRow("Средства") { navigate(Route.Remedies) } }
        item { SimpleRow("Недавно читали") { navigate(Route.History) } }
        item { SimpleRow("Настройки чтения") { navigate(Route.Settings) } }
        item { SimpleRow("О приложении") { navigate(Route.About) } }
    }
}

@Composable
private fun SettingsScreen(
    darkMode: Boolean, fontSize: Float, onImport: () -> Unit,
    importBusy: Boolean, importStatus: String?, hasFullText: Boolean,
    onDark: (Boolean) -> Unit, onSize: (Float) -> Unit
) {
    Screen("Настройки чтения") {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (darkMode) Icons.Default.DarkMode else Icons.Default.WbSunny, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Тёмная тема", modifier = Modifier.weight(1f))
                        Switch(checked = darkMode, onCheckedChange = onDark)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    Text("Размер текста: ${fontSize.toInt()}", style = MaterialTheme.typography.bodyLarge)
                    Slider(value = fontSize, onValueChange = onSize, valueRange = 14f..32f)
                    Text("Пример текста книги", fontSize = fontSize.sp, lineHeight = (fontSize * 1.48f).sp)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Офлайн-книга", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (hasFullText) "Полный текст доступен офлайн."
                        else "Полный текст не встроен в публичную тестовую сборку. Выберите файл book.json, извлечённый из вашей версии 1.0.0.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = onImport, enabled = !importBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (importBusy) "Импорт выполняется…" else "Выбрать файл книги")
                    }
                    if (importStatus != null) {
                        Text(importStatus, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutScreen(book: BookData) {
    Screen("О приложении") {
        item { Text("Медицина Пророка ﷺ — Ибн Каййим аль-Джаузия") }
        item {
            Text(
                "Независимое нативное приложение на Kotlin и Jetpack Compose. " +
                "Эта версия предназначена для проверки архитектуры и интерфейса; не заменяет установленную 1.0.0. " +
                "Исторические медицинские представления не следует использовать как замену помощи врача.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        if (!book.hasFullText) item { BookUnavailable() }
    }
}
