package com.seoul.fmonair

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RadioPlaybackService.resetExitSignal()

        lifecycleScope.launch {
            RadioPlaybackService.shouldExitAppFlow.collect { shouldExit ->
                if (shouldExit) {
                    RadioPlaybackService.resetExitSignal()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask()
                    } else {
                        finishAffinity()
                    }
                }
            }
        }

        setContent {
            val darkGlassColorScheme = darkColorScheme(
                primary = Color(0xFF38BDF8),
                secondary = Color(0xFF818CF8),
                background = Color(0xFF0B1120),
                surface = Color(0xFF111827),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFE2E8F0)
            )
            MaterialTheme(colorScheme = darkGlassColorScheme) {
                SeoulRadioAppScreen()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (RadioPlaybackService.isPlayingFlow.value) {
            val ensureIntent = Intent(this, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_ENSURE_NOTIFICATION
            }
            startService(ensureIntent)
            Toast.makeText(
                this,
                "상단 알림창에서 라디오 재생 중지 및 앱 종료가 가능합니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun SeoulRadioAppScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var stations by remember { mutableStateOf(StationRepository.loadStations(context)) }
    var favoriteIds by remember { mutableStateOf(StationRepository.getFavoriteIds(context)) }

    val currentStation by RadioPlaybackService.currentStationFlow.collectAsState()
    val currentProgram by RadioPlaybackService.currentProgramFlow.collectAsState()
    val isPlaying by RadioPlaybackService.isPlayingFlow.collectAsState()
    val streamStatus by RadioPlaybackService.streamStatusFlow.collectAsState()
    val liveIcyTitle by RadioPlaybackService.liveMetadataTitleFlow.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(StationCategory.ALL) }
    var channelSearchQuery by remember { mutableStateOf("") }

    var isHeroCompact by remember { mutableStateOf(false) }

    val maxVol = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volumeLevel by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol)
    }

    var selectedSleepOption by remember { mutableStateOf(SleepTimerOption.OFF) }
    var remainingSleepSeconds by remember { mutableIntStateOf(0) }

    var showSleepTimerModal by remember { mutableStateOf(false) }
    var showCustomUrlModal by remember { mutableStateOf(false) }
    var showApkGuideModal by remember { mutableStateOf(false) }

    var showMelonWebModal by remember { mutableStateOf(false) }
    var melonSearchKeyword by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && isPlaying) {
            val ensureIntent = Intent(context, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_ENSURE_NOTIFICATION
            }
            context.startService(ensureIntent)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val defaultLyricLines = remember {
        listOf(
            "상단 [BGM 인식]은 ACRCloud + AudD 핑거프린팅으로 곡을 정밀 식별합니다",
            "우측 상단 [연필 아이콘]에서 무료 ACRCloud 키 또는 AudD 토큰을 설정할 수 있습니다",
            "[🍈 멜론] 창에서 [카카오계정 로그인]으로 로그인해 바로 검색·감상하세요",
            "홈 버튼을 눌러도 상단 알림창에서 중지·종료할 수 있습니다"
        )
    }

    var currentSong by remember {
        mutableStateOf(
            RecognizedSong(
                id = "init_song",
                title = "BGM 인식 또는 가사 검색을 실행해 주세요",
                artist = "수도권 온에어 FM 라디오",
                album = "ACRCloud / AudD 핑거프린팅 대기",
                stationName = currentStation.name,
                frequencyText = currentStation.frequencyText,
                recognizedAt = "대기 중",
                plainLyrics = defaultLyricLines.joinToString(separator = "\n"),
                syncedLyrics = defaultLyricLines.mapIndexed { idx, line -> LyricLine(idx * 3.5f, line) },
                sourceMethod = "ACRCloud / AudD + 멜론 연동"
            )
        )
    }
    var isRecognizingAudio by remember { mutableStateOf(false) }
    var lyricsSearchQuery by remember { mutableStateOf("") }
    var lrcSearchResults by remember { mutableStateOf<List<RecognizedSong>>(emptyList()) }
    var isSearchingLyrics by remember { mutableStateOf(false) }
    var isLyricsFocusMode by remember { mutableStateOf(false) }
    var activeLyricLineIndex by remember { mutableIntStateOf(0) }

    var savedSongsHistory by remember {
        mutableStateOf<List<RecognizedSong>>(SongAndLyricsService.loadSavedSongs(context))
    }

    fun buildCleanMelonKeyword(song: RecognizedSong): String {
        val isPlaceholder = song.id == "init_song" || song.title.startsWith("현재 곡 미감지") || song.title.startsWith("BGM 인식")
        if (isPlaceholder) {
            return lyricsSearchQuery.trim()
        }
        return "${song.artist} ${song.title}".trim()
    }

    fun openMelonSearchModal(customKeyword: String? = null) {
        melonSearchKeyword = customKeyword ?: buildCleanMelonKeyword(currentSong)
        showMelonWebModal = true
    }

    LaunchedEffect(liveIcyTitle) {
        val icy = liveIcyTitle
        if (!icy.isNullOrBlank() && icy.contains("-")) {
            val recognized = SongAndLyricsService.recognizeCurrentRadioSong(
                context = context,
                station = currentStation,
                icyMetadataTitle = icy
            )
            if (!recognized.title.startsWith("현재 곡 미감지")) {
                currentSong = recognized
                lyricsSearchQuery = "${recognized.artist} ${recognized.title}".trim()
                savedSongsHistory = SongAndLyricsService.saveSongToHistory(context, recognized)
            }
        }
    }

    LaunchedEffect(currentSong.id, isPlaying) {
        activeLyricLineIndex = 0
        while (true) {
            delay(3500)
            if (currentSong.syncedLyrics.isNotEmpty()) {
                activeLyricLineIndex = (activeLyricLineIndex + 1) % currentSong.syncedLyrics.size
            }
        }
    }

    LaunchedEffect(remainingSleepSeconds) {
        if (remainingSleepSeconds > 0) {
            delay(1000)
            remainingSleepSeconds -= 1
            if (remainingSleepSeconds == 0) {
                selectedSleepOption = SleepTimerOption.OFF
                if (isPlaying) {
                    val stopIntent = Intent(context, RadioPlaybackService::class.java).apply {
                        action = RadioPlaybackService.ACTION_STOP_PLAYBACK
                    }
                    context.startService(stopIntent)
                }
                Toast.makeText(context, "취침예약 시간이 종료되어 라디오를 자동으로 정지했습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun playStation(station: RadioStation) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_PLAY_STATION
            putExtra(RadioPlaybackService.EXTRA_STATION_ID, station.id)
        }
        context.startService(intent)
    }

    fun togglePlayPause() {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_TOGGLE_PLAY
        }
        context.startService(intent)
    }

    fun stopPlayback() {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_STOP_PLAYBACK
        }
        context.startService(intent)
    }

    fun exitProgram() {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_EXIT_APP
        }
        context.startService(intent)
    }

    fun runSongRecognition() {
        if (isRecognizingAudio) return
        isRecognizingAudio = true
        Toast.makeText(context, "5.5초 고감도 녹음 및 ACRCloud/AudD 핑거프린팅 분석 중...", Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            val recognized = SongAndLyricsService.recognizeCurrentRadioSong(
                context = context,
                station = currentStation,
                icyMetadataTitle = liveIcyTitle
            )
            currentSong = recognized
            if (!recognized.title.startsWith("현재 곡 미감지")) {
                lyricsSearchQuery = "${recognized.artist} ${recognized.title}".trim()
                savedSongsHistory = SongAndLyricsService.saveSongToHistory(context, recognized)
                Toast.makeText(context, "곡 인식 성공: ${recognized.artist} - ${recognized.title}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "음원이 감지되지 않아 가사/멜론 검색 탭으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
            isRecognizingAudio = false
            selectedTabIndex = 1
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        runSongRecognition()
    }

    fun triggerAudioRecognitionWithPermission() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            runSongRecognition()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val filteredStations = stations.filter { station ->
        val matchesCategory = when (selectedCategory) {
            StationCategory.ALL -> true
            StationCategory.FAVORITE -> favoriteIds.contains(station.id)
            else -> station.category == selectedCategory
        }
        val q = channelSearchQuery.trim()
        val scheduledProg = StationRepository.getScheduledProgramTitle(station)
        val matchesSearch = q.isEmpty() ||
            station.name.contains(q, ignoreCase = true) ||
            station.frequencyText.contains(q, ignoreCase = true) ||
            station.defaultProgram.contains(q, ignoreCase = true) ||
            scheduledProg.contains(q, ignoreCase = true) ||
            station.broadcaster.contains(q, ignoreCase = true)

        matchesCategory && matchesSearch
    }

    Scaffold(
        containerColor = Color(0xFF0B1120),
        topBar = {
            TopHeaderBar(
                isPlaying = isPlaying,
                currentStation = currentStation,
                currentProgram = currentProgram,
                streamStatus = streamStatus,
                remainingSleepSeconds = remainingSleepSeconds,
                onTogglePlay = { togglePlayPause() },
                onStopPlay = { stopPlayback() },
                onExitApp = { exitProgram() },
                onOpenSleepModal = { showSleepTimerModal = true },
                onOpenCustomUrlModal = { showCustomUrlModal = true },
                onOpenApkGuideModal = { showApkGuideModal = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1120), Color(0xFF111827), Color(0xFF0F172A))
                    )
                )
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            if (!isLyricsFocusMode || selectedTabIndex != 1) {
                item {
                    NowPlayingHeroCard(
                        station = currentStation,
                        currentProgram = currentProgram,
                        isPlaying = isPlaying,
                        streamStatus = streamStatus,
                        isFavorite = favoriteIds.contains(currentStation.id),
                        volumeLevel = volumeLevel,
                        isRecognizingAudio = isRecognizingAudio,
                        isCompact = isHeroCompact,
                        onToggleCompact = { isHeroCompact = !isHeroCompact },
                        onTogglePlay = { togglePlayPause() },
                        onStopPlay = { stopPlayback() },
                        onPrevChannel = {
                            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                                action = RadioPlaybackService.ACTION_PREV_STATION
                            }
                            context.startService(intent)
                        },
                        onNextChannel = {
                            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                                action = RadioPlaybackService.ACTION_NEXT_STATION
                            }
                            context.startService(intent)
                        },
                        onToggleFavorite = {
                            favoriteIds = StationRepository.toggleFavorite(context, currentStation.id)
                        },
                        onVolumeChange = { newVol ->
                            volumeLevel = newVol
                            val targetVol = (newVol * maxVol).toInt()
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        },
                        onJumpToLyrics = {
                            selectedTabIndex = 1
                        },
                        onRecognizeAudio5Sec = {
                            triggerAudioRecognitionWithPermission()
                        },
                        onOpenMelonSearch = {
                            openMelonSearchModal()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFF111827).copy(alpha = 0.95f),
                    contentColor = Color(0xFF38BDF8),
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF38BDF8)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                "수도권 채널 (${stations.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                "가사·멜론검색",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Lyrics, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Text(
                                "노래 보관함 (${savedSongsHistory.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (selectedTabIndex) {
                0 -> {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StationCategory.entries.forEach { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category },
                                        label = { Text(category.displayName, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF2563EB),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF1E293B),
                                            labelColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = channelSearchQuery,
                                onValueChange = { channelSearchQuery = it },
                                placeholder = { Text("채널명, 주파수(MHz), 프로그램명 검색...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF111827),
                                    unfocusedContainerColor = Color(0xFF111827),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    val rows = filteredStations.chunked(2)
                    items(rows) { rowPair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPair.forEach { station ->
                                val isCurrent = station.id == currentStation.id
                                val displayProgram = if (isCurrent && currentProgram.isNotBlank()) {
                                    currentProgram
                                } else {
                                    StationRepository.getScheduledProgramTitle(station)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    StationGridCard(
                                        station = station,
                                        programTitle = displayProgram,
                                        isSelected = isCurrent,
                                        isPlaying = isPlaying,
                                        isFav = favoriteIds.contains(station.id),
                                        onSelectStation = { playStation(station) },
                                        onToggleFavorite = {
                                            favoriteIds = StationRepository.toggleFavorite(context, station.id)
                                        }
                                    )
                                }
                            }
                            if (rowPair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        RealtimeLyricsSection(
                            currentStation = currentStation,
                            currentProgram = currentProgram,
                            currentSong = currentSong,
                            activeLyricIndex = activeLyricLineIndex,
                            isFocusMode = isLyricsFocusMode,
                            searchQuery = lyricsSearchQuery,
                            isSearching = isSearchingLyrics,
                            searchResults = lrcSearchResults,
                            onToggleFocusMode = { isLyricsFocusMode = !isLyricsFocusMode },
                            onSearchQueryChange = { lyricsSearchQuery = it },
                            onExecuteLrcSearch = {
                                if (lyricsSearchQuery.isNotBlank()) {
                                    isSearchingLyrics = true
                                    coroutineScope.launch {
                                        lrcSearchResults = SongAndLyricsService.searchLyricsFromLrcLib(
                                            query = lyricsSearchQuery,
                                            stationName = currentStation.name,
                                            frequencyText = currentStation.frequencyText
                                        )
                                        isSearchingLyrics = false
                                    }
                                }
                            },
                            onOpenMelonWithQuery = { keyword ->
                                openMelonSearchModal(keyword)
                            },
                            onSelectSearchedSong = { chosen ->
                                currentSong = chosen
                                savedSongsHistory = SongAndLyricsService.saveSongToHistory(context, chosen)
                                lrcSearchResults = emptyList()
                            }
                        )
                    }
                }

                2 -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "라디오에서 찾은 노래 보관함 (${savedSongsHistory.size}곡)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (savedSongsHistory.isNotEmpty()) {
                                TextButton(onClick = {
                                    savedSongsHistory = SongAndLyricsService.clearAllHistory(context)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("전체 삭제", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (savedSongsHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "아직 저장된 노래 히스토리가 없습니다. 상단 'BGM 인식' 또는 가사 검색을 실행하면 한국어 제목으로 자동 저장됩니다.",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(savedSongsHistory, key = { it.id }) { song ->
                            Surface(
                                color = Color(0xFF1E293B).copy(alpha = 0.75f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                                    .clickable {
                                        currentSong = song
                                        selectedTabIndex = 1
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${song.title} - ${song.artist}",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${song.stationName} (${song.frequencyText}) · 저장 시각: ${song.recognizedAt}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "클릭하여 가사 보기 · ${song.sourceMethod}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF00CD3C),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable {
                                                openMelonSearchModal("${song.artist} ${song.title}")
                                            }
                                        ) {
                                            Text(
                                                text = "🍈 멜론",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }

                                        IconButton(onClick = {
                                            savedSongsHistory = SongAndLyricsService.deleteSongFromHistory(context, song.id)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFF94A3B8))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSleepTimerModal) {
        SleepTimerDialog(
            currentOption = selectedSleepOption,
            remainingSeconds = remainingSleepSeconds,
            onSelectOption = { option ->
                selectedSleepOption = option
                remainingSleepSeconds = option.minutes * 60
                showSleepTimerModal = false
            },
            onDismiss = { showSleepTimerModal = false }
        )
    }

    if (showCustomUrlModal) {
        CustomStreamUrlDialog(
            context = context,
            station = currentStation,
            onSaveSettings = { newUrl, newAuddToken, acrHost, acrKey, acrSecret ->
                StationRepository.saveCustomStreamUrl(context, currentStation.id, newUrl)
                SongAndLyricsService.saveAuddApiToken(context, newAuddToken)
                SongAndLyricsService.saveAcrCloudConfig(context, acrHost, acrKey, acrSecret)
                stations = StationRepository.loadStations(context)
                val updated = stations.find { it.id == currentStation.id } ?: currentStation
                playStation(updated)
                showCustomUrlModal = false
                Toast.makeText(context, "ACRCloud / AudD 핑거프린팅 및 스트림 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCustomUrlModal = false }
        )
    }

    if (showApkGuideModal) {
        ApkBuildGuideDialog(onDismiss = { showApkGuideModal = false })
    }

    if (showMelonWebModal) {
        MelonSearchWebDialog(
            initialKeyword = melonSearchKeyword,
            onDismiss = { showMelonWebModal = false }
        )
    }
}

/**
 * 카카오계정 새 창 팝업(window.open) 및 카카오톡 앱 간편로그인을 100% 지원하는 인앱 멜론 웹뷰 모달
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MelonSearchWebDialog(
    initialKeyword: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchInput by remember { mutableStateOf(initialKeyword) }
    var mainWebViewRef by remember { mutableStateOf<WebView?>(null) }
    var popupWebViewRef by remember { mutableStateOf<WebView?>(null) }
    var containerRef by remember { mutableStateOf<FrameLayout?>(null) }

    val targetSearchUrl = remember(initialKeyword) {
        if (initialKeyword.isNotBlank()) {
            val encoded = URLEncoder.encode(initialKeyword.trim(), "UTF-8")
            "https://m2.melon.com/search/searchMcom.htm?q=$encoded"
        } else {
            "https://m2.melon.com/"
        }
    }

    fun handleCustomSchemeUrl(ctx: Context, view: WebView?, url: String): Boolean {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false
        }
        return try {
            if (url.startsWith("intent://")) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                try {
                    ctx.startActivity(intent)
                } catch (_: Exception) {
                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                    if (!fallbackUrl.isNullOrBlank()) {
                        view?.loadUrl(fallbackUrl)
                    } else {
                        val pkg = intent.`package`
                        if (!pkg.isNullOrBlank()) {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                            )
                        }
                    }
                }
            } else {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            true
        } catch (_: Exception) {
            true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun configureMelonWebView(webView: WebView, ctx: Context, isPopup: Boolean = false) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            // ★ 카카오계정 로그인 팝업(window.open) 필수 설정
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 14; SM-S928N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleCustomSchemeUrl(ctx, view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                CookieManager.getInstance().flush()
                // 로그인 완료 후 멜론 메인으로 리다이렉트되면 검색어가 있을 때 자동으로 검색 결과로 이동
                if (!isPopup && url != null && (url == "https://m2.melon.com/" || url == "https://m.melon.com/") && searchInput.isNotBlank()) {
                    val encoded = URLEncoder.encode(searchInput.trim(), "UTF-8")
                    view?.loadUrl("https://m2.melon.com/search/searchMcom.htm?q=$encoded")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val container = containerRef ?: return false
                val popup = WebView(ctx)
                configureMelonWebView(popup, ctx, isPopup = true)
                popup.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                container.addView(popup)
                popupWebViewRef = popup

                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = popup
                resultMsg?.sendToTarget()
                return true
            }

            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
                window?.let { w ->
                    containerRef?.removeView(w)
                    w.destroy()
                }
                popupWebViewRef = null
                CookieManager.getInstance().flush()
                // 카카오 팝업 로그인이 끝나고 닫히면 부모 웹뷰 새로고침/검색 실행
                if (searchInput.isNotBlank()) {
                    val encoded = URLEncoder.encode(searchInput.trim(), "UTF-8")
                    mainWebViewRef?.loadUrl("https://m2.melon.com/search/searchMcom.htm?q=$encoded")
                } else {
                    mainWebViewRef?.reload()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .border(1.5.dp, Color(0xFF00CD3C), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0B1120)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 멜론 컨트롤 바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111827))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (popupWebViewRef != null) {
                            IconButton(
                                onClick = {
                                    popupWebViewRef?.let { p ->
                                        containerRef?.removeView(p)
                                        p.destroy()
                                    }
                                    popupWebViewRef = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "팝업 닫기", tint = Color(0xFFFACC15))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Surface(
                            color = Color(0xFF00CD3C),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🍈 MELON",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (popupWebViewRef != null) "카카오/멜론 인증 중" else "멜론 검색 & 로그인",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 카카오/멜론 통합 로그인 화면 열기
                        Surface(
                            color = Color(0xFFFEE500),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable {
                                popupWebViewRef?.let { p ->
                                    containerRef?.removeView(p)
                                    p.destroy()
                                    popupWebViewRef = null
                                }
                                val retUrl = URLEncoder.encode(targetSearchUrl, "UTF-8")
                                mainWebViewRef?.loadUrl("https://member.melon.com/muid/mobile/login/login_inform.htm?returnPage=$retUrl")
                            }
                        ) {
                            Text(
                                text = "카카오·멜론 로그인",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        TextButton(
                            onClick = {
                                val q = searchInput.trim()
                                try {
                                    val encoded = URLEncoder.encode(q, "UTF-8")
                                    val externalUrl = if (q.isNotBlank()) {
                                        "https://m2.melon.com/search/searchMcom.htm?q=$encoded"
                                    } else {
                                        "https://m2.melon.com"
                                    }
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl)))
                                } catch (_: Exception) {
                                }
                            }
                        ) {
                            Text("멜론앱 열기", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                        }
                    }
                }

                // 안내 배너 (카카오계정 vs 구 멜론아이디 구분 안내)
                Surface(
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 팁: 카카오 연동 회원은 반드시 노란색 [카카오계정 로그인]을 눌러주세요. (일반 멜론아이디 탭에 이메일을 입력하면 '없는 회원'으로 표시됩니다)",
                        color = Color(0xFFFDE047),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // 검색어 입력 바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111827))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = { Text("한국어 가수·제목 입력", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00CD3C),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            popupWebViewRef?.let { p ->
                                containerRef?.removeView(p)
                                p.destroy()
                                popupWebViewRef = null
                            }
                            if (searchInput.isNotBlank()) {
                                val encoded = URLEncoder.encode(searchInput.trim(), "UTF-8")
                                mainWebViewRef?.loadUrl("https://m2.melon.com/search/searchMcom.htm?q=$encoded")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CD3C)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("멜론 검색", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }

                // 메인 웹뷰 + 카카오 팝업 웹뷰를 담는 FrameLayout 컨테이너
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            val mainWv = WebView(ctx)
                            configureMelonWebView(mainWv, ctx, isPopup = false)
                            mainWv.layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            addView(mainWv)
                            mainWv.loadUrl(targetSearchUrl)

                            mainWebViewRef = mainWv
                            containerRef = this
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StationGridCard(
    station: RadioStation,
    programTitle: String,
    isSelected: Boolean,
    isPlaying: Boolean,
    isFav: Boolean,
    onSelectStation: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.65f) else Color(0xFF1E293B).copy(alpha = 0.65f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelectStation() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(station.badgeColorHex),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = station.broadcaster,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (isSelected && isPlaying) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF16A34A),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "재생중",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "즐겨찾기",
                    tint = if (isFav) Color(0xFFFACC15) else Color(0xFF64748B),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onToggleFavorite() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = station.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = station.frequencyText,
                color = Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🎙️ $programTitle",
                color = if (isSelected) Color(0xFFFDE047) else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (station.resolverType) {
                    StreamResolverType.STATIC_HLS -> "고정 HLS (.m3u8)"
                    else -> "실시간 토큰 Resolver"
                },
                color = if (isSelected && isPlaying) Color(0xFF4ADE80) else Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TopHeaderBar(
    isPlaying: Boolean,
    currentStation: RadioStation,
    currentProgram: String,
    streamStatus: String,
    remainingSleepSeconds: Int,
    onTogglePlay: () -> Unit,
    onStopPlay: () -> Unit,
    onExitApp: () -> Unit,
    onOpenSleepModal: () -> Unit,
    onOpenCustomUrlModal: () -> Unit,
    onOpenApkGuideModal: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "topLivePulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val isBuffering = streamStatus.contains("해석") || streamStatus.contains("버퍼링") || streamStatus.contains("전환")

    Surface(
        color = Color(0xFF0B1120).copy(alpha = 0.96f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "앱 아이콘",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "수도권 FM",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = when {
                            isPlaying -> Color(0xFF065F46)
                            isBuffering -> Color(0xFF1E3A8A)
                            else -> Color(0xFF1E293B)
                        },
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = when {
                                isPlaying -> Color(0xFF4ADE80)
                                isBuffering -> Color(0xFF38BDF8)
                                else -> Color(0xFF334155)
                            },
                            shape = RoundedCornerShape(50)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .alpha(if (isPlaying || isBuffering) dotAlpha else 1f)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isPlaying -> Color(0xFF4ADE80)
                                            isBuffering -> Color(0xFF38BDF8)
                                            else -> Color(0xFF64748B)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when {
                                    isPlaying -> "LIVE 재생중"
                                    isBuffering -> "연결중..."
                                    else -> "정지됨"
                                },
                                color = when {
                                    isPlaying -> Color(0xFF4ADE80)
                                    isBuffering -> Color(0xFF38BDF8)
                                    else -> Color(0xFF94A3B8)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (remainingSleepSeconds > 0) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                        modifier = Modifier
                            .clickable { onOpenSleepModal() }
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "취침예약",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (remainingSleepSeconds > 0) {
                                    String.format("%02d:%02d", remainingSleepSeconds / 60, remainingSleepSeconds % 60)
                                } else {
                                    "취침"
                                },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenCustomUrlModal,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ACRCloud/AudD 설정",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenApkGuideModal,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "사용 가이드",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onExitApp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "프로그램 종료",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isPlaying || isBuffering) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPlaying) Color(0xFF064E3B).copy(alpha = 0.55f) else Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .border(
                            width = 1.dp,
                            color = if (isPlaying) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF38BDF8).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTogglePlay() }
                        ) {
                            FiveBarAudioEqualizer(isPlaying = isPlaying)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF2563EB),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isPlaying) "ON-AIR" else "BUFFER",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentStation.name} (${currentStation.frequencyText}) · ${currentProgram.ifBlank { currentStation.defaultProgram }}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.clickable { onStopPlay() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "재생 중지",
                                        tint = Color(0xFFFACC15),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "중지",
                                        color = Color(0xFFFACC15),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(5.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF7F1D1D),
                                modifier = Modifier.clickable { onExitApp() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "앱 종료",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "종료",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NowPlayingHeroCard(
    station: RadioStation,
    currentProgram: String,
    isPlaying: Boolean,
    streamStatus: String,
    isFavorite: Boolean,
    volumeLevel: Float,
    isRecognizingAudio: Boolean,
    isCompact: Boolean,
    onToggleCompact: () -> Unit,
    onTogglePlay: () -> Unit,
    onStopPlay: () -> Unit,
    onPrevChannel: () -> Unit,
    onNextChannel: () -> Unit,
    onToggleFavorite: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onJumpToLyrics: () -> Unit,
    onRecognizeAudio5Sec: () -> Unit,
    onOpenMelonSearch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .border(
                width = if (isPlaying) 1.5.dp else 1.dp,
                color = if (isPlaying) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.72f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(station.badgeColorHex),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = station.broadcaster,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = station.scheduleHours,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", station.frequencyMhz),
                            color = Color(0xFF38BDF8),
                            fontSize = if (isCompact) 22.sp else 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MHz",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = station.name,
                            color = Color.White,
                            fontSize = if (isCompact) 16.sp else 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isPlaying) Color(0xFF38BDF8).copy(alpha = 0.45f) else Color(0xFF334155),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF475569),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isPlaying) "ON-AIR 방송" else "편성 프로그램",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentProgram.ifBlank { station.defaultProgram },
                                color = Color(0xFFFDE047),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isPlaying) "● 실시간 스트리밍 재생 중 ($streamStatus)" else "상태: $streamStatus",
                        color = if (isPlaying) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FiveBarAudioEqualizer(isPlaying = isPlaying)
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "즐겨찾기",
                                tint = if (isFavorite) Color(0xFFFACC15) else Color(0xFF94A3B8)
                            )
                        }
                        IconButton(onClick = onToggleCompact, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isCompact) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = "접기/펼치기",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = !isCompact) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalogFmFrequencyDial(currentFrequencyMhz = station.frequencyMhz)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrevChannel,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "이전 채널", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isPlaying) {
                                        listOf(Color(0xFF059669), Color(0xFF0284C7))
                                    } else {
                                        listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
                                    }
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "재생/일시정지",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    IconButton(
                        onClick = onStopPlay,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "재생 중지", tint = Color(0xFFFACC15))
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    IconButton(
                        onClick = onNextChannel,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "다음 채널", tint = Color.White)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onRecognizeAudio5Sec,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        if (isRecognizingAudio) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("분석중", fontSize = 11.sp, color = Color(0xFF38BDF8))
                        } else {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("BGM 인식", fontSize = 11.sp, color = Color(0xFF38BDF8))
                        }
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    Button(
                        onClick = onOpenMelonSearch,
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CD3C)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("🍈 멜론", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            AnimatedVisibility(visible = !isCompact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "볼륨",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volumeLevel,
                        onValueChange = onVolumeChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0xFF0F172A)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(volumeLevel * 100).toInt()}%",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnalogFmFrequencyDial(currentFrequencyMhz: Float) {
    val clamped = currentFrequencyMhz.coerceIn(88.0f, 108.0f)
    val fraction by animateFloatAsState(
        targetValue = (clamped - 88.0f) / 20.0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "dialPointer"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090D16))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("88", "92", "96", "100", "104", "108 MHz").forEach { label ->
                Text(
                    text = label,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            val width = size.width
            val height = size.height
            val tickCount = 40

            for (i in 0..tickCount) {
                val x = (i.toFloat() / tickCount) * width
                val isMajor = i % 4 == 0
                val tickTop = if (isMajor) 2f else height * 0.35f
                drawLine(
                    color = if (isMajor) Color(0xFF94A3B8) else Color(0xFF475569),
                    start = Offset(x, tickTop),
                    end = Offset(x, height - 2f),
                    strokeWidth = if (isMajor) 2f else 1f
                )
            }

            val needleX = fraction * width
            drawLine(
                color = Color(0xFFEF4444),
                start = Offset(needleX, 0f),
                end = Offset(needleX, height),
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFFACC15),
                radius = 5f,
                center = Offset(needleX, height / 2f)
            )
        }
    }
}

@Composable
fun FiveBarAudioEqualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "eqTransition")
    val durations = listOf(380, 520, 310, 460, 350)

    Row(
        modifier = Modifier
            .height(22.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        durations.forEachIndexed { idx, dur ->
            val animRatio by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = dur, delayMillis = idx * 70),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$idx"
            )
            val barFraction = if (isPlaying) animRatio else 0.18f
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((20 * barFraction).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPlaying) Color(0xFF4ADE80) else Color(0xFF475569)
                    )
            )
        }
    }
}

@Composable
fun RealtimeLyricsSection(
    currentStation: RadioStation,
    currentProgram: String,
    currentSong: RecognizedSong,
    activeLyricIndex: Int,
    isFocusMode: Boolean,
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<RecognizedSong>,
    onToggleFocusMode: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExecuteLrcSearch: () -> Unit,
    onOpenMelonWithQuery: (String) -> Unit,
    onSelectSearchedSong: (RecognizedSong) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        if (!isFocusMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("한국어 노래 제목·가수·가사 입력", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF111827),
                        unfocusedContainerColor = Color(0xFF111827),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onExecuteLrcSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("가사 검색", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        val q = searchQuery.ifBlank {
                            if (currentSong.id != "init_song" && !currentSong.title.startsWith("현재 곡 미감지")) {
                                "${currentSong.artist} ${currentSong.title}"
                            } else ""
                        }
                        onOpenMelonWithQuery(q)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CD3C)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("🍈 멜론 검색", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        searchResults.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSearchedSong(item) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${item.artist} · ${item.album}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Text("가사 보기", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .border(1.dp, Color(0xFF00CD3C), RoundedCornerShape(50))
                        .clickable {
                            val q = if (currentSong.id != "init_song" && !currentSong.title.startsWith("현재 곡 미감지")) {
                                "${currentSong.artist} ${currentSong.title}"
                            } else searchQuery
                            onOpenMelonWithQuery(q)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🍈 멜론 로그인·즉시검색", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                val quickLinks = listOf(
                    "${currentStation.name} 선곡표" to currentStation.playlistWebUrl,
                    "KBS 콩 선곡표" to "https://program.kbs.co.kr/2fm/radio/main.html",
                    "MBC 미니 선곡표" to "https://www.imbc.com/broad/radio/",
                    "SBS 고릴라 선곡표" to "https://www.sbs.co.kr/radio",
                    "CBS 레인보우 선곡표" to "https://www.cbs.co.kr/radio"
                )
                quickLinks.forEach { (label, url) ->
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.85f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF9333EA)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentSong.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${currentSong.artist} · ${currentSong.stationName} ($currentProgram)",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "방식: ${currentSong.sourceMethod}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF00CD3C),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                val q = if (currentSong.id != "init_song" && !currentSong.title.startsWith("현재 곡 미감지")) {
                                    "${currentSong.artist} ${currentSong.title}"
                                } else searchQuery
                                onOpenMelonWithQuery(q)
                            }
                        ) {
                            Text(
                                text = "🍈 멜론에서 보기",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("lyrics", currentSong.plainLyrics))
                                Toast.makeText(context, "가사 전체가 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "가사 복사", tint = Color(0xFFCBD5E1))
                        }

                        TextButton(onClick = onToggleFocusMode) {
                            Text(
                                text = if (isFocusMode) "집중모드 해제" else "집중 모드",
                                color = Color(0xFFFACC15),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = Color(0xFF090D16),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentSong.syncedLyrics.forEachIndexed { index, line ->
                    val isHighlighted = index == activeLyricIndex
                    Text(
                        text = line.text,
                        color = if (isHighlighted) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        fontSize = if (isHighlighted) 18.sp else 15.sp,
                        fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isHighlighted) Color(0xFF1E293B).copy(alpha = 0.65f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SleepTimerDialog(
    currentOption: SleepTimerOption,
    remainingSeconds: Int,
    onSelectOption: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text("취침예약 타이머 설정", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (remainingSeconds > 0) {
                    Text(
                        text = "현재 남은 시간: ${remainingSeconds / 60}분 ${remainingSeconds % 60}초 후 자동 종료",
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                SleepTimerOption.entries.forEach { option ->
                    Button(
                        onClick = { onSelectOption(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentOption == option) Color(0xFF2563EB) else Color(0xFF0F172A)
                        )
                    ) {
                        Text(option.label, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = Color(0xFF38BDF8))
            }
        }
    )
}

@Composable
fun CustomStreamUrlDialog(
    context: Context,
    station: RadioStation,
    onSaveSettings: (String?, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val acrConfig = remember { SongAndLyricsService.getAcrCloudConfig(context) }
    var inputUrl by remember { mutableStateOf(station.customStreamUrl ?: "") }
    var inputAcrHost by remember { mutableStateOf(acrConfig.host) }
    var inputAcrKey by remember { mutableStateOf(acrConfig.accessKey) }
    var inputAcrSecret by remember { mutableStateOf(acrConfig.accessSecret) }
    var inputAuddToken by remember { mutableStateOf(SongAndLyricsService.getAuddApiToken(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text("ACRCloud / AudD 핑거프린팅 & 스트림 설정", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "1. ACRCloud 오디오 핑거프린팅 설정 (1순위 추천)",
                    color = Color(0xFF4ADE80),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "console.acrcloud.com 무료 가입 후 Audio & Video Recognition 프로젝트의 Host, Access Key, Access Secret을 입력하면 한국 노래 인식률이 비약적으로 상승합니다.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = inputAcrHost,
                    onValueChange = { inputAcrHost = it },
                    label = { Text("ACRCloud Host", fontSize = 11.sp) },
                    placeholder = { Text("identify-ap-southeast-1.acrcloud.com", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4ADE80),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputAcrKey,
                    onValueChange = { inputAcrKey = it },
                    label = { Text("ACRCloud Access Key", fontSize = 11.sp) },
                    placeholder = { Text("Access Key 입력", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4ADE80),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputAcrSecret,
                    onValueChange = { inputAcrSecret = it },
                    label = { Text("ACRCloud Access Secret", fontSize = 11.sp) },
                    placeholder = { Text("Access Secret 입력", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4ADE80),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "2. AudD 오디오 핑거프린팅 API 토큰 (2순위 보조)",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ACRCloud 미설정 시 사용됩니다. api.audd.io에서 발급받은 토큰을 입력할 수 있습니다.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputAuddToken,
                    onValueChange = { inputAuddToken = it },
                    placeholder = { Text("test", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "3. ${station.name} 커스텀 스트림 URL",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("비워두면 기본 온에어 주소 사용", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onSaveSettings(null, inputAuddToken, inputAcrHost, inputAcrKey, inputAcrSecret) }) {
                Text("스트림 기본값 복원", color = Color(0xFFFACC15))
            }
        },
        confirmButton = {
            Button(onClick = { onSaveSettings(inputUrl, inputAuddToken, inputAcrHost, inputAcrKey, inputAcrSecret) }) {
                Text("저장")
            }
        }
    )
}

@Composable
fun ApkBuildGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text("ACRCloud 핑거프린팅 & 멜론 로그인 팁", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "• [BGM 인식]은 16kHz 광대역 녹음과 자동 음량 증폭을 거친 뒤 ACRCloud 및 AudD 오디오 핑거프린팅 엔진으로 곡을 식별합니다.",
                    color = Color(0xFF4ADE80),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "• 우측 상단 [연필 아이콘]을 눌러 무료 ACRCloud 키(Host / Access Key / Secret)를 입력해 두면 한국 가요·팝송 인식률이 대폭 올라갑니다.",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "• [🍈 멜론] 창에서 로그인할 때 카카오 연동 회원은 노란색 [카카오·멜론 로그인] -> [카카오계정 로그인]을 눌러주세요.",
                    color = Color(0xFFFDE047),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인", color = Color(0xFF38BDF8))
            }
        }
    )
}