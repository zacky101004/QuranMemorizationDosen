// ui/screens/DashboardViewModel.kt
package com.example.quranmemorizationdosen.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quranmemorizationdosen.TokenManager
import com.example.quranmemorizationdosen.data.api.DosenResponse
import com.example.quranmemorizationdosen.data.api.RetrofitClient
import com.example.quranmemorizationdosen.data.api.SetoranMahasiswaResponse
import com.example.quranmemorizationdosen.data.api.SetoranRequest
import com.example.quranmemorizationdosen.data.api.SetoranItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DashboardViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _dosenState = MutableStateFlow<DosenState>(DosenState.Idle)
    val dosenState: StateFlow<DosenState> = _dosenState

    private val _setoranState = MutableStateFlow<SetoranState>(SetoranState.Idle)
    val setoranState: StateFlow<SetoranState> = _setoranState

    private val TAG = "DashboardViewModel"

    fun fetchDosenInfo() {
        viewModelScope.launch {
            _dosenState.value = DosenState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    val response = RetrofitClient.apiService.getDosenInfo("Bearer $token")
                    if (response.isSuccessful) {
                        _dosenState.value = DosenState.Success(response.body()!!)
                    } else {
                        handleError(response.code(), response.message(), true)
                    }
                } else {
                    _dosenState.value = DosenState.Error("Token tidak ditemukan")
                }
            } catch (e: Exception) {
                _dosenState.value = DosenState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    fun fetchSetoranMahasiswa(nim: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    val response = RetrofitClient.apiService.getSetoranMahasiswa("Bearer $token", nim)
                    if (response.isSuccessful) {
                        _setoranState.value = SetoranState.Success(response.body()!!)
                    } else {
                        handleError(response.code(), response.message(), false)
                    }
                } else {
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun postSetoranMahasiswa(nim: String, idKomponenSetoran: String, namaKomponenSetoran: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    val request = SetoranRequest(
                        dataSetoran = listOf(
                            SetoranItem(
                                idKomponenSetoran = idKomponenSetoran,
                                namaKomponenSetoran = namaKomponenSetoran
                            )
                        )
                    )
                    val response = RetrofitClient.apiService.postSetoranMahasiswa(
                        token = "Bearer $token",
                        nim = nim,
                        request = request
                    )
                    if (response.isSuccessful) {
                        _setoranState.value = SetoranState.Success(null)
                        fetchSetoranMahasiswa(nim) // Refresh data setelah berhasil
                    } else {
                        _setoranState.value = SetoranState.Error("Gagal menambahkan setoran: ${response.message()}")
                    }
                } else {
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: Exception) {
                _setoranState.value = SetoranState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    fun deleteSetoranMahasiswa(nim: String, idSetoran: String, idKomponenSetoran: String, namaKomponenSetoran: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    val request = SetoranRequest(
                        dataSetoran = listOf(
                            SetoranItem(
                                id = idSetoran,
                                idKomponenSetoran = idKomponenSetoran,
                                namaKomponenSetoran = namaKomponenSetoran
                            )
                        )
                    )
                    val response = RetrofitClient.apiService.deleteSetoranMahasiswa(
                        token = "Bearer $token",
                        nim = nim,
                        id = idSetoran,
                        request = request
                    )
                    if (response.isSuccessful) {
                        _setoranState.value = SetoranState.Success(null)
                        fetchSetoranMahasiswa(nim) // Refresh data setelah berhasil
                    } else {
                        _setoranState.value = SetoranState.Error("Gagal menghapus setoran: ${response.message()}")
                    }
                } else {
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: Exception) {
                _setoranState.value = SetoranState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    private fun handleError(code: Int, message: String, isDosen: Boolean) {
        when (code) {
            401 -> {
                viewModelScope.launch {
                    val refreshToken = tokenManager.getRefreshToken()
                    if (refreshToken != null) {
                        val refreshResponse = RetrofitClient.kcApiService.refreshToken(
                            "setoran-mobile-dev",
                            "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                            "refresh_token",
                            refreshToken
                        )
                        if (refreshResponse.isSuccessful) {
                            refreshResponse.body()?.let { auth ->
                                tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                                if (isDosen) fetchDosenInfo() else fetchSetoranMahasiswa("")
                            }
                        } else {
                            if (isDosen) _dosenState.value = DosenState.Error("Gagal refresh token")
                            else _setoranState.value = SetoranState.Error("Gagal refresh token")
                        }
                    }
                }
            }
            else -> {
                if (isDosen) _dosenState.value = DosenState.Error("Gagal: $message (Kode: $code)")
                else _setoranState.value = SetoranState.Error("Gagal: $message (Kode: $code)")
            }
        }
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(TokenManager(context)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed class DosenState {
    object Idle : DosenState()
    object Loading : DosenState()
    data class Success(val data: DosenResponse) : DosenState()
    data class Error(val message: String) : DosenState()
}

sealed class SetoranState {
    object Idle : SetoranState()
    object Loading : SetoranState()
    data class Success(val data: SetoranMahasiswaResponse?) : SetoranState()
    data class Error(val message: String) : SetoranState()
}