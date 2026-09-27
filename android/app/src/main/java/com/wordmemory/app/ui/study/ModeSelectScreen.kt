package com.wordmemory.app.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.wordmemory.app.data.LessonWords
import com.wordmemory.app.data.WordRepository
import com.wordmemory.app.ui.isCompactWidth
import com.wordmemory.app.ui.Loading
import com.wordmemory.app.ui.TopBar
import com.wordmemory.app.ui.appViewModel
import com.wordmemory.app.ui.lessonLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads one lesson's words; shared by the mode picker and listen mode. */
class LessonWordsViewModel(lessonId: Long, repository: WordRepository) : ViewModel() {
    private val _data = MutableStateFlow<LessonWords?>(null)
    val data: StateFlow<LessonWords?> = _data.asStateFlow()

    init {
        viewModelScope.launch { _data.value = repository.lessonWords(lessonId) }
    }
}

@Composable
fun ModeSelectScreen(
    lessonId: Long,
    onBack: () -> Unit,
    onListen: () -> Unit,
    onCard: () -> Unit,
    onType: () -> Unit,
) {
    val vm = appViewModel { LessonWordsViewModel(lessonId, it.repository) }
    val data by vm.data.collectAsStateWithLifecycle()
    val lesson = data ?: return Loading()

    val compact = isCompactWidth()
    val modes = listOf(
        ModeSpec("👀", "레벨 1", "듣기", "보고 듣고 뜻 알기", MaterialTheme.colorScheme.secondaryContainer, onListen),
        ModeSpec("🧩", "레벨 2", "카드", "글자 카드 맞추기", MaterialTheme.colorScheme.tertiaryContainer, onCard),
        ModeSpec("⌨️", "레벨 3", "쓰기", "듣고 철자 쓰기", MaterialTheme.colorScheme.primaryContainer, onType),
    )

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "${lessonLabel(lesson.lessonName)} · ${lesson.words.size}단어", onBack = onBack)
        Text(
            "어떤 레벨로 공부할까요?",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = if (compact) 16.dp else 32.dp, vertical = 8.dp),
        )
        if (compact) {
            // Narrow screens (Fold cover): three side-by-side cards clipped their text, so stack them.
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                modes.forEach { ModeCard(it, horizontal = true, modifier = Modifier.fillMaxWidth().height(128.dp)) }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                modes.forEach { ModeCard(it, horizontal = false, modifier = Modifier.weight(1f).height(300.dp)) }
            }
        }
    }
}

private data class ModeSpec(
    val emoji: String,
    val level: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val onClick: () -> Unit,
)

@Composable
private fun ModeCard(mode: ModeSpec, horizontal: Boolean, modifier: Modifier) {
    Card(
        onClick = mode.onClick,
        colors = CardDefaults.cardColors(containerColor = mode.color),
        modifier = modifier,
    ) {
        if (horizontal) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(mode.emoji, fontSize = 56.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${mode.level} · ${mode.title}", style = MaterialTheme.typography.headlineMedium)
                    Text(mode.subtitle, style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(mode.emoji, fontSize = 72.sp)
                Text(mode.level, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(mode.title, style = MaterialTheme.typography.headlineLarge)
                Text(
                    mode.subtitle,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
