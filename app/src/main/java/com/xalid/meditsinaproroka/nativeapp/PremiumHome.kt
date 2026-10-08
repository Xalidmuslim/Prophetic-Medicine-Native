package com.xalid.meditsinaproroka.nativeapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Presentation-only dashboard for the ORIGINAL 111-chapter native reader.
 * No chapter data, search, bookmark, note, preference or navigation handler is replaced.
 * All decorative art is local, scalable VectorDrawable with no decoding/network cost.
 */
private val Ivory = Color(0xFFF7F3EB)
private val Deep = Color(0xFF214B3D)
private val Forest = Color(0xFF2F6A52)
private val Sage = Color(0xFF8FA48F)
private val Sand = Color(0xFFC8A46A)
private val BorderCream = Color(0xFFE7DED1)

@Composable
fun PremiumHomeScreen(
    book: BookData,
    store: AppStore,
    modifier: Modifier,
    navigate: (Route) -> Unit,
    onGlobalSearch: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    val chaptersById = remember(book) { book.chapters.associateBy { it.id } }
    val last = store.lastChapterId?.let(chaptersById::get)
    val complete = book.chapters.count { chapter ->
        val index = store.progress[chapter.id]
        index != null && chapter.blocks.isNotEmpty() &&
            index.toFloat() >= (chapter.blocks.size - 1).toFloat() * 0.96f
    }
    val progress = if (book.chapters.isEmpty()) 0 else
        (100f * complete / book.chapters.size).toInt().coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 480),
        label = "bookProgress",
    )
    val currentTitle = when {
        last == null -> book.chapters.firstOrNull()?.title ?: "Начните с первой главы"
        last.title.trim().startsWith("Глава:", ignoreCase = true) -> last.title.trim()
        else -> "Глава: ${last.title.trim()}"
    }
    val onContinue = {
        val chapter = last ?: book.chapters.firstOrNull()
        if (chapter != null) navigate(Route.Reader(chapter.id, resume = last != null))
    }
    val bg = MaterialTheme.colorScheme.background
    val border = MaterialTheme.colorScheme.outline
    val surface = MaterialTheme.colorScheme.surface

    Column(modifier.fillMaxSize().background(bg)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 17.dp, end = 17.dp, top = 9.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.medicine_launcher),
                contentDescription = null,
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Медицина Пророка ﷺ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Ибн Каййим аль-Джаузия",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                onClick = { navigate(Route.Settings) },
                modifier = Modifier.size(43.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, border),
                color = surface,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = border.copy(alpha = .7f), thickness = .7.dp)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "hero") {
                Surface(
                    color = surface,
                    border = BorderStroke(1.dp, border),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 19.dp, bottom = 8.dp)) {
                        Text(
                            text = "Книга, разбитая на главы, темы и средства",
                            fontFamily = WebLiterataFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            lineHeight = 31.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(Modifier.fillMaxWidth().heightIn(min = 155.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Полный русский текст с поиском, заметками, источниками и офлайн-доступом.",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 21.sp,
                            )
                            Image(
                                painter = painterResource(R.drawable.premium_hero),
                                contentDescription = null,
                                modifier = Modifier.width(170.dp).height(155.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }
            }
            item(key = "resume") {
                Surface(
                    color = surface,
                    border = BorderStroke(1.dp, border),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp,
                ) {
                    Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp)) {
                                Text(
                                    "Продолжить чтение",
                                    Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                "$progress%",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.premium_reading),
                                contentDescription = null,
                                modifier = Modifier.size(112.dp),
                                contentScale = ContentScale.Fit,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                currentTitle,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = WebLiterataFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                lineHeight = 24.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        // Custom track avoids Material3's trailing stop-dot at 0%.
                        Box(
                            Modifier.fillMaxWidth().height(5.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            if (animatedProgress > 0f) {
                                Box(
                                    Modifier.fillMaxHeight().fillMaxWidth(animatedProgress)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                        Button(
                            onClick = onContinue,
                            modifier = Modifier.fillMaxWidth().height(49.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text(
                                if (last != null) "Продолжить →" else "Начать чтение →",
                                fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            item(key = "search") {
                Surface(
                    onClick = onGlobalSearch,
                    color = surface,
                    border = BorderStroke(1.dp, border),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(painterResource(R.drawable.premium_ic_search), null, Modifier.size(34.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Поиск по всей книге", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        Text("›", fontSize = 27.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item(key = "treatments") {
                Surface(
                    onClick = { navigate(Route.Treatments) },
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .22f)),
                    shape = RoundedCornerShape(19.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 86.dp).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(65.dp), contentAlignment = Alignment.Center) {
                            Image(painterResource(R.drawable.premium_ic_remedy), null, Modifier.size(56.dp))
                        }
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Как лечили / что применялось",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                "Состояние → средства → полный текст",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                            )
                        }
                        Text("›", fontSize = 26.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            val cards = listOf(
                PremiumSection("Читать книгу", "${book.chapters.size} глав", R.drawable.premium_ic_book, Route.Book),
                PremiumSection("Темы", "${book.topics.size} разделов", R.drawable.premium_ic_topics, Route.Topics),
                PremiumSection("Средства", "${book.remedies.size} средств", R.drawable.premium_ic_remedy, Route.Remedies),
                PremiumSection("Закладки", "${store.bookmarks.size} сохранено", R.drawable.premium_ic_bookmark, Route.Bookmarks),
                PremiumSection("Заметки", "${store.notes.size} записей", R.drawable.premium_ic_notes, Route.Notes),
                PremiumSection("Источники", "Ссылки и данные", R.drawable.premium_ic_source, Route.Source),
            )
            items(3, key = { "sections$it" }) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    PremiumSectionCard(cards[row * 2], Modifier.weight(1f), navigate)
                    PremiumSectionCard(cards[row * 2 + 1], Modifier.weight(1f), navigate)
                }
            }
            if (book.collections.isNotEmpty()) {
                item(key = "collections") {
                    Surface(
                        onClick = { navigate(Route.Collections) }, color = surface,
                        border = BorderStroke(1.dp, border),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(17.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(painterResource(R.drawable.premium_ic_book), null, Modifier.size(36.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Подборки", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text("Все →", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

private data class PremiumSection(val title: String, val meta: String, val imageId: Int, val route: Route)

@Composable
private fun PremiumSectionCard(
    section: PremiumSection,
    modifier: Modifier,
    navigate: (Route) -> Unit,
) {
    Surface(
        onClick = { navigate(section.route) },
        modifier = modifier.height(145.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
    ) {
        Box(Modifier.fillMaxSize().padding(13.dp)) {
            Image(
                painter = painterResource(section.imageId),
                contentDescription = null,
                modifier = Modifier.size(49.dp).align(Alignment.TopStart),
            )
            Text(
                "›",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd),
            )
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    section.meta,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
