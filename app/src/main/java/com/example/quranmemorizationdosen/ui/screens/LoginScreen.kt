package com.example.quranmemorizationdosen.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranmemorizationdosen.R
import com.example.quranmemorizationdosen.data.api.RetrofitClient
import com.example.quranmemorizationdosen.TokenManager
import com.example.quranmemorizationdosen.ui.theme.IslamicGreen
import com.example.quranmemorizationdosen.ui.theme.IslamicWhite
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(IslamicWhite, IslamicGreen.copy(alpha = 0.1f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 64.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            Text(
                text = "Quran Memorization Dosen",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                color = IslamicGreen,
                modifier = Modifier.padding(top = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.kcApiService.login(
                                        clientId = "setoran-mobile-dev",
                                        clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                                        grantType = "password",
                                        username = username,
                                        password = password,
                                        scope = "openid profile email"
                                    )
                                    if (response.isSuccessful) {
                                        response.body()?.let { auth ->
                                            tokenManager.saveTokens(
                                                auth.access_token,
                                                auth.refresh_token,
                                                auth.id_token
                                            )
                                            onLoginSuccess()
                                        }
                                    } else {
                                        errorMessage = "Login gagal: ${response.message()}"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Kesalahan: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Login", color = IslamicWhite)
                    }
                }
            }
        }
    }
}