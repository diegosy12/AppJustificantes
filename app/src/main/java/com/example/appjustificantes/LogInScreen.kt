package com.example.appjustificantes

import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


@Composable
fun LoginScreen(navigationConroller: NavHostController,
                onLoginClick: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize().background(Color(0xFFF2F2F2)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.fillMaxWidth().padding(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )) {
            Column(modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(id = R.drawable.iconoarchivo), modifier = Modifier.size(60.dp),contentDescription = "icono archivo")
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Justificante de Faltas", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                Text("Accede para justificar tus ausencias")
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ){
                    Text("Correo electrónico", fontWeight = FontWeight.Bold,fontSize = 17.sp,)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("alumno@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF2F2F2),
                            unfocusedContainerColor = Color(0xFFF2F2F2)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Contraseña", fontWeight = FontWeight.Bold,fontSize = 17.sp,)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("pasw_123") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF2F2F2),
                            unfocusedContainerColor = Color(0xFFF2F2F2)
                        )
                    )
                }


                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Lanzamos coroutine para no bloquear UI
                        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                            loginApi(email, password, context, onLoginClick)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp),
                ) {
                    Text("Iniciar Sesión")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("¿Olvidaste tu contraseña?")
            }

        }

    }
}


// Función para hacer login a la API Node.js
suspend fun loginApi(
    email: String,
    password: String,
    context: android.content.Context,
    onLoginClick: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val json = JSONObject()
            json.put("email", email)
            json.put("password", password)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("http://192.168.1.137:3000/login") // TU IP LOCAL Y PUERTO DE NODE
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LoginAPI", "Error de conexión: ${e.message}")
                    android.os.Handler(context.mainLooper).post {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseString = response.body?.string() ?: ""
                    Log.d("LoginAPI", "Respuesta completa: $responseString")

                    val jsonResponse = JSONObject(responseString)
                    if (jsonResponse.optBoolean("success")) {
                        val userEmail = jsonResponse.getJSONObject("user").getString("email")
                        android.os.Handler(context.mainLooper).post {
                            onLoginClick(userEmail)
                        }
                    } else {
                        val message = jsonResponse.optString("message", "Error desconocido")
                        android.os.Handler(context.mainLooper).post {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("LoginAPI", "Excepción: ${e.message}")
            android.os.Handler(context.mainLooper).post {
                Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}