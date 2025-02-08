package com.almiga.fetchassignment.util

import kotlinx.datetime.Clock
import javax.inject.Inject

interface SystemTimeProvider {
    fun getCurrentTimeMillis(): Long
}

class ClockSystemTimeProvider @Inject constructor()
    : SystemTimeProvider {
    override fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}