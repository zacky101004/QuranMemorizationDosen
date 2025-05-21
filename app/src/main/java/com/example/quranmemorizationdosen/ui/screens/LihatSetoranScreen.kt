package com.example.quranmemorizationdosen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranmemorizationdosen.data.api.SetoranMahasiswaResponse
import com.example.quranmemorizationdosen.ui.navigation.BottomNavigationBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LihatSetoranScreen(navController: NavController, nim: String) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val setoranState by viewModel.setoranState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(nim) {
        viewModel.fetchSetoranMahasiswa(nim)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Setoran Mahasiswa", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3B82F6),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1094A4),
                            Color(0xFFD083EE),
                            Color(0xFF3787A8)
                        )
                    )
                )
        ) {
            when (val state = setoranState) {
                is SetoranState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SetoranState.Success -> {
                    state.data?.let { setoran ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                StyledSectionCard(title = "Info Mahasiswa") {
                                    ProfileItem(label = "Nama", value = setoran.data.info.nama)
                                    ProfileItem(label = "NIM", value = setoran.data.info.nim)
                                    ProfileItem(label = "Email", value = setoran.data.info.email)
                                    ProfileItem(label = "Angkatan", value = setoran.data.info.angkatan)
                                    ProfileItem(label = "Semester", value = setoran.data.info.semester.toString())
                                    ProfileItem(label = "Dosen PA", value = setoran.data.info.dosen_pa.nama)
                                }
                            }
                            item {
                                StyledSectionCard(title = "Progres Setoran") {
                                    Text(
                                        text = "Persentase: ${setoran.data.setoran.info_dasar.persentase_progres_setor}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = (setoran.data.setoran.info_dasar.persentase_progres_setor / 100.0).toFloat(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp),
                                        color = when {
                                            setoran.data.setoran.info_dasar.persentase_progres_setor < 30 -> Color(0xFFE57373)
                                            setoran.data.setoran.info_dasar.persentase_progres_setor < 70 -> Color(0xFFFFB74D)
                                            else -> Color(0xFF81C784)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        StatisticBox(
                                            title = "Wajib Setor",
                                            value = setoran.data.setoran.info_dasar.total_wajib_setor.toString(),
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                        StatisticBox(
                                            title = "Sudah Setor",
                                            value = setoran.data.setoran.info_dasar.total_sudah_setor.toString(),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        )
                                        StatisticBox(
                                            title = "Belum Setor",
                                            value = setoran.data.setoran.info_dasar.total_belum_setor.toString(),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Terakhir Setor: ${setoran.data.setoran.info_dasar.terakhir_setor}")
                                    setoran.data.setoran.info_dasar.tgl_terakhir_setor?.let {
                                        Text("Tanggal: ${it.take(10)}")
                                    }
                                }
                            }
                            item {
                                StyledSectionCard(title = "Ringkasan Setoran") {
                                    setoran.data.setoran.ringkasan.forEach { ringkasan ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(ringkasan.label)
                                            Text("${ringkasan.total_sudah_setor}/${ringkasan.total_wajib_setor} (${ringkasan.persentase_progres_setor}%)")
                                        }
                                    }
                                }
                            }
                            item {
                                StyledSectionCard(title = "Detail Setoran") {
                                    setoran.data.setoran.detail.forEach { item ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            elevation = CardDefaults.cardElevation(1.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(item.nama, fontWeight = FontWeight.SemiBold)
                                                Text("Label: ${item.label}")
                                                Text("Status: ${if (item.sudah_setor) "Sudah Setor" else "Belum Setor"}")
                                                item.info_setoran?.let { info ->
                                                    Text("Tanggal Setoran: ${info.tgl_setoran}")
                                                    Text("Tanggal Validasi: ${info.tgl_validasi}")
                                                    Text("Dosen: ${info.dosen_yang_mengesahkan.nama}")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Data setoran kosong",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is SetoranState.Error -> {
                    LaunchedEffect(state) {
                        scope.launch { snackbarHostState.showSnackbar(state.message) }
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gagal memuat setoran: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Silakan pilih mahasiswa untuk melihat setoran",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StyledSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StatisticBox(title: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}