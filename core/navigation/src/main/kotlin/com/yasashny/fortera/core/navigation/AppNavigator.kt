package com.yasashny.fortera.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class AppNavigator(initialDestination: Any) {
    val backStack: SnapshotStateList<Any> = mutableStateListOf(initialDestination)

    fun navigate(destination: Any) = backStack.add(destination)

    fun back() { backStack.removeLastOrNull() }

    fun clearAndNavigate(vararg destinations: Any) {
        backStack.clear()
        destinations.forEach { backStack.add(it) }
    }
}
