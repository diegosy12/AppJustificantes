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
import com.google.firebase.auth.FirebaseAuth
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

    val auth = FirebaseAuth.getInstance()
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
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(
                                context,
                                "Introduce el correo y la contraseña",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userEmail = auth.currentUser?.email ?: email.trim()
                                    onLoginClick(userEmail)
                                } else {
                                    Toast.makeText(
                                        context,
                                        task.exception?.message ?: "Error al iniciar sesión",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
