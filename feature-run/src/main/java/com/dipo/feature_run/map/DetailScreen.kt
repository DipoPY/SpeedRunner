package com.dipo.feature_run.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dipo.background.service.TrackingService
import com.dipo.background.service.TrackingSessionState
import com.dipo.feature_run.util.toDisplayDuration
import com.dipo.feature_run.util.toKilometersString
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

@Composable
fun DetailScreen() {
    val context = LocalContext.current

    // запрашиваем разрешения и стартуем сервис трекинга
    val locationSettingsClient = remember { LocationServices.getSettingsClient(context) }

    val sessionSnapshot by TrackingSessionState.state.collectAsState()
    val isActive = sessionSnapshot.active
    val distanceText = sessionSnapshot.distanceMeters.toKilometersString()
    val durationText = sessionSnapshot.durationMillis.toDisplayDuration()

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            TrackingService.start(context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notifOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            result[Manifest.permission.POST_NOTIFICATIONS] == true else true
        if ((fine || coarse) && notifOk) {
            ensureLocationEnabled(context, locationSettingsClient, settingsLauncher)
        }
    }

    fun requestPermissionsAndStart() {
        val perms = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
        val allGranted = perms.all { hasPermission(context, it) }
        if (allGranted) {
            ensureLocationEnabled(context, locationSettingsClient, settingsLauncher)
        } else {
            permissionLauncher.launch(perms)
        }
    }


    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { requestPermissionsAndStart() }, enabled = !isActive) { Text("Старт") }
            Button(onClick = { TrackingService.stop(context) }, enabled = isActive) { Text("Стоп") }
        }
        if (isActive) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "Активная сессия:\nДистанция — $distanceText км\nВремя — $durationText"
            )
        }
    }
}

private fun ensureLocationEnabled(
    context: Context,
    settingsClient: com.google.android.gms.location.SettingsClient,
    settingsLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
) {
    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
        .setMinUpdateIntervalMillis(2_500L)
        .build()
    val settingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(request)
        .setAlwaysShow(true)
        .build()
    settingsClient.checkLocationSettings(settingsRequest)
        .addOnSuccessListener { TrackingService.start(context) }
        .addOnFailureListener { ex ->
            if (ex is ResolvableApiException) {
                val intentSender = IntentSenderRequest.Builder(ex.resolution).build()
                settingsLauncher.launch(intentSender)
            }
        }
}

private fun hasPermission(context: Context, permission: String): Boolean =
    androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        permission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
