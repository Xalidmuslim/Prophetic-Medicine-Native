package com.xalid.meditsinaproroka.nativeapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Median calm paper regions sampled from the supplied reference.
internal val AntiqueParchment = Color(0xFFE9DAC8)
internal val AntiquePaper = Color(0xFFF1E7D8)
internal val AntiqueInk = Color(0xFF211D19)
internal val AntiqueMuted = Color(0xFF625D53)
internal val AntiqueGreen = Color(0xFF305A43)
internal val AntiqueBorder = Color(0xFFD8C8AE)

@Composable
internal fun AntiqueIcon(@DrawableRes resource: Int, modifier: Modifier, description: String? = null,
                         scale: ContentScale = ContentScale.Fit) {
    Image(painterResource(resource), description, modifier, contentScale = scale)
}

/** The scalable frame, text and click target are native; artwork is a separate layer. */
@Composable
private fun PaperCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        color = if (dark) MaterialTheme.colorScheme.surface else AntiquePaper,
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(0.7.dp, if (dark) MaterialTheme.colorScheme.outline else AntiqueBorder),
        shadowElevation = 1.5.dp,
    ) {
        Box {
            if (!dark) Image(
                painterResource(R.drawable.antique_card_paper), null,
                Modifier.matchParentSize(), contentScale = ContentScale.Crop, alpha = 0.65f,
            )
            content()
        }
    }
}

@Composable
private fun AntiqueHomeHeader(book: BookData, onSettings: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    val enlarged = LocalDensity.current.fontScale > 1.15f
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val artWidth = maxWidth * 0.34f
        if (!dark) {
            Image(painterResource(R.drawable.antique_hero_scene), null,
                Modifier.width(artWidth).aspectRatio(472f / 414f).align(Alignment.TopEnd),
                contentScale = ContentScale.Fit)
            Box(Modifier.width(artWidth).aspectRatio(472f / 414f).align(Alignment.TopEnd)
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.82f to Color.Transparent, 1f to AntiqueParchment))
                .background(Brush.horizontalGradient(0f to AntiqueParchment, 0.16f to Color.Transparent, 1f to Color.Transparent)))
        }
        Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 12.dp, top = 15.dp, bottom = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.medicine_launcher), "Настройки чтения",
                    Modifier.size(70.dp).clip(RoundedCornerShape(15.dp)).clickable(role = Role.Button, onClick = onSettings),
                    contentScale = ContentScale.Fit)
                if (!enlarged) {
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f).padding(end = artWidth - 12.dp)) {
                        Text(book.title, fontFamily = WebSerifFont, fontWeight = FontWeight.Bold,
                            fontSize = 23.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(6.dp))
                        Text(book.author, fontFamily = WebSansFont, fontSize = 10.5.sp, lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            if (enlarged) {
                // At accessible font sizes, put all native text below the still life.
                Spacer(Modifier.height(artWidth / (472f / 414f) - 70.dp))
                Text(book.title, fontFamily = WebSerifFont, fontWeight = FontWeight.Bold,
                    fontSize = 23.sp, lineHeight = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(book.author, fontFamily = WebSansFont, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun ReadingCard(title: String, pct: Int, hasLast: Boolean, onContinue: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    PaperCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Only transparent vertical padding is trimmed; the book keeps its scale.
                AntiqueIcon(R.drawable.antique_open_book, Modifier.size(width = 52.dp, height = 42.dp), scale = ContentScale.Crop)
                Spacer(Modifier.width(5.dp))
                Text("Продолжить чтение", Modifier.weight(1f), fontFamily = WebSansFont,
                    fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size((39 * LocalDensity.current.fontScale.coerceAtLeast(1f)).dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { pct / 100f }, modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = if (dark) MaterialTheme.colorScheme.outline else AntiqueBorder.copy(alpha = 0.6f),
                        strokeWidth = 2.dp,
                    )
                    Text("$pct%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(title, fontFamily = WebSerifFont, fontWeight = FontWeight.Bold,
                fontSize = 18.5.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { pct / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = if (dark) MaterialTheme.colorScheme.outline else AntiqueBorder,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(10.dp))
            Surface(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                color = AntiqueGreen,
                border = BorderStroke(0.6.dp, Color(0xFF78866A)),
                shadowElevation = 1.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Cropping is restricted to decorative edges, never text or functional art.
                    Image(painterResource(R.drawable.antique_button), null,
                        Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                    Text(if (hasLast) "Продолжить →" else "Начать чтение →",
                        Modifier.padding(horizontal = 38.dp, vertical = 10.dp),
                        fontFamily = WebSerifFont, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, lineHeight = 23.sp, color = Color(0xFFFFFAEF))
                }
            }
        }
    }
}

@Composable
private fun AntiqueQuickCard(@DrawableRes icon: Int, title: String, subtitle: String,
                             modifier: Modifier, onClick: () -> Unit) {
    PaperCard(modifier.fillMaxHeight().heightIn(min = 96.dp), onClick) {
        Column(Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 3.dp, bottom = 9.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AntiqueIcon(icon, Modifier.size(width = 69.dp, height = 55.dp))
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp))
            }
            Text(title, fontFamily = WebSerifFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(1.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = WebSansFont,
                fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AntiqueHomeScreen(
    book: BookData, store: AppStore, modifier: Modifier,
    navigate: (Route) -> Unit, onGlobalSearch: () -> Unit, onToggleTheme: () -> Unit,
) {
    // Preserve the existing home screen's progress calculation and resume behavior.
    val chaptersById = remember(book) { book.chapters.associateBy { it.id } }
    val last = store.lastChapterId?.let(chaptersById::get)
    val completed = book.chapters.count { chapter ->
        val index = store.progress[chapter.id]
        index != null && chapter.blocks.isNotEmpty() &&
            index.toFloat() >= ((chapter.blocks.size - 1).toFloat() * 0.96f)
    }
    val pct = if (book.chapters.isEmpty()) 0 else
        ((completed * 100f) / book.chapters.size).toInt().coerceIn(0, 100)
    val quickCollections = book.collections.take(4)
    val continueTitle = when {
        last == null -> "Начать с первой главы"
        last.title.trim().startsWith("Глава:", ignoreCase = true) -> last.title.trim()
        else -> "Глава: ${last.title.trim()}"
    }
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!dark) Image(painterResource(R.drawable.antique_parchment), null,
            Modifier.matchParentSize(), contentScale = ContentScale.Crop, alpha = 0.72f)
        LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            item { AntiqueHomeHeader(book) { navigate(Route.Settings) } }
            item {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(start = 21.dp, end = 19.dp, top = 10.dp, bottom = 4.dp)) {
                  val headingSize = ((maxWidth.value + 40f) / 14.6f).coerceIn(24.5f, 28f)
                  Column {
                    Text("Книга, разбитая на главы,\nтемы и средства",
                        fontFamily = WebSerifFont, fontWeight = FontWeight.Bold,
                        fontSize = headingSize.sp, lineHeight = (headingSize * 1.06f).sp, letterSpacing = (-0.4).sp)
                    Spacer(Modifier.height(9.dp))
                    Text("Полный русский текст с поиском, заметками, источниками и офлайн-доступом.",
                        color = MaterialTheme.colorScheme.onBackground, fontFamily = WebSansFont,
                        fontSize = 13.sp, lineHeight = 17.sp)
                  }
                }
            }
            item {
                Box(Modifier.padding(horizontal = 14.dp)) {
                    ReadingCard(continueTitle, pct, last != null) {
                        val chapter = last ?: book.chapters.firstOrNull()
                        if (chapter != null) navigate(Route.Reader(chapter.id, resume = last != null))
                    }
                }
            }
            item {
                PaperCard(Modifier.fillMaxWidth().padding(horizontal = 14.dp), onClick = { navigate(Route.Search) }) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        AntiqueIcon(R.drawable.antique_search, Modifier.size(39.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Поиск по всей книге", Modifier.weight(1f), fontFamily = WebSansFont,
                            fontSize = 14.sp, lineHeight = 18.sp)
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(22.dp))
                    }
                }
            }
            item {
                PaperCard(Modifier.fillMaxWidth().padding(horizontal = 14.dp), onClick = { navigate(Route.Treatments) }) {
                    // Foliage stays in a narrow edge strip outside the text's reserved width.
                    if (!dark) Image(painterResource(R.drawable.antique_foliage), null,
                        Modifier.align(Alignment.CenterEnd).width(26.dp).height(65.dp),
                        contentScale = ContentScale.Crop, alpha = 0.16f)
                    Row(Modifier.fillMaxWidth().padding(start = 5.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        AntiqueIcon(R.drawable.antique_mortar, Modifier.size(68.dp))
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Как лечили / что применялось", fontFamily = WebSerifFont,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp)
                            Spacer(Modifier.height(3.dp))
                            Text("Состояние → средства → полный текст", fontFamily = WebSansFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp, lineHeight = 14.sp)
                        }
                        Spacer(Modifier.width(7.dp))
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AntiqueQuickCard(R.drawable.antique_books, "Читать книгу", "${book.chapters.size} глав", Modifier.weight(1f)) { navigate(Route.Book) }
                    AntiqueQuickCard(R.drawable.antique_scroll, "Темы", "${book.topics.size} разделов", Modifier.weight(1f)) { navigate(Route.Topics) }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AntiqueQuickCard(R.drawable.antique_remedies, "Средства", "${book.remedies.size} позиций", Modifier.weight(1f)) { navigate(Route.Remedies) }
                    AntiqueQuickCard(R.drawable.antique_bookmark, "Закладки", "${store.bookmarks.size} сохранено", Modifier.weight(1f)) { navigate(Route.Bookmarks) }
                }
            }
            if (quickCollections.isNotEmpty()) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Быстрые подборки", Modifier.weight(1f), fontFamily = WebSerifFont,
                            fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        TextButton(onClick = { navigate(Route.Collections) }) { Text("Все") }
                    }
                }
                for (rowIndex in 0 until ((quickCollections.size + 1) / 2)) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            val first = quickCollections.getOrNull(rowIndex * 2)
                            val second = quickCollections.getOrNull(rowIndex * 2 + 1)
                            if (first != null) WebCollectionCard(first, Modifier.weight(1f)) { navigate(Route.CollectionDetail(first.id)) }
                            if (second != null) WebCollectionCard(second, Modifier.weight(1f)) { navigate(Route.CollectionDetail(second.id)) }
                            else Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Продолжить изучение", fontFamily = WebSerifFont, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    // Wrap at narrower widths / larger system fonts instead of horizontal scroll.
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = { navigate(Route.Glossary) }) { Text("Словарь терминов") }
                        OutlinedButton(onClick = { navigate(Route.Hadiths) }) { Text("Хадисы и источники") }
                        OutlinedButton(onClick = { navigate(Route.Notes) }) { Text("Мои заметки") }
                        OutlinedButton(onClick = { navigate(Route.Offline) }) { Text("Чтение без интернета") }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onGlobalSearch) { Text("Поиск") }
                        TextButton(onClick = onToggleTheme) { Text(if (dark) "Светлая тема" else "Тёмная тема") }
                        TextButton(onClick = { navigate(Route.Settings) }) { Text("Aa · Настройки чтения") }
                    }
                }
            }
        }
    }
}
