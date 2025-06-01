package com.example.quranmemorizationdosen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranmemorizationdosen.data.api.SetoranItem
import com.example.quranmemorizationdosen.ui.navigation.BottomNavigationBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaSetoranScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dosenState by viewModel.dosenState.collectAsState()
    val setoranState by viewModel.setoranState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nimInput by remember { mutableStateOf("") }
    var idKomponenSetoranInput by remember { mutableStateOf("") }
    var namaKomponenSetoranInput by remember { mutableStateOf("") }
    var idSetoranInput by remember { mutableStateOf("") }
    var idKomponenSetoranDeleteInput by remember { mutableStateOf("") }
    var namaKomponenSetoranDeleteInput by remember { mutableStateOf("") }
    var nimExpanded by remember { mutableStateOf(false) }
    var komponenExpanded by remember { mutableStateOf(false) }
    var setoranExpanded by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Tambah Setoran", "Hapus Setoran")

    val mahasiswaList = when (val state = dosenState) {
        is DosenState.Success -> state.data.data.info_mahasiswa_pa.daftar_mahasiswa
        else -> emptyList()
    }

    val komponenSetoranList = when (val state = setoranState) {
        is SetoranState.Success -> state.data?.data?.setoran?.detail
            ?.filter { !it.sudah_setor }
            ?.map { it } ?: emptyList()
        else -> emptyList()
    }

    val idSetoranList = when (val state = setoranState) {
        is SetoranState.Success -> state.data?.data?.setoran?.detail
            ?.filter { it.sudah_setor && it.info_setoran != null }
            ?.mapNotNull { detail ->
                detail.info_setoran?.let { info ->
                    SetoranItem(
                        id = info.id,
                        idKomponenSetoran = detail.id,
                        namaKomponenSetoran = detail.nama
                    )
                }
            } ?: emptyList()
        else -> emptyList()
    }

    LaunchedEffect(nimInput) {
        if (nimInput.isNotBlank()) {
            viewModel.fetchSetoranMahasiswa(nimInput)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDosenInfo()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kelola Setoran",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32)
                ),
                modifier = Modifier.shadow(6.dp)
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        modifier = Modifier.background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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
                        text = "Pilih Mahasiswa",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF2E7D32)
                        )
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
                    ExposedDropdownMenuBox(
                        expanded = nimExpanded,
                        onExpandedChange = { nimExpanded = !nimExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (nimInput.isNotBlank()) {
                                val selected = mahasiswaList.find { it.nim == nimInput }
                                selected?.let { "${it.nim} - ${it.nama}" } ?: nimInput
                            } else "",
                            onValueChange = {},
                            label = { Text("NIM - Nama Mahasiswa") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2E7D32),
                                unfocusedBorderColor = Color(0xFF9E9E9E)
                            ),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = nimExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = nimExpanded,
                            onDismissRequest = { nimExpanded = false }
                        ) {
                            mahasiswaList.forEach { mahasiswa ->
                                DropdownMenuItem(
                                    text = { Text("${mahasiswa.nim} - ${mahasiswa.nama}") },
                                    onClick = {
                                        nimInput = mahasiswa.nim
                                        nimExpanded = false
                                    },
                                    modifier = Modifier.background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF2E7D32),
                divider = { Divider(color = Color(0xFFE0E0E0)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
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
                                    text = "Komponen Setoran",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = komponenExpanded,
                                    onExpandedChange = { komponenExpanded = !komponenExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = namaKomponenSetoranInput,
                                        onValueChange = {},
                                        label = { Text("Pilih Komponen Setoran") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        readOnly = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF2E7D32),
                                            unfocusedBorderColor = Color(0xFF9E9E9E)
                                        ),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = komponenExpanded)
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = komponenExpanded,
                                        onDismissRequest = { komponenExpanded = false }
                                    ) {
                                        komponenSetoranList.forEach { komponen ->
                                            DropdownMenuItem(
                                                text = { Text(komponen.nama) },
                                                onClick = {
                                                    idKomponenSetoranInput = komponen.id
                                                    namaKomponenSetoranInput = komponen.nama
                                                    komponenExpanded = false
                                                },
                                                modifier = Modifier.background(Color.White)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (nimInput.isBlank()) {
                                            scope.launch { snackbarHostState.showSnackbar("Pilih mahasiswa terlebih dahulu") }
                                        } else if (idKomponenSetoranInput.isBlank() || namaKomponenSetoranInput.isBlank()) {
                                            scope.launch { snackbarHostState.showSnackbar("Pilih komponen setoran") }
                                        } else {
                                            viewModel.postSetoranMahasiswa(nimInput, idKomponenSetoranInput, namaKomponenSetoranInput)
                                            idKomponenSetoranInput = ""
                                            namaKomponenSetoranInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50))
                                        )
                                    )
                                ) {
                                    Text(
                                        "Tambah Setoran",
                                        fontSize = 16.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
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
                                    text = "Pilih Setoran untuk Dihapus",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = setoranExpanded,
                                    onExpandedChange = { setoranExpanded = !setoranExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = namaKomponenSetoranDeleteInput,
                                        onValueChange = {},
                                        label = { Text("Pilih Setoran") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        readOnly = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF2E7D32),
                                            unfocusedBorderColor = Color(0xFF9E9E9E)
                                        ),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = setoranExpanded)
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = setoranExpanded,
                                        onDismissRequest = { setoranExpanded = false }
                                    ) {
                                        idSetoranList.forEach { setoran ->
                                            DropdownMenuItem(
                                                text = { Text(setoran.namaKomponenSetoran) }, // Perubahan: Hanya tampilkan nama surah
                                                onClick = {
                                                    idSetoranInput = setoran.id ?: ""
                                                    idKomponenSetoranDeleteInput = setoran.idKomponenSetoran
                                                    namaKomponenSetoranDeleteInput = setoran.namaKomponenSetoran
                                                    setoranExpanded = false
                                                },
                                                modifier = Modifier.background(Color.White)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (nimInput.isBlank()) {
                                            scope.launch { snackbarHostState.showSnackbar("Pilih mahasiswa terlebih dahulu") }
                                        } else if (idSetoranInput.isBlank() || idKomponenSetoranDeleteInput.isBlank() || namaKomponenSetoranDeleteInput.isBlank()) {
                                            scope.launch { snackbarHostState.showSnackbar("Pilih setoran untuk dihapus") }
                                        } else {
                                            viewModel.deleteSetoranMahasiswa(
                                                nimInput,
                                                idSetoranInput,
                                                idKomponenSetoranDeleteInput,
                                                namaKomponenSetoranDeleteInput
                                            )
                                            idSetoranInput = ""
                                            idKomponenSetoranDeleteInput = ""
                                            namaKomponenSetoranDeleteInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFEF4444), Color(0xFFF87171))
                                        )
                                    )
                                ) {
                                    Text(
                                        "Hapus Setoran",
                                        fontSize = 16.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            when (val state = setoranState) {
                is SetoranState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2E7D32))
                    }
                }
                is SetoranState.Success -> {
                    state.data?.let {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Aksi setoran berhasil, data diperbarui",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF065F46),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
                is SetoranState.Error -> {
                    LaunchedEffect(state) {
                        scope.launch { snackbarHostState.showSnackbar(state.message) }
                    }
                }
                else -> {}
            }
        }
    }
}