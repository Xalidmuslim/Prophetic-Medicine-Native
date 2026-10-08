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
 * Images are cropped into app resources from that reference, are offline WebP,
 * and are never fetched from the network at runtime.
 * The original book, all Route handlers, AppStore, Reader and search remain intact.
 */
private val RefDeep = Color(0xFF214B3D)
private val RefIvory = Color(0xFFF7F3EB)
private val RefGold = Color(0xFFC8A46A)
private val RefSage = Color(0xFF8FA48F)

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
    Column(modifier.fillMaxSize().background(bg)) {
        // Reference architecture continues through the title area instead of
        // disappearing behind a plain ivory app bar.
        Box(Modifier.fillMaxWidth().height(80.dp)) {
            Image(
                painter = painterResource(R.drawable.medicine_photo_arch),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd).width(202.dp).height(80.dp),
                contentScale = ContentScale.Fit,
                alpha = .48f,
            )
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 5.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(R.drawable.medicine_photo_app_icon), null,
                Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Медицина Пророка ﷺ", color = ink,
                    fontFamily = WebLiterataFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    lineHeight = 23.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Ибн Каййим аль-Джаузия",
                    fontFamily = WebModernFont, fontSize = 11.5.sp,
                    color = secondary, maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                onClick = { navigate(Route.Settings) },
                modifier = Modifier.size(42.dp),
                shape = CircleShape, color = surface,
                border = BorderStroke(1.dp, outline.copy(alpha = .65f)),
                shadowElevation = 1.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, "Настройки",
                        Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 13.dp, end = 13.dp, top = 1.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item(key = "ref-hero") {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().height(191.dp),
                ) {
                    val compact = maxWidth < 345.dp
                    Image(
                        painter = painterResource(R.drawable.medicine_photo_hero),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomEnd)
                            .width(maxWidth * .65f).height(182.dp),
                        // Never crop the bowl, pestle, oil bottle or leaves.
                        contentScale = ContentScale.Fit,
                    )
                    // Photographic crop stays visible at right, while the ivory
                    // overlay guarantees contrast behind the Russian heading.
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                0f to bg,
                                .27f to bg,
                                .48f to bg.copy(alpha = .95f),
                                .65f to bg.copy(alpha = .22f),
                                .89f to Color.Transparent,
                                1f to Color.Transparent,
                            )
                        )
                    )
                    Column(
                        modifier = Modifier.align(Alignment.TopStart)
                            .fillMaxWidth(.61f)
                            .padding(start = 6.dp, top = 8.dp),
                    ) {
                        Text(
                            "Книга, разбитая\nна главы, темы\nи средства",
                            color = ink,
                            fontFamily = WebLiterataFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (compact) 21.sp else 24.sp,
                            lineHeight = if (compact) 26.sp else 30.sp,
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            "Полный русский текст с поиском, заметками, источниками и офлайн-доступом.",
                            color = secondary,
                            fontFamily = WebModernFont,
                            fontSize = if (compact) 11.5.sp else 12.5.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    Box(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth(.47f)
                            .height(1.dp).background(RefGold.copy(alpha=.57f))
                    )
                }
            }
            item(key = "ref-reading") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = surface,
                    shape = RoundedCornerShape(19.dp),
                    border = BorderStroke(1.dp, outline),
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        Modifier.fillMaxWidth()
                            .background(Brush.horizontalGradient(
                                0f to Color(0xFFE9E9DE),
                                .46f to surface,
                                1f to surface,
                            ))
                            .heightIn(min = 168.dp)
                            .padding(start = 7.dp, end = 11.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painterResource(R.drawable.medicine_photo_reading), null,
                            modifier = Modifier.width(111.dp).height(142.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(Modifier.width(5.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
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
                                fontSize = 14.sp, lineHeight = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 4, overflow = TextOverflow.Ellipsis,
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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 75.dp),
                    color = surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, outline),
                    shadowElevation = 1.dp,
                ) {
                    Row(Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(
                            0f to Color(0xFFDDE6DB),
                            .48f to Color(0xFFEAECE0),
                            1f to Color(0xFFE0E7DD),
                        ))
                        .padding(start=2.dp,end=12.dp,top=4.dp,bottom=4.dp),
                        verticalAlignment=Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.medicine_photo_treatments), null,
                            Modifier.size(width=116.dp,height=80.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(Modifier.width(2.dp))
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            Text("Как лечили / что применялось",
                                fontFamily=WebLiterataFont,fontSize=14.sp,
                                lineHeight=18.sp,fontWeight=FontWeight.SemiBold,
                                maxLines=2,overflow=TextOverflow.Ellipsis)
                            Text("Состояние → средства → полный текст",
                                fontSize=10.sp,lineHeight=14.sp,
                                color=secondary,maxLines=2)
                        }
                        Text("›",color=RefDeep,fontSize=25.sp)
                    }
                }
            }
            val sections = listOf(
                ReferenceSection("Читать книгу", "${book.chapters.size} глав", R.drawable.medicine_photo_ic_book, Route.Book),
                ReferenceSection("Темы", "${book.topics.size} разделов", R.drawable.medicine_photo_ic_topics, Route.Topics),
                ReferenceSection("Средства", "${book.remedies.size} позиций", R.drawable.medicine_photo_ic_remedy, Route.Remedies),
                ReferenceSection("Закладки", "${store.bookmarks.size} сохранено", R.drawable.medicine_photo_ic_bookmark, Route.Bookmarks),
                ReferenceSection("Заметки", "${store.notes.size} записей", R.drawable.medicine_photo_ic_notes, Route.Notes),
                ReferenceSection("Источники", "Ссылки и описания", R.drawable.medicine_photo_ic_source, Route.Source),
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
                            Image(painterResource(R.drawable.premium_ic_book),null,Modifier.size(30.dp))
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
}

private data class ReferenceSection(
    val title:String,val meta:String,val photoRes:Int,val route:Route
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
        shadowElevation=1.dp,
    ) {
        Row(
            Modifier.fillMaxSize().padding(start=4.dp,end=7.dp,top=6.dp,bottom=6.dp),
            verticalAlignment=Alignment.CenterVertically,
        ) {
            Image(
                painterResource(section.photoRes), null,
                Modifier.size(width=54.dp,height=61.dp),
                contentScale=ContentScale.Fit,
            )
            Spacer(Modifier.width(3.dp))
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
