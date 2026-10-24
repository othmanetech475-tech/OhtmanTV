@file:OptIn(androidx.media3.common.util.UnstableApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.ohtman.tv

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

val Gold = Color(0xFFF5B83D)
val Bg = Color(0xFF0A0A0F)
val Surf = Color(0xFF16161F)
val Live = Color(0xFFE53935)
val Muted = Color(0xFF9A9AA8)

fun grad(c: Long) = Brush.linearGradient(listOf(Color(c), Color(0xFF14141C)))
val featured = listOf(alArabiya) + sections[1].channels.take(1) + sections[2].channels.take(1)
val allChannels = sections.flatMap { it.channels }

class Favs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("ohtman", 0)
    val ids = mutableStateListOf<String>().apply { addAll(sp.getStringSet("f", emptySet()) ?: emptySet()) }
    fun toggle(id: String) {
        if (id in ids) ids.remove(id) else ids.add(id)
        sp.edit().putStringSet("f", ids.toSet()).apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current
    val favs = remember { Favs(ctx) }
    var tab by remember { mutableStateOf(0) }
    var current by remember { mutableStateOf<Channel?>(null) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, background = Bg, surface = Surf)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(Modifier.fillMaxSize().background(Bg)) {
                val c = current
                if (c != null) {
                    PlayerScreen(c, favs, onBack = { current = null }, onPick = { current = it })
                } else {
                    val play: (Channel) -> Unit = { current = it }
                    Scaffold(containerColor = Bg, topBar = { TopNav(tab) { tab = it } }) { pad ->
                        when {
                            tab == 0 -> Home(pad, play)
                            tab == 1 -> Matches(pad) { id -> allChannels.firstOrNull { it.id == id }?.let(play) }
                            tab >= 2 -> CategoryScreen(pad, sections[tab - 2], play)
                            tab == -1 -> Favorites(pad, favs, play)
                            else -> SearchScreen(pad, play)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Logo() {
    Row(Modifier.padding(20.dp, 8.dp, 20.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("ohtman", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(4.dp))
        Text("TV", color = Bg, fontSize = 17.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(Gold).padding(7.dp, 1.dp))
    }
}

@Composable
fun LiveBadge() {
    Row(Modifier.clip(RoundedCornerShape(6.dp)).background(Live).padding(7.dp, 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White))
        Spacer(Modifier.width(4.dp))
        Text("مباشر", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionTitle(t: String) {
    Row(Modifier.padding(20.dp, 22.dp, 20.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(4.dp, 18.dp).clip(CircleShape).background(Gold))
        Spacer(Modifier.width(8.dp))
        Text(t, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Chip(label: String, sel: Boolean, onClick: () -> Unit) {
    Text(label, color = if (sel) Bg else Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(if (sel) Gold else Surf).clickable(onClick = onClick).padding(18.dp, 9.dp))
}

@Composable
fun ChannelCard(ch: Channel, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.aspectRatio(16 / 10f).clip(RoundedCornerShape(16.dp)).background(grad(ch.color)).clickable(onClick = onClick)) {
        Text(ch.name.first().toString(), color = Color.White.copy(.1f), fontSize = 100.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp))
        Box(Modifier.padding(8.dp).align(Alignment.TopStart)) { LiveBadge() }
        Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.75f)))).padding(10.dp, 18.dp, 10.dp, 10.dp)) {
            Text(ch.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
        }
    }
}

fun LazyListScope.channelGrid(list: List<Channel>, onPlay: (Channel) -> Unit) {
    items(list.chunked(2)) { row ->
        Row(Modifier.padding(16.dp, 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { ch -> ChannelCard(ch, Modifier.weight(1f)) { onPlay(ch) } }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun Hero(list: List<Channel>, onPlay: (Channel) -> Unit) {
    val st = rememberPagerState { list.size }
    LaunchedEffect(Unit) { while (true) { delay(4500); st.animateScrollToPage((st.currentPage + 1) % list.size) } }
    Column {
        HorizontalPager(st, contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp) { i ->
            val ch = list[i]
            Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(24.dp)).background(grad(ch.color)).clickable { onPlay(ch) }) {
                Text(ch.name.first().toString(), color = Color.White.copy(.1f), fontSize = 190.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.8f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    LiveBadge()
                    Spacer(Modifier.height(8.dp))
                    Text(ch.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text(ch.desc, color = Color.White.copy(.7f), fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.clip(RoundedCornerShape(50)).background(Gold).padding(16.dp, 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Bg, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("مشاهدة الآن", color = Bg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.Center) {
            repeat(list.size) { i ->
                Box(Modifier.padding(3.dp).height(6.dp).width(if (i == st.currentPage) 18.dp else 6.dp).clip(CircleShape)
                    .background(if (i == st.currentPage) Gold else Color.White.copy(.25f)))
            }
        }
    }
}

@Composable
fun TopNav(tab: Int, onTab: (Int) -> Unit) {
    val labels = listOf("الرئيسية", "المباريات") + sections.map { it.title }
    Column(Modifier.fillMaxWidth().background(Bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { Logo() }
            IconButton(onClick = { onTab(-2) }) { Icon(Icons.Filled.Search, null, tint = if (tab == -2) Gold else Color.White) }
            IconButton(onClick = { onTab(-1) }) { Icon(Icons.Filled.Favorite, null, tint = if (tab == -1) Gold else Color.White) }
            Spacer(Modifier.width(8.dp))
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
            labels.forEachIndexed { i, l ->
                Column(Modifier.clickable { onTab(i) }.padding(14.dp, 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(l, color = if (tab == i) Gold else Muted, fontSize = 15.sp, fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.height(3.dp).width(24.dp).clip(CircleShape).background(if (tab == i) Gold else Color.Transparent))
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(.07f)))
    }
}

@Composable
fun CategoryScreen(pad: PaddingValues, s: Section, onPlay: (Channel) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = pad) {
        item { SectionTitle(s.title) }
        channelGrid(s.channels, onPlay)
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun Home(pad: PaddingValues, onPlay: (Channel) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = pad) {
        item { Spacer(Modifier.height(12.dp)); Hero(featured, onPlay) }
        item {
            SectionTitle("مباريات اليوم")
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(matches.filter { it.day == "اليوم" }) { m ->
                    MatchCard(m, Modifier.width(300.dp)) { id -> allChannels.firstOrNull { it.id == id }?.let(onPlay) }
                }
            }
        }
        sections.forEach { sec ->
            item {
                SectionTitle(sec.title)
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sec.channels) { ch -> ChannelCard(ch, Modifier.width(180.dp)) { onPlay(ch) } }
                }
            }
        }
        item { SocialRow() }
    }
}

@Composable
fun Team(name: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF23232F)), contentAlignment = Alignment.Center) {
            Text(name.first().toString(), color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text(name, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun MatchCard(m: Match, modifier: Modifier = Modifier, onWatch: (String) -> Unit) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Surf).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(m.league, color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (m.live) LiveBadge()
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Team(m.home, Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                Text(if (m.live) "VS" else m.time, color = if (m.live) Live else Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(m.day, color = Muted, fontSize = 11.sp)
            }
            Team(m.away, Modifier.weight(1f))
        }
        if (m.channelId != null) {
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (m.live) Gold else Color(0xFF23232F))
                .clickable { onWatch(m.channelId) }.padding(10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PlayArrow, null, tint = if (m.live) Bg else Gold, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("مشاهدة البث", color = if (m.live) Bg else Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun Matches(pad: PaddingValues, onWatch: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = pad) {
        matches.groupBy { it.day }.forEach { (day, list) ->
            item { SectionTitle(day) }
            items(list) { m -> MatchCard(m, Modifier.padding(16.dp, 6.dp).fillMaxWidth(), onWatch) }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun Favorites(pad: PaddingValues, favs: Favs, onPlay: (Channel) -> Unit) {
    val list = allChannels.filter { it.id in favs.ids }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = pad) {
        item { SectionTitle("المفضلة") }
        if (list.isEmpty()) item {
            Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.FavoriteBorder, null, tint = Muted, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("لا توجد قنوات مفضلة بعد", color = Color.White, fontWeight = FontWeight.Bold)
                Text("اضغط على القلب داخل أي قناة لإضافتها", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        } else channelGrid(list, onPlay)
    }
}

@Composable
fun SearchScreen(pad: PaddingValues, onPlay: (Channel) -> Unit) {
    var q by remember { mutableStateOf("") }
    val res = if (q.isBlank()) allChannels else allChannels.filter { it.name.contains(q.trim(), true) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = pad) {
        item {
            TextField(value = q, onValueChange = { q = it }, singleLine = true, shape = RoundedCornerShape(16.dp),
                placeholder = { Text("ابحث عن قناة...") }, leadingIcon = { Icon(Icons.Filled.Search, null) },
                colors = TextFieldDefaults.colors(focusedContainerColor = Surf, unfocusedContainerColor = Surf,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold),
                modifier = Modifier.fillMaxWidth().padding(16.dp, 4.dp, 16.dp, 12.dp))
        }
        channelGrid(res, onPlay)
    }
}

@Composable
fun RoundBtn(modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(modifier.padding(12.dp).size(40.dp).clip(CircleShape).background(Color.Black.copy(.55f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center) { content() }
}

@Composable
fun PlayerScreen(ch: Channel, favs: Favs, onBack: () -> Unit, onPick: (Channel) -> Unit) {
    val ctx = LocalContext.current
    val act = ctx as Activity
    var full by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf(false) }
    var buffering by remember { mutableStateOf(true) }
    val player = remember { ExoPlayer.Builder(ctx).build().apply { playWhenReady = true } }

    LaunchedEffect(ch.id) { err = false; player.setMediaItem(MediaItem.fromUri(ch.url)); player.prepare() }
    DisposableEffect(Unit) {
        val l = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) { buffering = state == Player.STATE_BUFFERING }
            override fun onPlayerError(error: PlaybackException) { err = true }
        }
        player.addListener(l)
        onDispose { player.removeListener(l); player.release() }
    }
    DisposableEffect(full) {
        val ctl = WindowCompat.getInsetsController(act.window, act.window.decorView)
        if (full) {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            ctl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ctl.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ctl.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; ctl.show(WindowInsetsCompat.Type.systemBars()) }
    }
    BackHandler { if (full) full = false else onBack() }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(if (full) Modifier.fillMaxSize().background(Color.Black) else Modifier.fillMaxWidth().aspectRatio(16 / 9f).background(Color.Black)) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { c ->
                PlayerView(c).apply { this.player = player; keepScreenOn = true; setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) }
            })
            if (buffering && !err) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Gold)
            if (err) Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تعذر تشغيل البث", color = Color.White)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { err = false; player.prepare() }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Bg)) { Text("إعادة المحاولة") }
            }
            RoundBtn(Modifier.align(Alignment.TopStart), { if (full) full = false else onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            RoundBtn(Modifier.align(Alignment.TopEnd), { full = !full }) {
                Text(if (full) "⤡" else "⛶", color = Color.White, fontSize = 20.sp)
            }
        }
        if (!full) {
            val isFav = ch.id in favs.ids
            Column(Modifier.padding(20.dp)) {
                LiveBadge()
                Spacer(Modifier.height(10.dp))
                Text(ch.name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Text(ch.desc, color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Row(Modifier.clip(RoundedCornerShape(50)).background(Surf).clickable { favs.toggle(ch.id) }.padding(16.dp, 9.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = Gold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isFav) "في المفضلة" else "أضف للمفضلة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            SectionTitle("قنوات أخرى")
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(allChannels.filter { it.id != ch.id }) { c -> ChannelCard(c, Modifier.width(170.dp)) { onPick(c) } }
            }
        }
    }
}

@Composable
fun SocialRow() {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(20.dp, 28.dp, 20.dp, 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("تابعنا على", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            socials.forEach { s ->
                Box(Modifier.size(42.dp).clip(CircleShape).background(Color(s.color)).clickable {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(s.url))) } catch (_: Exception) {}
                }, contentAlignment = Alignment.Center) {
                    Text(s.glyph, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("ohtmanTV © 2026", color = Muted.copy(.6f), fontSize = 11.sp)
    }
}
