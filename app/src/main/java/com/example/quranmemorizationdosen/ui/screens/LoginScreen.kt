package com.example.quranmemorizationdosen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.quranmemorizationdosen.ui.theme.IslamicGold
import com.example.quranmemorizationdosen.ui.theme.IslamicGreen
import com.example.quranmemorizationdosen.ui.theme.IslamicWhite
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Logo UIN SUSKA RIAU",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "QuranMemorizationDosen",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                color = IslamicGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Setoran Hafalan Mahasiswa",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = IslamicGold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = IslamicGold
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
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = IslamicGold
                        )
                    )

                    AnimatedVisibility(
                        visible = showError,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        Text(
                            text = "Invalid credentials or network error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
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
                                            showError = false
                                        }
                                    } else {
                                        showError = true
                                    }
                                } catch (e: Exception) {
                                    showError = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Login", color = IslamicWhite, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}