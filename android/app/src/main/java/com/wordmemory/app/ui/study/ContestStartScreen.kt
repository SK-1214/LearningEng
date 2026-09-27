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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.wordmemory.app.data.WordRepository
import com.wordmemory.app.ui.isCompactWidth
import com.wordmemory.app.ui.TopBar
import com.wordmemory.app.ui.appViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ContestStartViewModel(repository: WordRepository) : ViewModel() {
    val wordCount: StateFlow<Int> = repository.observeDistinctWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

/** Picks how many words to ask in contest practice (M5); 0 means every word. */
@Composable
fun ContestStartScreen(onBack: () -> Unit, onStart: (questionCount: Int) -> Unit) {
    val side = if (isCompactWidth()) 16.dp else 32.dp
    val vm = appViewModel { ContestStartViewModel(it.repository) }
    val total by vm.wordCount.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "🏆 대회 연습", onBack = onBack)
        Text(
            "모든 레슨의 단어가 섞여서 나와요. 뜻 없이 발음만 듣고 써요.\n모르겠으면 💡 뜻 보기를 눌러요.",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = side, vertical = 8.dp),
        )
        Text(
            "몇 문제 풀까요?",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = side, vertical = 16.dp),
        )
        val options = listOf(10, 20).filter { it < total }.map { n -> Triple("$n 문제", "약 ${n / 2}분", n) } +
            Triple("전체 $total 문제", "대회 전 마지막 연습", 0)
        if (isCompactWidth()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                options.forEach { (title, sub, n) ->
                    CountCard(title, sub, Modifier.fillMaxWidth().height(136.dp)) { onStart(n) }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(32.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                options.forEach { (title, sub, n) ->
                    CountCard(title, sub, Modifier.weight(1f).height(220.dp)) { onStart(n) }
                }
            }
        }
    }
}

@Composable
private fun CountCard(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
