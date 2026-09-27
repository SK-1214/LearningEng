package com.wordmemory.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.wordmemory.app.data.WRONG_WORDS_LESSON_ID
import com.wordmemory.app.data.WRONG_WORDS_LESSON_NAME
import com.wordmemory.app.data.WordRepository
import com.wordmemory.app.data.db.LessonSummary
import com.wordmemory.app.ui.isCompactWidth
import com.wordmemory.app.ui.CenteredMessage
import com.wordmemory.app.ui.Loading
import com.wordmemory.app.ui.appViewModel
import com.wordmemory.app.ui.lessonLabel
import com.wordmemory.app.ui.theme.Star
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repository: WordRepository) : ViewModel() {
    val lessons: StateFlow<List<LessonSummary>?> = repository.observeLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val wrongWordCount: StateFlow<Int> = repository.observeWrongWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(onLesson: (Long) -> Unit, onContest: () -> Unit, onAdmin: () -> Unit) {
    val vm = appViewModel { HomeViewModel(it.repository) }
    val lessons by vm.lessons.collectAsStateWithLifecycle()
    val wrongWordCount by vm.wrongWordCount.collectAsStateWithLifecycle()
    var showAdminHint by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🐝 단어 쏙쏙", style = MaterialTheme.typography.displaySmall, modifier = Modifier.weight(1f))
            if (showAdminHint) {
                Text(
                    "부모님 설정은 길게 누르세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Long press keeps the child from wandering into the parent screen (MVP default for Q-5).
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .combinedClickable(onClick = { showAdminHint = true }, onLongClick = onAdmin),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Settings, "부모님 설정", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val list = lessons
        when {
            list == null -> Loading()
            list.isEmpty() -> CenteredMessage(
                title = "아직 단어가 없어요",
                body = "부모님이 ⚙️ 설정을 길게 눌러\n엑셀 단어 목록을 넣어 주세요.",
            )
            else -> {
                Text(
                    "어떤 레슨을 공부할까요?",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = if (isCompactWidth()) 16.dp else 32.dp, vertical = 8.dp),
                )
                // Narrow screens (Fold cover): two lesson columns, with the special cards spanning the full width.
                val compact = isCompactWidth()
                val size = if (compact) {
                    CardSize(height = 140.dp, wideHeight = 110.dp, title = MaterialTheme.typography.headlineMedium)
                } else {
                    CardSize(height = 180.dp, wideHeight = 180.dp, title = MaterialTheme.typography.headlineLarge)
                }
                val gap = if (compact) 12.dp else 24.dp
                LazyVerticalGrid(
                    columns = if (compact) GridCells.Fixed(2) else GridCells.Adaptive(minSize = 220.dp),
                    contentPadding = PaddingValues(if (compact) 16.dp else 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    val wide: LazyGridItemSpanScope.() -> GridItemSpan = {
                        GridItemSpan(if (compact) maxLineSpan else 1)
                    }
                    item(key = "contest", span = wide) { ContestCard(size, onClick = onContest) }
                    if (wrongWordCount > 0) {
                        item(key = "wrong", span = wide) {
                            WrongWordsCard(wrongWordCount, size, onClick = { onLesson(WRONG_WORDS_LESSON_ID) })
                        }
                    }
                    items(list, key = { it.id }) { lesson -> LessonCard(lesson, size, onClick = { onLesson(lesson.id) }) }
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonSummary, size: CardSize, onClick: () -> Unit) {
    val progress = if (lesson.wordCount == 0) 0f else lesson.masteredCount.toFloat() / lesson.wordCount
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth().height(size.height),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(lessonLabel(lesson.name), style = size.title)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "⭐ ${lesson.masteredCount} / ${lesson.wordCount}",
                    style = MaterialTheme.typography.titleLarge,
                )
                LinearProgressIndicator(
                    progress = { progress },
                    color = Star,
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            }
        }
    }
}

/** Review card: words last answered wrong in level 3 across all lessons (wiki/learning/learning-levels.md). */
@Composable
private fun WrongWordsCard(count: Int, size: CardSize, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth().height(size.wideHeight),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("🔁 $WRONG_WORDS_LESSON_NAME", style = size.title)
            Text("다시 도전! ${count}개", style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** Entry to contest practice (M5): all lessons mixed, meaning hidden. */
@Composable
private fun ContestCard(size: CardSize, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth().height(size.wideHeight),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("🏆 대회 연습", style = size.title)
            Text("뜻 없이 듣고 쓰기", style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** Card sizes for the lesson grid; [wideHeight] is for the contest and wrong-word cards. */
private data class CardSize(val height: Dp, val wideHeight: Dp, val title: TextStyle)
