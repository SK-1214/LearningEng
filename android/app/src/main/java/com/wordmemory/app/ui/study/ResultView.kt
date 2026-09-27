package com.wordmemory.app.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordmemory.app.data.db.StudyWord
import com.wordmemory.app.domain.SpellingGrader
import com.wordmemory.app.ui.BigButton
import com.wordmemory.app.ui.PopIn
import com.wordmemory.app.ui.isCompactWidth
import com.wordmemory.app.ui.theme.Star
import com.wordmemory.app.ui.theme.Wrong

/** One graded word in a level 2 or level 3 round. [answer] is what the child made, for the wrong-word list. */
data class StudyResult(val word: StudyWord, val correct: Boolean, val answer: String)

/** End-of-round screen shared by level 2 (card) and level 3 (type). Stacked and scrollable on narrow screens. */
@Composable
fun ResultView(results: List<StudyResult>, onRetryWrong: () -> Unit, onRestart: () -> Unit, onBack: () -> Unit) {
    val wrong = results.filterNot { it.correct }
    if (isCompactWidth()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Summary(results, wrong, onRetryWrong, onRestart, onBack, Modifier.fillMaxWidth())
            if (wrong.isNotEmpty()) WrongList(wrong, compact = true)
        }
    } else {
        Row(Modifier.fillMaxSize().padding(32.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Summary(results, wrong, onRetryWrong, onRestart, onBack, Modifier.weight(1f).fillMaxSize())
            if (wrong.isNotEmpty()) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { WrongList(wrong, compact = false) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Summary(
    results: List<StudyResult>,
    wrong: List<StudyResult>,
    onRetryWrong: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val total = results.size
    val correct = total - wrong.size
    val stars = SpellingGrader.stars(correct, total)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        PopIn {
            Text(
                buildAnnotatedString {
                    repeat(3) { i -> withStyle(SpanStyle(color = if (i < stars) Star else Color.LightGray)) { append("★") } }
                },
                fontSize = 96.sp,
            )
        }
        val scoreStyle = if (isCompactWidth()) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displaySmall
        Text("$total 개 중 $correct 개 맞았어요!", style = scoreStyle, textAlign = TextAlign.Center)
        if (wrong.isNotEmpty()) {
            BigButton("🔁 틀린 단어 다시 하기 (${wrong.size})", onClick = onRetryWrong)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BigButton("처음부터 다시", onClick = onRestart, container = MaterialTheme.colorScheme.secondary)
            BigButton("레슨 고르기", onClick = onBack,
                container = MaterialTheme.colorScheme.surfaceVariant, content = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Wrong words with what the child made. On narrow screens the answer goes under the word. */
@Composable
private fun WrongList(wrong: List<StudyResult>, compact: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("틀린 단어", style = MaterialTheme.typography.headlineMedium, color = Wrong)
        wrong.forEach { r ->
            if (compact) {
                Column {
                    Text("${r.word.text}  ·  ${r.word.meaning}", style = MaterialTheme.typography.headlineMedium)
                    Text("✗ ${r.answer}", style = MaterialTheme.typography.titleLarge, color = Wrong)
                }
            } else {
                Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(r.word.text, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                    Text(r.word.meaning, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("✗ ${r.answer}", style = MaterialTheme.typography.titleLarge, color = Wrong,
                        modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
