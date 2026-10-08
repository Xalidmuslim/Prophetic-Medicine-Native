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
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Presentation-only implementation based on the exact owner-supplied reference.
 * New original offline WebP illustrations are produced for this app,
 * and are never fetched from the network at runtime.
 * The original book, all Route handlers, AppStore, Reader and search remain intact.
 */
private val RefDeep = Color(0xFF3F5046)
private val RefIvory = Color(0xFFF8F5F0)
private val RefGold = Color(0xFFC3B39E)
private val RefSage = Color(0xFF9DA69D)

@Composable
fun ReferenceHomeScreen(
    book: BookData,
    store: AppStore,
    modifier: Modifier,
    navigate: (Route) -> Unit,
    onGlobalSearch: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    val lookup = remember(book) { book.chapters.associateBy { it.id } }
    val last = store.lastChapterId?.let(lookup::get)
    // The first entry in some editions is a cover page ("Медицина Пророка").
    // Select first readable chapter for a fresh installation without changing
    // user progress, chapter order or any stored last-chapter location.
    val firstContent = remember(book) {
        book.chapters.firstOrNull {
            val t = it.title.trim()
            t.isNotBlank() &&
                !t.equals(book.title.trim(), ignoreCase = true) &&
                !t.equals("МЕДИЦИНА ПРОРОКА", ignoreCase = true) &&
                !t.equals("МЕДИЦИНА ПРОРОКА ﷺ", ignoreCase = true)
        } ?: book.chapters.firstOrNull()
    }
    val resume = last ?: firstContent
    val displayTitle = when {
        resume == null -> "Начните с первой главы"
        resume.title.trim().startsWith("Глава:", ignoreCase = true) -> resume.title.trim()
        else -> "Глава: ${resume.title.trim()}"
    }
    val complete = book.chapters.count { chapter ->
        val pos = store.progress[chapter.id]
        pos != null && chapter.blocks.isNotEmpty() &&
            pos.toFloat() >= (chapter.blocks.size - 1).toFloat() * .96f
    }
    val percent = if (book.chapters.isEmpty()) 0 else
        (100f * complete / book.chapters.size).toInt().coerceIn(0, 100)
    val fraction by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(durationMillis = 400),
        label = "homeReadingProgress",
    )
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    val bg = MaterialTheme.colorScheme.background
    val ink = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    // Home masthead and introductory text share ONE full-bleed original
    // botanical photograph; no separate squared image or clipped overlay.
    LazyColumn(
        modifier = modifier.fillMaxSize().background(bg),
        contentPadding = PaddingValues(start = 13.dp, end = 13.dp, top = 0.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        item(key = "ref-hero") {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(295.dp),
            ) {
                val compact = maxWidth < 345.dp
                Image(
                    painter = painterResource(R.drawable.medicine_minimal_hero),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            0f to bg.copy(alpha = .32f),
                            .31f to bg.copy(alpha = .21f),
                            .56f to bg.copy(alpha = .03f),
                            1f to Color.Transparent,
                        )
                    )
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(start = 2.dp, end = 2.dp, top = 5.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.medicine_photo_app_icon),
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Медицина Пророка ﷺ", color = ink,
                                fontFamily = WebLiterataFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp, lineHeight = 23.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Ибн Каййим аль-Джаузия",
                                fontFamily = WebModernFont,
                                fontSize = 11.5.sp, color = secondary,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Surface(
                            onClick = { navigate(Route.Settings) },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape, color = surface.copy(alpha = .89f),
                            border = BorderStroke(1.dp, outline.copy(alpha = .65f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Settings, "Настройки",
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(15.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(.65f).padding(start = 7.dp),
                    ) {
                        Text(
                            "Книга, разбитая\nна главы, темы\nи средства",
                            color = ink, fontFamily = WebLiterataFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (compact) 21.sp else 24.sp,
                            lineHeight = if (compact) 26.sp else 30.sp,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Полный русский текст с поиском, заметками, источниками и офлайн-доступом.",
                            color = secondary, fontFamily = WebModernFont,
                            fontSize = if (compact) 11.5.sp else 12.5.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
                Box(
                    Modifier.fillMaxWidth().height(26.dp)
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(
                            colors = listOf(Color.Transparent, bg),
                        ))
                )
            }
        }
            item(key = "ref-reading") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = surface,
                    shape = RoundedCornerShape(19.dp),
                    border = BorderStroke(1.dp, outline),
                    shadowElevation = 1.dp,
                ) {
                    BoxWithConstraints(Modifier.fillMaxWidth().height(192.dp)) {
                        val compact = maxWidth < 345.dp
                        Image(
                            painter = painterResource(R.drawable.medicine_minimal_card),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.horizontalGradient(
                                    0f to Color.Transparent,
                                    .29f to surface.copy(alpha = .12f),
                                    .42f to surface.copy(alpha = .84f),
                                    .73f to surface.copy(alpha = .94f),
                                    1f to surface.copy(alpha = .90f),
                                )
                            )
                        )
                        Column(
                            modifier = Modifier.fillMaxSize()
                                .padding(
                                    start = if (compact) 116.dp else 132.dp,
                                    end = 11.dp, top = 9.dp, bottom = 9.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .92f),
                                    shape = CircleShape,
                                ) {
                                    Text(
                                        "Продолжить чтение",
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                        fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Box(Modifier.size(39.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { fraction },
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                                        strokeWidth = 3.dp,
                                    )
                                    Text("$percent%", fontSize = 11.sp, color = RefDeep,
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Text(
                                displayTitle,
                                color = ink, fontFamily = WebLiterataFont,
                                fontSize = 13.5.sp, lineHeight = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 3, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape)
                                .background(outline.copy(alpha = .48f))) {
                                if (fraction > 0f) {
                                    Box(Modifier.fillMaxHeight().fillMaxWidth(fraction)
                                        .background(MaterialTheme.colorScheme.primary))
                                }
                            }
                            Button(
                                onClick = {
                                    resume?.let { navigate(Route.Reader(it.id, resume = last != null)) }
                                },
                                enabled = resume != null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(41.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RefDeep),
                                contentPadding = PaddingValues(horizontal=5.dp, vertical=0.dp),
                            ) {
                                Text(
                                    if (last == null) "Начать чтение →" else "Продолжить →",
                                    fontFamily = WebLiterataFont,
                                    fontSize = 12.5.sp, maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
            item(key = "ref-search") {
                Surface(
                    onClick = onGlobalSearch,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    color = surface, shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, outline),
                    shadowElevation = 1.dp,
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal=12.dp),
                        verticalAlignment=Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.premium_ic_search),null,Modifier.size(30.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Поиск по всей книге", Modifier.weight(1f),
                            fontFamily = WebModernFont, fontSize=13.sp, color=ink)
                        Text("›", fontSize=25.sp, color=RefDeep)
                    }
                }
            }
            item(key = "ref-treatments") {
                Surface(
                    onClick = { navigate(Route.Treatments) },
                    modifier = Modifier.fillMaxWidth().height(84.dp),
                    color = surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, outline),
                    shadowElevation = 1.dp,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(R.drawable.medicine_minimal_card),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.horizontalGradient(
                                    0f to Color.Transparent,
                                    .31f to surface.copy(alpha = .20f),
                                    .49f to surface.copy(alpha = .88f),
                                    1f to surface.copy(alpha = .91f),
                                )
                            )
                        )
                        Row(
                            Modifier.fillMaxSize().padding(start = 116.dp, end = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    "Как лечили / что применялось",
                                    fontFamily = WebLiterataFont, fontSize = 13.5.sp,
                                    lineHeight = 18.sp, fontWeight = FontWeight.SemiBold,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "Состояние → средства → полный текст",
                                    fontSize = 10.sp, lineHeight = 14.sp,
                                    color = secondary, maxLines = 2,
                                )
                            }
                            Text("›", color = RefDeep, fontSize = 25.sp)
                        }
                    }
                }
            }
            val sections = listOf(
                ReferenceSection("Читать книгу", "${book.chapters.size} глав", Icons.Outlined.MenuBook, Route.Book),
                ReferenceSection("Темы", "${book.topics.size} разделов", Icons.Outlined.GridView, Route.Topics),
                ReferenceSection("Средства", "${book.remedies.size} позиций", Icons.Outlined.Science, Route.Remedies),
                ReferenceSection("Закладки", "${store.bookmarks.size} сохранено", Icons.Outlined.BookmarkBorder, Route.Bookmarks),
                ReferenceSection("Заметки", "${store.notes.size} записей", Icons.Outlined.EditNote, Route.Notes),
                ReferenceSection("Источники", "Ссылки и описания", Icons.Outlined.LibraryBooks, Route.Source),
            )
            items(3,key={"ref-row-$it"}) { index ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    ReferenceSectionCard(sections[index*2],Modifier.weight(1f),navigate)
                    ReferenceSectionCard(sections[index*2+1],Modifier.weight(1f),navigate)
                }
            }
            if (book.collections.isNotEmpty()) {
                item(key = "ref-collections") {
                    Surface(
                        onClick = { navigate(Route.Collections) },
                        shape = RoundedCornerShape(17.dp),color=surface,
                        border = BorderStroke(1.dp,outline),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(13.dp),verticalAlignment=Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Dashboard, null, Modifier.size(25.dp), tint=MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Быстрые подборки",Modifier.weight(1f),
                                fontFamily=WebModernFont,fontSize=13.sp)
                            Text("Все →",fontSize=12.sp,color=RefDeep)
                        }
                    }
                }
            }
        }
}

private data class ReferenceSection(
    val title:String,val meta:String,val icon:ImageVector,val route:Route
)

@Composable
private fun ReferenceSectionCard(
    section:ReferenceSection, modifier:Modifier, navigate:(Route)->Unit
) {
    Surface(
        onClick={navigate(section.route)},
        modifier=modifier.height(79.dp),
        color=MaterialTheme.colorScheme.surface,
        shape=RoundedCornerShape(17.dp),
        border=BorderStroke(1.dp,MaterialTheme.colorScheme.outline),
        shadowElevation=0.dp,
    ) {
        Row(
            Modifier.fillMaxSize().padding(start=4.dp,end=7.dp,top=6.dp,bottom=6.dp),
            verticalAlignment=Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.Center) {
                Text(
                    section.title,
                    fontFamily=WebModernFont,fontWeight=FontWeight.SemiBold,
                    fontSize=12.sp,lineHeight=15.sp,maxLines=2,
                    overflow=TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    section.meta, color=MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize=9.5.sp,lineHeight=12.sp,maxLines=2,
                    overflow=TextOverflow.Ellipsis,
                )
            }
            Text("›",fontSize=18.sp,color=MaterialTheme.colorScheme.onSurface)
        }
    }
}
