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
                Log.e(TAG, "fetchDosenInfo error: ${e.message}", e)
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
                Log.e(TAG, "fetchSetoranMahasiswa HTTP error: ${e.message()}", e)
            } catch (e: Exception) {
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
                Log.e(TAG, "fetchSetoranMahasiswa error: ${e.message}", e)
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
                    Log.d(TAG, "postSetoranMahasiswa: Sending request for NIM=$nim, idKomponen=$idKomponenSetoran")
                    val response = RetrofitClient.apiService.postSetoranMahasiswa(
                        token = "Bearer $token",
                        nim = nim,
                        request = request
                    )
                    if (response.isSuccessful) {
                        _setoranState.value = SetoranState.Success(null)
                        Log.d(TAG, "postSetoranMahasiswa: Success")
                        fetchSetoranMahasiswa(nim) // Refresh data setelah berhasil
                    } else {
                        _setoranState.value = SetoranState.Error("Gagal menambahkan setoran: ${response.message()} (Kode: ${response.code()})")
                        Log.e(TAG, "postSetoranMahasiswa error: ${response.message()} (Kode: ${response.code()})")
                    }
                } else {
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                    Log.e(TAG, "postSetoranMahasiswa error: Token tidak ditemukan")
                }
            } catch (e: Exception) {
                _setoranState.value = SetoranState.Error("Kesalahan: ${e.message}")
                Log.e(TAG, "postSetoranMahasiswa error: ${e.message}", e)
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
                    Log.d(TAG, "deleteSetoranMahasiswa: Sending request for NIM=$nim, idSetoran=$idSetoran, idKomponen=$idKomponenSetoran")
                    val response = RetrofitClient.apiService.deleteSetoranMahasiswa(
                        token = "Bearer $token",
                        nim = nim,
                        id = idSetoran,
                        request = request
                    )
                    if (response.isSuccessful) {
                        _setoranState.value = SetoranState.Success(null)
                        Log.d(TAG, "deleteSetoranMahasiswa: Success")
                        fetchSetoranMahasiswa(nim) // Refresh data setelah berhasil
                    } else {
                        _setoranState.value = SetoranState.Error("Gagal menghapus setoran: ${response.message()} (Kode: ${response.code()})")
                        Log.e(TAG, "deleteSetoranMahasiswa error: ${response.message()} (Kode: ${response.code()})")
                    }
                } else {
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                    Log.e(TAG, "deleteSetoranMahasiswa error: Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
                Log.e(TAG, "deleteSetoranMahasiswa HTTP error: ${e.message()} (Kode: ${e.code()})", e)
            } catch (e: Exception) {
                _setoranState.value = SetoranState.Error("Kesalahan: ${e.message}")
                Log.e(TAG, "deleteSetoranMahasiswa error: ${e.message}", e)
            }
        }
    }

    private fun handleError(code: Int, message: String, isDosen: Boolean) {
        Log.e(TAG, "handleError: Code=$code, Message=$message, isDosen=$isDosen")
        when (code) {
            401 -> {
                viewModelScope.launch {
                    val refreshToken = tokenManager.getRefreshToken()
                    if (refreshToken != null) {
                        Log.d(TAG, "handleError: Attempting to refresh token")
                        val refreshResponse = RetrofitClient.kcApiService.refreshToken(
                            "setoran-mobile-dev",
                            "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                            "refresh_token",
                            refreshToken
                        )
                        if (refreshResponse.isSuccessful) {
                            refreshResponse.body()?.let { auth ->
                                tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                                Log.d(TAG, "handleError: Token refreshed successfully")
                                if (isDosen) fetchDosenInfo() else fetchSetoranMahasiswa("")
                            }
                        } else {
                            if (isDosen) _dosenState.value = DosenState.Error("Gagal refresh token: ${refreshResponse.message()} (Kode: ${refreshResponse.code()})")
                            else _setoranState.value = SetoranState.Error("Gagal refresh token: ${refreshResponse.message()} (Kode: ${refreshResponse.code()})")
                            Log.e(TAG, "handleError: Failed to refresh token: ${refreshResponse.message()} (Kode: ${refreshResponse.code()})")
                        }
                    } else {
                        if (isDosen) _dosenState.value = DosenState.Error("Refresh token tidak ditemukan")
                        else _setoranState.value = SetoranState.Error("Refresh token tidak ditemukan")
                        Log.e(TAG, "handleError: Refresh token tidak ditemukan")
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