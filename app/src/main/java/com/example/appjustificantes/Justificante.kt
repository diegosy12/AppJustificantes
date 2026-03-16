package com.example.appjustificantes

import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(email: String,navigationConroller: NavHostController) {
    // Formato de fecha
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var date by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    dateFormat.isLenient = false

    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No hay archivo seleccionado") }

    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    // Launcher para seleccionar archivo
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFile = uri
        fileName = uri?.lastPathSegment ?: "Archivo seleccionado"
    }
    Column(
        modifier = Modifier
            .fillMaxSize().background(Color(0xFFF2F2F2)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(Color.Black)){
            Spacer(modifier = Modifier.height(30.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically){
                Text("Justificante de Faltas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(top = 20.dp).weight(1f) )
                Button(onClick = {navigationConroller.popBackStack()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                ),shape = RoundedCornerShape(5.dp), modifier = Modifier.padding(5.dp)) {
                    Text("Salir")
                }
            }

        }
        Card(modifier = Modifier.fillMaxWidth().padding(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )) {
            Column(modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start){
                Text(
                    text = "Subir Justificante", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Fecha de la falta", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        error = !isValidDate(it, dateFormat)
                    },
                    label = { Text("Fecha (dd/MM/yyyy)") },
                    placeholder = { Text("Ej: 13/02/2026") },
                    isError = error,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF2F2F2),
                        unfocusedContainerColor = Color(0xFFF2F2F2)
                    )
                )
                if (error) {
                    Text(
                        text = "Fecha inválida",
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Archivo justificante", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { fileLauncher.launch("*/*") }, // cualquier tipo de archivo
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("Seleccionar Archivo")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedFile != null) {
                    Text(
                        text = "Archivo seleccionado: ${fileName}",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (date.isBlank()) {
                            Toast.makeText(context, "Introduce la fecha", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!isValidDate(date, dateFormat)) {
                            Toast.makeText(context, "La fecha no es válida", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedFile == null) {
                            Toast.makeText(context, "Selecciona un archivo", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val falta = hashMapOf(
                            "email" to email,
                            "fecha" to date,
                            "justificanteNombre" to fileName
                        )

                        db.collection("faltas")
                            .add(falta)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Falta guardada correctamente", Toast.LENGTH_SHORT).show()
                                date = ""
                                selectedFile = null
                                fileName = "No hay archivo seleccionado"
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("Guardar")
                }
            }

        }

    }
}

// Función de validación
fun isValidDate(input: String, format: SimpleDateFormat): Boolean {
    return try {
        format.parse(input)
        true
    } catch (e: ParseException) {
        false
    }
}
