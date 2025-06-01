package com.example.quranmemorizationdosen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranmemorizationdosen.data.api.InfoSetoranMhs
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

    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(nim) {
        viewModel.fetchSetoranMahasiswa(nim)
    }

    val tabs = listOf("Progres", "Ringkasan")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lihat Setoran Mahasiswa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32)
                ),
                modifier = Modifier.shadow(6.dp)
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.background(Color(0xFFFFFFFF))
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Info Mahasiswa
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Informasi Mahasiswa",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF2E7D32)
                            )
                        )
                        Divider(color = Color(0xFFE0E0E0))
                        when (val state = setoranState) {
                            is SetoranState.Success -> {
                                state.data?.let { setoran ->
                                    ProfileItem(label = "Nama", value = setoran.data.info.nama)
                                    ProfileItem(label = "NIM", value = setoran.data.info.nim)
                                    ProfileItem(label = "Email", value = setoran.data.info.email)
                                    ProfileItem(label = "Angkatan", value = setoran.data.info.angkatan)
                                    ProfileItem(label = "Semester", value = setoran.data.info.semester.toString())
                                    ProfileItem(label = "Dosen PA", value = setoran.data.info.dosen_pa.nama)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            item {
                // Progres dan Ringkasan dalam Tab
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Progres & Ringkasan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF2E7D32)
                            )
                        )
                        Divider(color = Color(0xFFE0E0E0))
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF2E7D32)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = if (selectedTabIndex == index) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                                        )
                                    }
                                )
                            }
                        }
                        when (selectedTabIndex) {
                            0 -> {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(300)),
                                    exit = fadeOut(animationSpec = tween(300))
                                ) {
                                    when (val state = setoranState) {
                                        is SetoranState.Success -> {
                                            state.data?.let { setoran ->
                                                Column(
                                                    modifier = Modifier
                                                        .padding(top = 16.dp)
                                                        .fillMaxWidth(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        progress = (setoran.data.setoran.info_dasar.persentase_progres_setor / 100.0).toFloat(),
                                                        modifier = Modifier.size(120.dp),
                                                        color = Color(0xFF2E7D32),
                                                        trackColor = Color(0xFFE0E0E0),
                                                        strokeWidth = 12.dp
                                                    )
                                                    Text(
                                                        text = "${setoran.data.setoran.info_dasar.persentase_progres_setor}%",
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 24.sp,
                                                            color = Color(0xFF2E7D32)
                                                        )
                                                    )
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceEvenly
                                                    ) {
                                                        StatisticBox(
                                                            title = "Wajib",
                                                            value = setoran.data.setoran.info_dasar.total_wajib_setor.toString(),
                                                            color = Color(0xFF4CAF50)
                                                        )
                                                        StatisticBox(
                                                            title = "Sudah",
                                                            value = setoran.data.setoran.info_dasar.total_sudah_setor.toString(),
                                                            color = Color(0xFF81C784)
                                                        )
                                                        StatisticBox(
                                                            title = "Belum",
                                                            value = setoran.data.setoran.info_dasar.total_belum_setor.toString(),
                                                            color = Color(0xFFC8E6C9)
                                                        )
                                                    }
                                                    Text(
                                                        text = "Terakhir Setor: ${setoran.data.setoran.info_dasar.terakhir_setor}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF2E7D32)
                                                        )
                                                    )
                                                    setoran.data.setoran.info_dasar.tgl_terakhir_setor?.let {
                                                        Text(
                                                            text = "Tanggal: ${it.take(10)}",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontSize = 14.sp,
                                                                color = Color(0xFF2E7D32)
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                            1 -> {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(300)),
                                    exit = fadeOut(animationSpec = tween(300))
                                ) {
                                    when (val state = setoranState) {
                                        is SetoranState.Success -> {
                                            state.data?.let { setoran ->
                                                Column(
                                                    modifier = Modifier
                                                        .padding(top = 16.dp)
                                                        .fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    setoran.data.setoran.ringkasan.forEach { ringkasan ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = ringkasan.label,
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    fontWeight = FontWeight.Medium,
                                                                    fontSize = 14.sp,
                                                                    color = Color(0xFF2E7D32)
                                                                )
                                                            )
                                                            Text(
                                                                text = "${ringkasan.total_sudah_setor}/${ringkasan.total_wajib_setor} (${ringkasan.persentase_progres_setor}%)",
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    fontSize = 14.sp,
                                                                    color = Color(0xFF4CAF50)
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Detail Setoran
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Detail Setoran",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF2E7D32)
                            )
                        )
                        Divider(color = Color(0xFFE0E0E0))
                        when (val state = setoranState) {
                            is SetoranState.Success -> {
                                state.data?.let { setoran ->
                                    setoran.data.setoran.detail.forEach { item ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = item.nama,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontSize = 16.sp,
                                                        color = Color(0xFF2E7D32)
                                                    )
                                                )
                                                Text(
                                                    text = "Label: ${item.label}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF4CAF50)
                                                    )
                                                )
                                                Text(
                                                    text = "Status: ${if (item.sudah_setor) "Sudah Setor" else "Belum Setor"}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 14.sp,
                                                        color = if (item.sudah_setor) Color(0xFF4CAF50) else Color(0xFFEF4444)
                                                    )
                                                )
                                                item.info_setoran?.let { info: InfoSetoranMhs ->
                                                    Text(
                                                        text = "Tanggal Setoran: ${info.tgl_setoran}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF4CAF50)
                                                        )
                                                    )
                                                    Text(
                                                        text = "Tanggal Validasi: ${info.tgl_validasi}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF4CAF50)
                                                        )
                                                    )
                                                    Text(
                                                        text = "Dosen: ${info.dosen_yang_mengesahkan.nama}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF4CAF50)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
        when (val state = setoranState) {
            is SetoranState.Error -> {
                LaunchedEffect(state) {
                    scope.launch { snackbarHostState.showSnackbar(state.message) }
                }
            }
            else -> {}
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
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = Color(0xFF4CAF50)
            )
        )
    }
}

@Composable
fun StatisticBox(title: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2E7D32)
            )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                color = Color(0xFF2E7D32)
            )
        )
    }
}