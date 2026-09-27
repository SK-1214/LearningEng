package com.wordmemory.app.ui.admin

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.wordmemory.app.data.WordRepository
import com.wordmemory.app.data.db.LessonSummary
import com.wordmemory.app.data.xlsx.XlsxFormatException
import com.wordmemory.app.data.xlsx.XlsxReader
import com.wordmemory.app.domain.WordList
import com.wordmemory.app.domain.WordListException
import com.wordmemory.app.domain.WordListParser
import com.wordmemory.app.ui.isCompactWidth
import com.wordmemory.app.ui.BigButton
import com.wordmemory.app.ui.TopBar
import com.wordmemory.app.ui.TtsStatusBanner
import com.wordmemory.app.ui.appContainer
import com.wordmemory.app.ui.appViewModel
import com.wordmemory.app.ui.lessonLabel
import com.wordmemory.app.ui.theme.Correct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ImportState {
    data object Idle : ImportState
    data object Working : ImportState
    data class Preview(val fileName: String, val list: WordList) : ImportState
    data class Error(val message: String) : ImportState
    data class Done(val wordCount: Int, val lessonCount: Int) : ImportState
}

class AdminViewModel(private val repository: WordRepository, private val resolver: ContentResolver) : ViewModel() {
    private val _import = MutableStateFlow<ImportState>(ImportState.Idle)
    val import: StateFlow<ImportState> = _import.asStateFlow()

    val lessons: StateFlow<List<LessonSummary>> = repository.observeLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(uri: Uri) {
        _import.value = ImportState.Working
        viewModelScope.launch {
            _import.value = try {
                val list = withContext(Dispatchers.IO) {
                    val rows = resolver.openInputStream(uri)?.use { XlsxReader.readFirstSheet(it) }
                        ?: throw XlsxFormatException("파일을 열 수 없어요")
                    WordListParser.parse(rows)
                }
                ImportState.Preview(displayName(uri), list)
            } catch (e: XlsxFormatException) {
                ImportState.Error(e.message.orEmpty())
            } catch (e: WordListException) {
                ImportState.Error(e.message.orEmpty())
            } catch (e: Exception) {
                ImportState.Error("파일을 읽지 못했어요: ${e.message}")
            }
        }
    }

    fun confirmReplace() {
        val preview = _import.value as? ImportState.Preview ?: return
        _import.value = ImportState.Working
        viewModelScope.launch {
            repository.replaceAll(preview.list)
            _import.value = ImportState.Done(preview.list.entries.size, preview.list.lessons.size)
        }
    }

    fun cancel() {
        _import.value = ImportState.Idle
    }

    private fun displayName(uri: Uri): String =
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        } ?: uri.lastPathSegment.orEmpty()
}

/** Parent screen (wiki/product/screens.md #1): replace the word list from an Excel file. */
@Composable
fun AdminScreen(onBack: () -> Unit) {
    val container = appContainer()
    val vm = appViewModel { AdminViewModel(it.repository, it.contentResolver) }
    val importState by vm.import.collectAsStateWithLifecycle()
    val lessons by vm.lessons.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // "*/*": some file managers do not tag .xlsx with its MIME type; the reader validates the file anyway.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.load(uri)
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "⚙️ 부모님 설정", onBack = onBack)
        TtsStatusBanner(container.speaker)
        val importSection: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("단어 목록 가져오기", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "엑셀(.xlsx) 첫 줄에 Lesson · word · meaning 제목이 있어야 해요.\n" +
                        "가져오면 레슨 목록이 새 파일로 바뀌어요. 학습 기록은 그대로 남아요.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                ImportPanel(importState, onPick = { picker.launch(arrayOf("*/*")) }, onConfirm = vm::confirmReplace,
                    onCancel = vm::cancel)
                HorizontalDivider()
                TextButton(onClick = {
                    try {
                        context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                    } catch (_: ActivityNotFoundException) {
                    }
                }) { Text("🔊 음성 읽기(TTS) 설정 열기", style = MaterialTheme.typography.titleLarge) }
            }
        }
        val lessonsSection: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier) {
                Text("지금 들어 있는 단어", style = MaterialTheme.typography.headlineMedium)
                if (lessons.isEmpty()) {
                    Text("아직 없어요", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
                }
                LazyColumn(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(lessons, key = { it.id }) { lesson ->
                        Text(
                            "${lessonLabel(lesson.name)} — ${lesson.wordCount}단어 · 레벨 3(쓰기)에서 맞힌 단어 ${lesson.masteredCount}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        // Narrow screens (Fold cover) stack the two halves instead of squeezing them side by side.
        if (isCompactWidth()) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                importSection(Modifier.fillMaxWidth())
                lessonsSection(Modifier.weight(1f))
            }
        } else {
            Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                importSection(Modifier.weight(1f))
                lessonsSection(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ImportPanel(state: ImportState, onPick: () -> Unit, onConfirm: () -> Unit, onCancel: () -> Unit) {
    when (state) {
        ImportState.Idle -> BigButton("📂 엑셀 파일 선택", onClick = onPick)
        ImportState.Working -> CircularProgressIndicator()
        is ImportState.Error -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️ ${state.message}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
            BigButton("📂 다른 파일 선택", onClick = onPick)
        }
        is ImportState.Done -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("✅ 레슨 ${state.lessonCount}개, 단어 ${state.wordCount}개를 가져왔어요",
                style = MaterialTheme.typography.titleLarge, color = Correct)
            BigButton("📂 다른 파일 선택", onClick = onPick)
        }
        is ImportState.Preview -> PreviewCard(state, onConfirm, onCancel)
    }
}

@Composable
private fun PreviewCard(state: ImportState.Preview, onConfirm: () -> Unit, onCancel: () -> Unit) {
    val list = state.list
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("📄 ${state.fileName}", style = MaterialTheme.typography.titleLarge)
            Text(
                "레슨 ${list.lessons.size}개 · ${list.entries.size}줄 · 서로 다른 단어 ${list.uniqueWordCount}개",
                style = MaterialTheme.typography.bodyLarge,
            )
            if (list.skippedRows.isNotEmpty()) {
                Text(
                    "⚠️ 단어나 뜻이 비어 있어 건너뛴 줄: ${list.skippedRows.joinToString()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                list.lessons.forEach { lesson ->
                    item(key = "h-$lesson") {
                        Text(lessonLabel(lesson), style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                    items(list.entries.filter { it.lesson == lesson }, key = { "$lesson-${it.position}" }) { e ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${e.position}. ${e.word}", style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f))
                            Text(e.meaning, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigButton("이 목록으로 바꾸기", onClick = onConfirm)
                BigButton("취소", onClick = onCancel,
                    container = MaterialTheme.colorScheme.surfaceVariant, content = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
