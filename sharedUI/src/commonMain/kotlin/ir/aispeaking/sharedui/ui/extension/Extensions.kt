package ir.aispeaking.sharedui.ui.extension

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

fun <T> immutableListOf(vararg elements: T): ImmutableList<T> = elements.toList().toImmutableList()

fun <T> immutableSetOf(vararg elements: T): ImmutableSet<T> = elements.toList().toImmutableSet()
