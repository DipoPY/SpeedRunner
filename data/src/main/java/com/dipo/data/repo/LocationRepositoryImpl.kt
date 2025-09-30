package com.dipo.data.repo

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import android.util.Log
import com.google.android.gms.tasks.CancellationTokenSource
import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.repo.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationRepositoryImpl(
    private val appContext: Context,
    private val fused: FusedLocationProviderClient,
) : LocationRepository {

    @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    override fun locationFlow(): Flow<LocationPoint> = callbackFlow {
        val hasFine = hasPermission(ACCESS_FINE_LOCATION)
        val hasCoarse = hasPermission(ACCESS_COARSE_LOCATION)

        if (!hasFine && !hasCoarse) {
            close(SecurityException("No location permission"))
            return@callbackFlow
        }

        val priority = when {
            hasFine   -> Priority.PRIORITY_HIGH_ACCURACY
            hasCoarse -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            else      -> Priority.PRIORITY_LOW_POWER
        }

        val updateIntervalMs      = if (hasFine) 5_000L else 10_000L
        val minUpdateIntervalMs   = if (hasFine) 2_500L else 7_500L
        val minUpdateDistanceM    = if (hasFine) 0f     else 5f

        val request = LocationRequest.Builder(updateIntervalMs)
            .setMinUpdateIntervalMillis(minUpdateIntervalMs)
            .setMinUpdateDistanceMeters(minUpdateDistanceM)
            .setPriority(priority)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(hasFine) // пусть дождётся точной, если есть FINE
            .build()

        // --- мягкий фильтр точности: первую точку принимаем всегда ---
        var emittedOnce = false
        fun emitLocation(loc: Location) {
            val acc = if (loc.hasAccuracy()) loc.accuracy else DEFAULT_ACCURACY
            val maxAcc = if (hasFine) MAX_ACCURACY_FINE else MAX_ACCURACY_COARSE

            if (emittedOnce && acc > maxAcc) {
                Log.d(TAG, "discard lat=${loc.latitude} lon=${loc.longitude} acc=$acc > $maxAcc")
                return
            }

            trySend(
                LocationPoint(
                    sessionId = "",
                    time = System.currentTimeMillis(),
                    lat = loc.latitude,
                    lon = loc.longitude,
                    accuracy = acc
                )
            )
            emittedOnce = true
            Log.d(TAG, "emit lat=${loc.latitude} lon=${loc.longitude} acc=$acc prio=$priority")
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (result.locations.isNullOrEmpty()) {
                    Log.d(TAG, "onLocationResult: empty")
                    return
                }
                result.locations.forEach(::emitLocation)
            }
        }

        val initialFixToken = CancellationTokenSource()
        try {
            // быстрые «стартовые» источники — не обязательны, но полезны
            fused.lastLocation.addOnSuccessListener { it?.let(::emitLocation) }
            fused.getCurrentLocation(priority, initialFixToken.token)
                .addOnSuccessListener { it?.let(::emitLocation) }
                .addOnFailureListener { Log.w(TAG, "getCurrentLocation failure", it) }

            fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
            Log.d(TAG, "requestLocationUpdates priority=$priority interval=$updateIntervalMs minInterval=$minUpdateIntervalMs minDist=$minUpdateDistanceM")
        } catch (se: SecurityException) {
            close(se); return@callbackFlow
        }

        awaitClose {
            fused.removeLocationUpdates(callback)
            initialFixToken.cancel()
            Log.d(TAG, "locationFlow closed")
        }
    }

    private companion object {
        // пороги подними: coarse на эмуляторе часто 2000–3000 м
        const val MAX_ACCURACY_FINE = 200f
        const val MAX_ACCURACY_COARSE = 3_000f
        const val DEFAULT_ACCURACY = 1_000f
        const val TAG = "LocationRepo"
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(appContext, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
