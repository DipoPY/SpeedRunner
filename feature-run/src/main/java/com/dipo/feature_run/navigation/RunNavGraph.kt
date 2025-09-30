package com.dipo.feature_run.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.compose.rememberAsyncImagePainter
import com.dipo.domain.entity.RunSession
import com.dipo.domain.usecase.GetRunHistory
import com.dipo.feature_run.map.DetailScreen
import com.dipo.feature_run.util.toDisplayDateTime
import com.dipo.feature_run.util.toDisplayDuration
import com.dipo.feature_run.util.toKilometersString
import org.koin.androidx.compose.get

object RunDestinations { const val MAP = "map"; const val HISTORY = "history" }

@Composable
fun RunNavGraph(nav: NavHostController, start: String = RunDestinations.MAP) {
    NavHost(navController = nav, startDestination = start) {
        composable(RunDestinations.MAP) {
            Column(modifier = Modifier.padding(top = 30.dp, start = 10.dp).fillMaxSize()) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = { nav.navigate(RunDestinations.HISTORY) }) { Text("История") }
                }
                DetailScreen()
            }
        }
        composable(RunDestinations.HISTORY) { HistoryScreen(onBack = { nav.popBackStack() }) }
    }
}

@Composable
private fun HistoryScreen(onBack: () -> Unit, getHistory: GetRunHistory = get()) {
    val state = remember { mutableStateOf<List<RunSession>>(emptyList()) }
    LaunchedEffect(Unit) {
        state.value = getHistory()
    }
    Column(modifier = Modifier.padding(top = 30.dp, start = 20.dp).fillMaxSize()) {
        Button(onClick = onBack) { Text("Назад") }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.value) { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Старт: ${session.startTime.toDisplayDateTime()}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        session.endTime?.let {
                            Text(text = "Финиш: ${it.toDisplayDateTime()}")
                        }
                        if (session.durationMillis > 0) {
                            Text(text = "Длительность: ${session.durationMillis.toDisplayDuration()}")
                        }
                        Text(text = "Дистанция: ${session.distanceMeters.toKilometersString()} км")
                    }
                }
            }
        }
    }
}
