package com.wordmemory.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordmemory.app.tts.Speaker

/**
 * Narrow screens such as the Galaxy Z Fold cover screen (~411dp wide). The tablet (wiki/tech/target-device.md)
 * and the Fold inner screen (~750dp) keep the side-by-side layouts; narrow screens stack them.
 */
@Composable
fun isCompactWidth(): Boolean = LocalConfiguration.current.screenWidthDp < COMPACT_WIDTH_DP

private const val COMPACT_WIDTH_DP = 600

/** "4" -> "레슨 4"; a named lesson such as "Animals" is shown as is (wiki ADR 0005). */
fun lessonLabel(name: String): String = if (name.toIntOrNull() != null) "레슨 $name" else name

@Composable
fun TopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    val compact = isCompactWidth()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (compact) 4.dp else 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 64.dp)) {
            Text(if (compact) "←" else "←  뒤로", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            title,
            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = if (compact) 4.dp else 16.dp),
        )
        actions()
    }
}

@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

/** Tells the parent when English TTS is not usable, with a shortcut to install the voice data. */
@Composable
fun TtsStatusBanner(speaker: Speaker) {
    val status by speaker.status.collectAsStateWithLifecycle()
    val message = when (status) {
        Speaker.Status.MISSING_DATA -> "영어 음성이 설치되어 있지 않아 발음이 나오지 않아요."
        Speaker.Status.UNAVAILABLE -> "이 기기에서 음성 읽기(TTS)를 쓸 수 없어요."
        else -> return
    }
    val context = LocalContext.current
    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = {
                try {
                    context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                } catch (_: ActivityNotFoundException) {
                    context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                }
            }) { Text("설치하기") }
        }
    }
}

@Composable
fun CenteredMessage(title: String, body: String, action: @Composable () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        action()
    }
}
