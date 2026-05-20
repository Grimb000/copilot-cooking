package com.example.podkast

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File

data class LastFmResponse(@SerializedName("toptracks") val topTracks: TopTracks)
data class TopTracks(@SerializedName("track") val trackList: List<TrackDto>)

data class TrackDto(
    val name: String,
    val listeners: String,
    val playcount: String,
    val artist: ArtistDto,
    val mbid: String?
)

data class ArtistDto(val name: String)

data class SimpleTrack(
    val id: String,
    val artist: String,
    val name: String,
    val playCount: String,
    val listeners: String
)


interface LastFmApi {
    @GET("2.0/")
    suspend fun getTopTracks(
        @Query("method") method: String = "artist.gettoptracks",
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 10
    ): LastFmResponse
}

const val API_KEY = "6f27ccf0697e047c210b1ccc0e06179a"
const val BASE_URL = "https://ws.audioscrobbler.com/"

class MainActivity : ComponentActivity() {
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "Нет интернета. Доступны только сохраненные записи.", Toast.LENGTH_LONG).show()
        }

        setContent {
            MusicApp()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@Composable
fun MusicApp() {
    var currentScreen by remember { mutableStateOf("search") }
    val context = LocalContext.current

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LastFmApi::class.java)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "search",
                    onClick = { currentScreen = "search" },
                    label = { Text("Поиск") },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = currentScreen == "saved",
                    onClick = { currentScreen = "saved" },
                    label = { Text("Сохраненные") },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (currentScreen == "search") {
                SearchScreen(retrofit, context)
            } else {
                SavedScreen(context)
            }
        }
    }
}

@Composable
fun SearchScreen(api: LastFmApi, context: Context) {
    var query by remember { mutableStateOf("") }
    var tracks by remember { mutableStateOf<List<SimpleTrack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Введите артиста (например, Cher)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val response = api.getTopTracks(artist = query, apiKey = API_KEY)
                        tracks = response.topTracks.trackList.map { dto ->
                            SimpleTrack(
                                id = dto.mbid ?: "No ID",
                                artist = dto.artist.name,
                                name = dto.name,
                                playCount = dto.playcount,
                                listeners = dto.listeners
                            )
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Найти топ-10 треков")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(tracks) { track ->
                TrackItem(track, showSaveButton = true) {
                    saveTrackToFile(context, track)
                }
            }
        }
    }
}

@Composable
fun SavedScreen(context: Context) {
    var savedTracks by remember { mutableStateOf<List<SimpleTrack>>(emptyList()) }

    LaunchedEffect(Unit) {
        savedTracks = readTracksFromFile(context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Сохраненные в файл tracks.txt:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        if (savedTracks.isEmpty()) {
            Text("Файл пуст. Сохраните что-нибудь в поиске.")
        }

        LazyColumn {
            items(savedTracks) { track ->
                TrackItem(track, showSaveButton = false) {}
            }
        }
    }
}

@Composable
fun TrackItem(track: SimpleTrack, showSaveButton: Boolean, onSave: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = track.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${track.artist}", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Plays: ${track.playCount} | Listeners: ${track.listeners}", fontSize = 12.sp)
            }
            if (showSaveButton) {
                Button(onClick = onSave) {
                    Text("Save")
                }
            }
        }
    }
}

fun saveTrackToFile(context: Context, track: SimpleTrack) {
    val fileContent = "${track.id}|${track.artist}|${track.name}|${track.playCount}|${track.listeners}\n"

    try {
        context.openFileOutput("tracks.txt", Context.MODE_APPEND).use {
            it.write(fileContent.toByteArray())
        }
        Toast.makeText(context, "Трек сохранен в файл!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
    }
}

fun readTracksFromFile(context: Context): List<SimpleTrack> {
    val file = File(context.filesDir, "tracks.txt")
    if (!file.exists()) return emptyList()

    val tracks = mutableListOf<SimpleTrack>()
    try {
        file.forEachLine { line ->
            val parts = line.split("|")
            if (parts.size == 5) {
                tracks.add(SimpleTrack(parts[0], parts[1], parts[2], parts[3], parts[4]))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return tracks
}