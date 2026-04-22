package com.example.appjustificantes

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@Composable
fun HomeScreen(email: String, navigationConroller: NavHostController) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    dateFormat.isLenient = false

    var date by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No hay archivo seleccionado") }
    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFile = uri
        fileName = uri?.let { getFileName(context, it) } ?: "No hay archivo seleccionado"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Justificante de Faltas",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .weight(1f)
                )
                Button(
                    onClick = { navigationConroller.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text("Salir")
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Subir Justificante",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Fecha de la falta", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        error = it.isNotBlank() && !isValidDate(it, dateFormat)
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
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Archivo justificante", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { fileLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
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
                        text = "Archivo seleccionado: $fileName",
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

                        val fileUri = selectedFile
                        if (fileUri == null) {
                            Toast.makeText(context, "Selecciona un archivo", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isUploading = true
                        val storagePath = buildStoragePath(email, fileName)
                        val storageRef = storage.reference.child(storagePath)

                        storageRef.putFile(fileUri)
                            .continueWithTask { uploadTask ->
                                if (!uploadTask.isSuccessful) {
                                    uploadTask.exception?.let { throw it }
                                }
                                storageRef.downloadUrl
                            }
                            .addOnSuccessListener { downloadUri ->
                                val falta = hashMapOf(
                                    "email" to email,
                                    "fecha" to date,
                                    "justificanteNombre" to fileName,
                                    "justificanteRuta" to storagePath,
                                    "justificanteUrl" to downloadUri.toString()
                                )

                                db.collection("faltas")
                                    .add(falta)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Justificante subido correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        date = ""
                                        selectedFile = null
                                        fileName = "No hay archivo seleccionado"
                                        isUploading = false
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Archivo subido, pero error al guardar: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isUploading = false
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error al subir el archivo: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isUploading = false
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(if (isUploading) "Subiendo..." else "Guardar")
                }
            }
        }
    }
}

fun isValidDate(input: String, format: SimpleDateFormat): Boolean {
    return try {
        format.parse(input)
        true
    } catch (_: ParseException) {
        false
    }
}

fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && it.moveToFirst()) {
            return it.getString(nameIndex)
        }
    }

    return uri.lastPathSegment ?: "Archivo seleccionado"
}

fun buildStoragePath(email: String, fileName: String): String {
    val safeEmail = email.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val safeFileName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
    return "justificantes/$safeEmail/${System.currentTimeMillis()}_${UUID.randomUUID()}_$safeFileName"
}
