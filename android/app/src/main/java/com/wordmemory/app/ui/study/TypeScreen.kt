package com.wordmemory.app.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordmemory.app.tts.SoundEffects
import com.wordmemory.app.ui.isCompactWidth
import com.wordmemory.app.ui.PopIn
import com.wordmemory.app.ui.rememberShake
import com.wordmemory.app.ui.shake
import com.wordmemory.app.ui.shakeOffset
import com.wordmemory.app.data.CONTEST_LESSON_ID
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

/**
 * Level 3 typing. With [CONTEST_LESSON_ID] it is contest practice (M5): words from all lessons,
 * [questionCount] per round, meaning hidden until asked for or graded.
 */
@Composable
fun TypeScreen(lessonId: Long, onBack: () -> Unit, questionCount: Int = 0) {
    val contest = lessonId == CONTEST_LESSON_ID
    val container = appContainer()
    val speaker = container.speaker
    val vm = appViewModel { TypeViewModel(lessonId, it.repository, questionCount) }
    val state by vm.state.collectAsStateWithLifecycle()
    if (state.loading) return Loading()

    DisposableEffect(Unit) { onDispose { speaker.stop() } }

    Column(Modifier.fillMaxSize()) {
        val title = if (contest) "🏆 대회 연습" else "⌨️ 레벨 3 쓰기 · ${lessonLabel(state.lessonName)}"
        TopBar(title = title, onBack = onBack)
        TtsStatusBanner(speaker)
        when {
            state.queue.isEmpty() -> CenteredMessage("단어가 없어요", "이 레슨에는 단어가 없어요.")
            state.finished -> ResultView(state.results, onRetryWrong = vm::retryWrong, onRestart = vm::restart, onBack = onBack)
            else -> TypingView(state, hideMeaning = contest, container.sounds, speaker::speak, vm::onInput, vm::submit, vm::next)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypingView(
    state: TypeUiState,
    hideMeaning: Boolean,
    sounds: SoundEffects,
    speak: (String) -> Unit,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
) {
    val word = state.current ?: return
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val shake = rememberShake()

    LaunchedEffect(state.round, state.index) {
        speak(word.text)
        focusRequester.requestFocus()
        keyboard?.show()
    }
    LaunchedEffect(state.graded) {
        when (state.graded) {
            true -> {
                sounds.playCorrect()
                delay(CORRECT_NEXT_DELAY_MS)
                onNext()
            }
            false -> {
                sounds.playWrong()
                shake.shake()
            }
            null -> {}
        }
    }

    Progress(current = state.index + 1, total = state.queue.size)

    val compact = isCompactWidth()

    // Scrollable because the keyboard covers about half of the landscape screen (wiki/tech/target-device.md).
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = if (compact) 16.dp else 32.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BigButton("🔊 다시 듣기", onClick = { speak(word.text) }, container = MaterialTheme.colorScheme.secondary)
            // Contest practice hides the meaning like a real spelling bee, but homophones (right/write) and
            // heteronyms (wind) need it, so the child can ask for it, as contestants may ask for a definition.
            var meaningShown by rememberSaveable(state.round, state.index) { mutableStateOf(false) }
            if (!hideMeaning || meaningShown || state.graded != null) {
                Text(word.meaning, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.CenterVertically))
            } else {
                BigButton("💡 뜻 보기", onClick = { meaningShown = true },
                    container = MaterialTheme.colorScheme.surfaceVariant, content = MaterialTheme.colorScheme.onSurface)
            }
            // First tablet test: "take off" was typed as "takeoff", so multi-word answers get a hint.
            val wordCount = word.text.trim().split(' ').size
            if (wordCount > 1) {
                Text("(${wordCount}단어 · 띄어쓰기)", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }

        val borderColor = when (state.graded) {
            true -> Correct
            false -> Wrong
            null -> MaterialTheme.colorScheme.primary
        }
        val answerStyle = if (compact) {
            MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center, letterSpacing = 2.sp)
        } else {
            MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center, letterSpacing = 4.sp)
        }
        val field: @Composable (Modifier) -> Unit = { modifier ->
            // A fresh field (and IME connection) per word, so keys pressed while the previous answer was
            // shown can never leak into the next answer.
            key(state.round, state.index) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInput,
                    singleLine = true,
                    readOnly = state.graded != null,
                    textStyle = answerStyle,
                    placeholder = {
                        Text("여기에 써요", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center)
                    },
                    // ADR 0004: the device keyboard must not suggest or correct words, or it gives the answer away.
                    // A password-type field turns off the suggestion strip on Samsung/Gboard and forces English;
                    // VisualTransformation.None keeps the letters visible. Verified on Galaxy Tab S7+ (2026-09-26)
                    // and Galaxy Z Fold7 (2026-09-27).
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    visualTransformation = VisualTransformation.None,
                    keyboardActions = KeyboardActions(onDone = { if (state.graded == null) onSubmit() else onNext() }),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = borderColor, unfocusedBorderColor = borderColor),
                    modifier = modifier.focusRequester(focusRequester),
                )
            }
        }
        val action: @Composable () -> Unit = {
            when (state.graded) {
                null -> BigButton("확인 ✔", onClick = onSubmit, enabled = state.input.isNotBlank())
                false -> BigButton("다음 ▶", onClick = onNext)
                true -> {}
            }
        }
        if (compact) {
            // Portrait phone: the full width goes to the answer, the button sits below it above the keyboard.
            Column(
                modifier = Modifier.fillMaxWidth().shakeOffset(shake),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                field(Modifier.fillMaxWidth())
                action()
            }
        } else {
            // The action button sits beside the field: below it, the keyboard covered it on the tablet.
            Row(
                modifier = Modifier.widthIn(max = 1000.dp).fillMaxWidth().shakeOffset(shake),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                field(Modifier.weight(1f))
                action()
            }
        }

        if (state.koreanKeyboard) {
            Text("⚠️ 키보드를 영어(A)로 바꿔 주세요", style = MaterialTheme.typography.titleLarge, color = Wrong)
        }

        when (state.graded) {
            null -> {}
            true -> PopIn { Text("정답! 🎉", style = MaterialTheme.typography.displaySmall, color = Correct) }
            false -> Text(
                buildAnnotatedString {
                    append("정답은  ")
                    highlightDifferences(word.text, state.input)
                },
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}

/** Shows the correct spelling, marking letters the child got wrong in red. */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightDifferences(expected: String, answer: String) {
    val typed = answer.trim().lowercase()
    expected.forEachIndexed { i, ch ->
        val ok = typed.getOrNull(i) == ch.lowercaseChar()
        withStyle(SpanStyle(color = if (ok) Correct else Wrong)) { append(ch) }
    }
}

