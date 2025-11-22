package com.sakethh.otic

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.otic.ui.theme.OticTheme

class OticActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val oticServiceIntent = Intent(this@OticActivity, OticService::class.java)
        enableEdgeToEdge()
        setContent {
            val oticVM = viewModel<OticVM>(factory = viewModelFactory {
                initializer {
                    OticVM(this@OticActivity)
                }
            })
            val runtimePermissionsLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap: Map<String, Boolean> ->
                    oticVM.updatePermissionsGrantedState(permissionsMap.all { it.value })
                }
            OticTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!oticVM.allPermissionsGranted) {
                            Text(
                                text = OticService.PERMISSION_REQUIRED_MESSAGE,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 15.dp, end = 15.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Button(onClick = {
                                runtimePermissionsLauncher.launch(OticService.permissions.toTypedArray())
                            }) {
                                Text(text = "Grant")
                            }
                            return@Column
                        }
                        Button(onClick = {
                            if (OticService.isServiceRunning) {
                                stopService(oticServiceIntent)
                                return@Button
                            }
                            if (Build.VERSION.SDK_INT <= 25) {
                                startService(oticServiceIntent)
                            } else {
                                startForegroundService(oticServiceIntent)
                            }
                        }) {
                            Text(text = if (!OticService.isServiceRunning) "Start Streaming" else "Stop Streaming")
                        }
                        if (OticService.isServiceRunning) {
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text = "Streaming on ${OticService.ipv4Address ?: "null"}:${OticService.serverPort}")
                        }
                    }
                }
            }
        }
    }
}

fun logger(string: String) {
    Log.d("OticService", string)
}