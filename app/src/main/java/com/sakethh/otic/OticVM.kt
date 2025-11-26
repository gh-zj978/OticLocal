package com.sakethh.otic

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OticVM(context: Context) : ViewModel() {
    var allPermissionsGranted by mutableStateOf(false)
        private set

    fun updatePermissionsGrantedState(granted: Boolean) {
        allPermissionsGranted = granted
    }

    private val PORT_NUM = "PORT_NUM"
    private val preferencesFileName = "preferences.json"

    init {
        allPermissionsGranted = OticService.permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        context.filesDir.resolve(preferencesFileName).apply {
            if (!exists()) return@apply

            OticService.updateServerPort(readText().run {
                this.substringAfter("$PORT_NUM=").toInt()
            })
        }
    }

    context(context: Context) fun updatePersistedPortNumber(
        value: Int, onCompletion: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            onError(e.message ?: e.stackTraceToString())
        }) {
            require(value in 0..65535) { VALID_PORT_MSG }

            context.filesDir.resolve(preferencesFileName).writeText("$PORT_NUM=$value")
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    companion object {
        const val VALID_PORT_MSG = "A valid port value is between 0 and 65535"
    }

}