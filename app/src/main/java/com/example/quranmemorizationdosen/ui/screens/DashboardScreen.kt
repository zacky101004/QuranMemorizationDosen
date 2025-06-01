package com.example.quranmemorizationdosen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = {
                    Text(
                        "Dashboard Dosen",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = {
                        tokenManager.clearTokens()
                        onLogout()
                    }) {
                        Text("Logout", color = Color.White, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    scrolledContainerColor = Color(0xFF2E7D32)
                ),
                modifier = Modifier.shadow(8.dp)
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E))
            )
        )
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
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Selamat datang, ${dosenResponse!!.data.nama}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    )
                                    Text(
                                        text = "NIP: ${dosenResponse!!.data.nip}",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )
                                    )
                                    Text(
                                        text = "Email: ${dosenResponse!!.data.email}",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Daftar Mahasiswa Bimbingan",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(dosenResponse!!.data.info_mahasiswa_pa.daftar_mahasiswa) { mahasiswa ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(500)),
                                exit = fadeOut(animationSpec = tween(500))
                            ) {
                                MahasiswaCard(mahasiswa) {
                                    navController.navigate("lihat_setoran/${mahasiswa.nim}")
                                }
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2E7D32)
                    )
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
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = mahasiswa.nama,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            )
            Text(
                text = "NIM: ${mahasiswa.nim}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF616161)
                )
            )
            Text(
                text = "Angkatan: ${mahasiswa.angkatan}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF616161)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progres: ${mahasiswa.info_setoran.persentase_progres_setor}%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = Color(0xFF616161)
                    )
                )
                LinearProgressIndicator(
                    progress = { mahasiswa.info_setoran.persentase_progres_setor / 100f },
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFDDAA33),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}