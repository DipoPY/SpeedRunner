package com.dipo.background.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**\
 * Хранилище состояния активной сессии, чтобы UI мог наблюдать за прогрессом пробежки.\
 */
object TrackingSessionState {

    private val _state = MutableStateFlow(SessionSnapshot())
    val state: StateFlow<SessionSnapshot> = _state.asStateFlow()

    fun activate(sessionId: String, distanceMeters: Double = 0.0, durationMillis: Long = 0L) {
        _state.value = SessionSnapshot(sessionId, distanceMeters, durationMillis, active = true)
    }

    fun update(sessionId: String, distanceMeters: Double, durationMillis: Long) {
        val current = _state.value
        if (!current.active || current.sessionId != sessionId) {
            _state.value = SessionSnapshot(sessionId, distanceMeters, durationMillis, active = true)
        } else {
            _state.value = current.copy(distanceMeters = distanceMeters, durationMillis = durationMillis)
        }
    }

    fun clear() {
        _state.value = SessionSnapshot()
    }
}

data class SessionSnapshot(
    val sessionId: String? = null,
    val distanceMeters: Double = 0.0,
    val durationMillis: Long = 0L,
    val active: Boolean = false,
)
