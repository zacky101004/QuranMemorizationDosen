package com.example.quranmemorizationdosen.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quranmemorizationdosen.TokenManager
import com.example.quranmemorizationdosen.data.api.RetrofitClient
import com.example.quranmemorizationdosen.data.api.DosenResponse
import com.example.quranmemorizationdosen.ui.navigation.BottomNavigationBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, onLogout: () -> Unit) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)
    val scope = rememberCoroutineScope()
    var dosenResponse by remember { mutableStateOf<DosenResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    val response = RetrofitClient.apiService.getDosenInfo("Bearer $token")
                    if (response.isSuccessful) {
                        dosenResponse = response.body()
                    } else {
                        errorMessage = "Gagal mengambil data: ${response.message()}"
                    }
                } else {
                    errorMessage = "Token tidak ditemukan"
                }
            } catch (e: Exception) {
                errorMessage = "Kesalahan: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Dosen") },
                actions = {
                    TextButton(onClick = {
                        tokenManager.clearTokens()
                        onLogout()
                    }) {
                        Text("Logout", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                dosenResponse != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Selamat datang, ${dosenResponse!!.data.nama}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "NIP: ${dosenResponse!!.data.nip}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Email: ${dosenResponse!!.data.email}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Daftar Mahasiswa Bimbingan",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(dosenResponse!!.data.info_mahasiswa_pa.daftar_mahasiswa) { mahasiswa ->
                            MahasiswaCard(mahasiswa) {
                                navController.navigate("lihat_setoran/${mahasiswa.nim}")
                            }
                        }
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun MahasiswaCard(mahasiswa: com.example.quranmemorizationdosen.data.api.Mahasiswa, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = mahasiswa.nama, style = MaterialTheme.typography.bodyLarge)
            Text(text = "NIM: ${mahasiswa.nim}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Angkatan: ${mahasiswa.angkatan}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Progres: ${mahasiswa.info_setoran.persentase_progres_setor}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}