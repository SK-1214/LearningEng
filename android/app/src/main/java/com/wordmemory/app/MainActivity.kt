package com.wordmemory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wordmemory.app.data.CONTEST_LESSON_ID
import com.wordmemory.app.ui.admin.AdminScreen
import com.wordmemory.app.ui.home.HomeScreen
import com.wordmemory.app.ui.study.CardScreen
import com.wordmemory.app.ui.study.ContestStartScreen
import com.wordmemory.app.ui.study.ListenScreen
import com.wordmemory.app.ui.study.ModeSelectScreen
import com.wordmemory.app.ui.study.TypeScreen
import com.wordmemory.app.ui.theme.WordMemoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordMemoryTheme {
                // safeDrawing includes the keyboard, so typing mode shrinks above it instead of being covered.
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavHost(Modifier.safeDrawingPadding())
                }
            }
        }
    }
}

/** Flow: lesson -> level (listen/card/type) -> study (wiki ADR 0005). */
@Composable
private fun AppNavHost(modifier: Modifier) {
    val nav = rememberNavController()
    val lessonArg = listOf(navArgument("lessonId") { type = NavType.LongType })
    NavHost(navController = nav, startDestination = "home", modifier = modifier) {
        composable("home") {
            HomeScreen(
                onLesson = { nav.navigate("modes/$it") },
                onContest = { nav.navigate("contest") },
                onAdmin = { nav.navigate("admin") },
            )
        }
        composable("admin") { AdminScreen(onBack = { nav.popBackStack() }) }
        composable("contest") {
            ContestStartScreen(
                onBack = { nav.popBackStack() },
                onStart = { count -> nav.navigate("type/$CONTEST_LESSON_ID?count=$count") },
            )
        }
        composable("modes/{lessonId}", lessonArg) { entry ->
            val lessonId = entry.arguments!!.getLong("lessonId")
            ModeSelectScreen(
                lessonId = lessonId,
                onBack = { nav.popBackStack() },
                onListen = { nav.navigate("listen/$lessonId") },
                onCard = { nav.navigate("card/$lessonId") },
                onType = { nav.navigate("type/$lessonId") },
            )
        }
        composable("listen/{lessonId}", lessonArg) { entry ->
            val lessonId = entry.arguments!!.getLong("lessonId")
            ListenScreen(
                lessonId = lessonId,
                onBack = { nav.popBackStack() },
                onType = { nav.navigate("type/$lessonId") { popUpTo("modes/$lessonId") } },
            )
        }
        composable("card/{lessonId}", lessonArg) { entry ->
            CardScreen(lessonId = entry.arguments!!.getLong("lessonId"), onBack = { nav.popBackStack() })
        }
        composable(
            "type/{lessonId}?count={count}",
            lessonArg + navArgument("count") { type = NavType.IntType; defaultValue = 0 },
        ) { entry ->
            val args = entry.arguments!!
            TypeScreen(lessonId = args.getLong("lessonId"), onBack = { nav.popBackStack() }, questionCount = args.getInt("count"))
        }
    }
}
