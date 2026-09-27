package com.wordmemory.app.ui.study

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordmemory.app.ui.BigButton
import com.wordmemory.app.ui.CenteredMessage
import com.wordmemory.app.ui.Loading
import com.wordmemory.app.ui.TopBar
import com.wordmemory.app.ui.TtsStatusBanner
import com.wordmemory.app.ui.appContainer
import com.wordmemory.app.ui.appViewModel
import com.wordmemory.app.ui.lessonLabel
import kotlinx.coroutines.delay

private const val MEANING_DELAY_MS = 1_500L
private const val NEXT_WORD_DELAY_MS = 5_000L

/** Listen mode: word -> pronunciation -> meaning -> next word after ~5 s (wiki/learning/learning-modes.md). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListenScreen(lessonId: Long, onBack: () -> Unit, onType: () -> Unit) {
    val speaker = appContainer().speaker
    val vm = appViewModel { LessonWordsViewModel(lessonId, it.repository) }
    val data by vm.data.collectAsStateWithLifecycle()
    val lesson = data ?: return Loading()
    val words = lesson.words
    if (words.isEmpty()) return CenteredMessage("단어가 없어요", "이 레슨에는 단어가 없어요.")

    var index by rememberSaveable { mutableIntStateOf(0) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var showMeaning by rememberSaveable(index) { mutableStateOf(false) }
    val word = words[index]

    DisposableEffect(Unit) { onDispose { speaker.stop() } }

    LaunchedEffect(index, finished) {
        if (finished) return@LaunchedEffect
        speaker.speak(word.text)
        delay(MEANING_DELAY_MS)
        showMeaning = true
    }
    LaunchedEffect(index, paused, finished) {
        if (paused || finished) return@LaunchedEffect
        delay(MEANING_DELAY_MS + NEXT_WORD_DELAY_MS)
        if (index < words.lastIndex) index++ else finished = true
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "👀 레벨 1 듣기 · ${lessonLabel(lesson.lessonName)}", onBack = onBack)
        TtsStatusBanner(speaker)

        if (finished) {
            CenteredMessage(title = "🎉 다 봤어요!", body = "${words.size}개 단어를 모두 들었어요.") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BigButton("🔁 다시 보기", onClick = { index = 0; finished = false; paused = false })
                    BigButton("⌨️ 쓰기 해 보기", onClick = onType, container = MaterialTheme.colorScheme.secondary)
                }
            }
            return@Column
        }

        Progress(current = index + 1, total = words.size)

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                FittedWord(word.text)
                val meaningAlpha by animateFloatAsState(if (showMeaning) 1f else 0f, label = "meaning")
                Text(
                    word.meaning,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.height(72.dp).alpha(meaningAlpha),
                )
            }
        }

        // Wraps to two rows on narrow screens; on the Fold cover screen "다음 ▶" was pushed off screen.
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BigButton("◀ 이전", enabled = index > 0, onClick = { index-- },
                container = MaterialTheme.colorScheme.surfaceVariant, content = MaterialTheme.colorScheme.onSurface)
            BigButton("🔊 다시 듣기", onClick = { speaker.speak(word.text) },
                container = MaterialTheme.colorScheme.secondary)
            BigButton(if (paused) "▶ 계속" else "⏸ 멈춤", onClick = { paused = !paused },
                container = MaterialTheme.colorScheme.surfaceVariant, content = MaterialTheme.colorScheme.onSurface)
            BigButton("다음 ▶", onClick = { if (index < words.lastIndex) index++ else finished = true })
        }
    }
}

@Composable
fun Progress(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LinearProgressIndicator(
            progress = { current.toFloat() / total },
            modifier = Modifier.weight(1f).widthIn(max = 800.dp).height(12.dp).clip(CircleShape),
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Text("$current / $total", style = MaterialTheme.typography.titleLarge)
    }
}

/** The word on one line, shrunk to fit narrow screens ("zookeeper" wrapped on the Fold cover screen). */
@Composable
private fun FittedWord(text: String) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        val base = MaterialTheme.typography.displayLarge
        // Bold Latin glyphs average about 0.62 em wide at this size, plus letter spacing.
        val fitted = (maxWidth.value / (text.length * 0.62f)).sp
        val size = if (fitted < base.fontSize) fitted else base.fontSize
        Text(text, style = base.copy(fontSize = size), maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
    }
}
