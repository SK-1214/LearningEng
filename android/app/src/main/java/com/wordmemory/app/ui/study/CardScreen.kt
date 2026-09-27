package com.wordmemory.app.ui.study

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordmemory.app.tts.SoundEffects
import com.wordmemory.app.ui.isCompactWidth
import com.wordmemory.app.ui.PopIn
import com.wordmemory.app.ui.rememberShake
import com.wordmemory.app.ui.shake
import com.wordmemory.app.ui.shakeOffset
import com.wordmemory.app.domain.CardPuzzle
import com.wordmemory.app.domain.LetterCard
import com.wordmemory.app.ui.BigButton
import com.wordmemory.app.ui.CenteredMessage
import com.wordmemory.app.ui.Loading
import com.wordmemory.app.ui.TopBar
import com.wordmemory.app.ui.TtsStatusBanner
import com.wordmemory.app.ui.appContainer
import com.wordmemory.app.ui.appViewModel
import com.wordmemory.app.ui.lessonLabel
import com.wordmemory.app.ui.theme.Correct
import com.wordmemory.app.ui.theme.Wrong
import kotlinx.coroutines.delay

private const val CORRECT_NEXT_DELAY_MS = 1_200L
private const val RETRY_DELAY_MS = 1_500L

/** Rough length of [shake], subtracted so the retry still comes [RETRY_DELAY_MS] after the wrong answer. */
private const val SHAKE_MS = 315L

private val MAX_SLOT_WIDTH = 84.dp

/** A space slot is this fraction of a letter slot. */
private const val SPACE_RATIO = 0.4f

@Composable
fun CardScreen(lessonId: Long, onBack: () -> Unit) {
    val container = appContainer()
    val speaker = container.speaker
    val vm = appViewModel { CardViewModel(lessonId, it.repository) }
    val state by vm.state.collectAsStateWithLifecycle()
    if (state.loading) return Loading()

    DisposableEffect(Unit) { onDispose { speaker.stop() } }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "🧩 레벨 2 카드 · ${lessonLabel(state.lessonName)}", onBack = onBack)
        TtsStatusBanner(speaker)
        when {
            state.queue.isEmpty() -> CenteredMessage("단어가 없어요", "이 레슨에는 단어가 없어요.")
            state.finished -> ResultView(state.results, onRetryWrong = vm::retryWrong, onRestart = vm::restart, onBack = onBack)
            else -> PuzzleView(state, container.sounds, speaker::speak, vm::onCard, vm::onSlot, vm::retry, vm::next)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PuzzleView(
    state: CardUiState,
    sounds: SoundEffects,
    speak: (String) -> Unit,
    onCard: (Int) -> Unit,
    onSlot: (Int) -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
) {
    val word = state.current ?: return
    val puzzle = state.puzzle ?: return
    val shake = rememberShake()
    val compact = isCompactWidth()

    LaunchedEffect(state.round, state.index) { speak(word.text) }
    LaunchedEffect(state.round, state.index, state.phase) {
        when (state.phase) {
            CardPhase.CORRECT -> { sounds.playCorrect(); delay(CORRECT_NEXT_DELAY_MS); onNext() }
            CardPhase.RETRY -> { sounds.playWrong(); shake.shake(); delay(RETRY_DELAY_MS - SHAKE_MS); onRetry() }
            CardPhase.REVEALED -> { sounds.playWrong(); shake.shake() }
            CardPhase.PLAYING -> {}
        }
    }

    Progress(current = state.index + 1, total = state.queue.size)

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = if (compact) 12.dp else 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            BigButton("🔊 다시 듣기", onClick = { speak(word.text) }, container = MaterialTheme.colorScheme.secondary)
            Text(word.meaning, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.secondary)
        }

        Box(Modifier.shakeOffset(shake)) { Slots(puzzle, state.phase, onSlot) }

        val message = when (state.phase) {
            CardPhase.PLAYING -> "카드를 순서대로 눌러요"
            CardPhase.CORRECT -> "정답! 🎉"
            CardPhase.RETRY -> "아쉬워요! 한 번 더 해 볼까요? 🙂"
            CardPhase.REVEALED -> "정답은  ${word.text}"
        }
        val messageColor = when (state.phase) {
            CardPhase.CORRECT -> Correct
            CardPhase.PLAYING -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> Wrong
        }
        if (state.phase == CardPhase.CORRECT) {
            PopIn { Text(message, style = MaterialTheme.typography.headlineLarge, color = messageColor) }
        } else {
            Text(message, style = MaterialTheme.typography.headlineLarge, color = messageColor)
        }

        if (state.phase == CardPhase.REVEALED) {
            BigButton("다음 ▶", onClick = onNext)
        } else {
            FlowRow(
                modifier = Modifier.widthIn(max = 1000.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val cardSize = if (compact) 76.dp else 100.dp
                puzzle.pool.forEach { card ->
                    PoolCard(card, size = cardSize, enabled = state.phase == CardPhase.PLAYING) { onCard(card.id) }
                }
            }
        }
    }
}

/** Answer slots. Spaces are narrow gaps; tapping a filled slot sends its card back. */
@Composable
private fun Slots(puzzle: CardPuzzle, phase: CardPhase, onSlot: (Int) -> Unit) {
    val filledColor = when (phase) {
        CardPhase.CORRECT -> Correct.copy(alpha = 0.18f)
        CardPhase.RETRY, CardPhase.REVEALED -> Wrong.copy(alpha = 0.15f)
        CardPhase.PLAYING -> MaterialTheme.colorScheme.surface
    }
    val border = when (phase) {
        CardPhase.CORRECT -> Correct
        CardPhase.RETRY, CardPhase.REVEALED -> Wrong
        CardPhase.PLAYING -> MaterialTheme.colorScheme.primary
    }
    // Slots shrink to fit the width: 10 letters did not fit on the Galaxy Z Fold cover screen (2026-09-27).
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val letters = puzzle.letterSlots.size
        val spaces = puzzle.answer.count { it == ' ' }
        val gap = 8.dp
        val slotWidth = ((maxWidth - gap * (letters + spaces - 1)) / (letters + spaces * SPACE_RATIO))
            .coerceAtMost(MAX_SLOT_WIDTH)
        Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
            puzzle.answer.forEachIndexed { slot, ch ->
                if (ch == ' ') {
                    Spacer(Modifier.width(slotWidth * SPACE_RATIO))
                    return@forEachIndexed
                }
                val letter = puzzle.letterAt(slot)
                val isHint = slot in puzzle.hintSlots
                Surface(
                    shape = RoundedCornerShape(slotWidth * 0.2f),
                    color = if (isHint) MaterialTheme.colorScheme.surfaceVariant else filledColor,
                    border = BorderStroke(3.dp, if (letter == null) MaterialTheme.colorScheme.outlineVariant else border),
                    modifier = Modifier
                        .size(width = slotWidth, height = slotWidth * 1.24f)
                        .clickable(enabled = letter != null && !isHint && phase == CardPhase.PLAYING) { onSlot(slot) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(letter?.toString() ?: "", fontSize = (slotWidth.value * 0.66f).sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PoolCard(card: LetterCard, size: Dp, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp,
        modifier = Modifier.size(size).clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                card.letter.toString(),
                fontSize = (size.value * 0.6f).sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
            )
        }
    }
}
