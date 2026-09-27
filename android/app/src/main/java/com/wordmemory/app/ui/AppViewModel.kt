package com.wordmemory.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordmemory.app.AppContainer
import com.wordmemory.app.WordMemoryApp

@Composable
fun appContainer(): AppContainer = (LocalContext.current.applicationContext as WordMemoryApp).container

/** Creates a ViewModel scoped to the current navigation entry, built from the [AppContainer]. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (AppContainer) -> VM): VM {
    val container = appContainer()
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create(container) as T
        }
    )
}
