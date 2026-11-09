package com.schalom.afterdark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

private val Bg = Color(0xFF100D16)
private val Panel = Color(0xFF1C1725)
private val Accent = Color(0xFFB77BFF)
private val TextMain = Color(0xFFF7F1FF)
private val Muted = Color(0xFFB8AFC4)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AfterdarkApp(this) }
    }
}

private data class ChatMessage(val text: String, val fromUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AfterdarkApp(activity: ComponentActivity) {
    val prefs = remember {
        val key = MasterKey.Builder(activity).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            activity, "afterdark_secure", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    var ageAccepted by remember { mutableStateOf(prefs.getBoolean("age_ok", false)) }
    var showSettings by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(prefs.getString("api_key", "") ?: "") }
    var endpoint by remember { mutableStateOf(prefs.getString("endpoint", "https://api.openai.com/v1/chat/completions") ?: "") }
    var model by remember { mutableStateOf(prefs.getString("model", "gpt-4o-mini") ?: "") }
    var userInput by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Configure ta clé API pour commencer.") }
    val messages = remember { mutableStateListOf(ChatMessage("Bonsoir. Je suis ton personnage virtuel AFTERDARK. Je suis une IA fictive, pas une personne réelle. Que souhaites-tu faire ce soir ?", false)) }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Accent, background = Bg, surface = Panel,
        onPrimary = Color.Black, onBackground = TextMain, onSurface = TextMain
    )) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            when {
                !ageAccepted -> AgeGate(
                    onAccept = {
                        prefs.edit().putBoolean("age_ok", true).apply()
                        ageAccepted = true
                    }
                )
                showSettings -> SettingsScreen(
                    apiKey, { apiKey = it }, endpoint, { endpoint = it },
                    model, { model = it },
                    onSave = {
                        prefs.edit().putString("api_key", apiKey.trim())
                            .putString("endpoint", endpoint.trim())
                            .putString("model", model.trim()).apply()
                        status = "Configuration enregistrée sur cet appareil."
                        showSettings = false
                    },
                    onBack = { showSettings = false }
                )
                else -> Column(Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Column {
                                Text("AFTERDARK AI", fontWeight = FontWeight.Bold, color = TextMain)
                                Text("BY SCHALOM KINNINNON  •  PERSONNAGE FICTIF 18+", fontSize = 9.sp, color = Muted)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = Accent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Panel)
                    )
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(54.dp).background(Accent.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
                                    Text("A", color = Accent, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("AVA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
                                    Text("Compagne virtuelle • adulte fictive", color = Muted, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(messages) { msg ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start) {
                                    Surface(
                                        color = if (msg.fromUser) Accent.copy(alpha = .22f) else Panel,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.widthIn(max = 310.dp)
                                    ) {
                                        Text(msg.text, Modifier.padding(13.dp), color = TextMain, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                        Text(status, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 5.dp))
                    }
                    Row(Modifier.fillMaxWidth().background(Panel).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = userInput, onValueChange = { userInput = it },
                            modifier = Modifier.weight(1f), placeholder = { Text("Écris ton message…") },
                            shape = RoundedCornerShape(18.dp), singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            enabled = !busy && userInput.isNotBlank(),
                            onClick = {
                                val prompt = userInput.trim()
                                userInput = ""
                                messages.add(ChatMessage(prompt, true))
                                busy = true
                                status = "AFTERDARK réfléchit…"
                                sendToAi(apiKey, endpoint, model, messages.toList()) { answer, error ->
                                    activity.runOnUiThread {
                                        busy = false
                                        if (answer != null) {
                                            messages.add(ChatMessage(answer, false))
                                            status = "Connectée"
                                        } else status = error ?: "Erreur de connexion."
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgeGate(onAccept: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("AFTERDARK", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Accent)
        Text("AI", fontSize = 22.sp, color = TextMain, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text("Accès réservé aux adultes", fontSize = 21.sp, color = TextMain, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("Cette application propose un personnage virtuel fictif destiné à un public adulte. Elle ne représente pas une personne réelle. En continuant, tu confirmes avoir au moins 18 ans et respecter les lois applicables.", color = Muted)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) { Text("J'AI 18 ANS OU PLUS") }
        Text("BY SCHALOM KINNINNON", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 22.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    apiKey: String, onKey: (String) -> Unit,
    endpoint: String, onEndpoint: (String) -> Unit,
    model: String, onModel: (String) -> Unit,
    onSave: () -> Unit, onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Accent) }
            Text("Configuration IA", fontSize = 22.sp, color = TextMain, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text("Fournisseur compatible avec l'API OpenAI Chat Completions", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = endpoint, onValueChange = onEndpoint, label = { Text("URL de l'API") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = model, onValueChange = onModel, label = { Text("Nom du modèle") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = apiKey, onValueChange = onKey, label = { Text("Clé API") },
            modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(8.dp))
        Text("La clé est stockée localement dans les préférences chiffrées Android. Ne partage pas ton APK avec une clé personnelle déjà enregistrée.", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("ENREGISTRER") }
    }
}

private fun sendToAi(
    apiKey: String, endpoint: String, model: String,
    history: List<ChatMessage>,
    callback: (String?, String?) -> Unit
) {
    if (apiKey.isBlank()) { callback(null, "Ajoute une clé API dans les paramètres."); return }
    if (!endpoint.startsWith("https://")) { callback(null, "L'URL doit utiliser HTTPS."); return }

    val msgs = JSONArray()
    msgs.put(JSONObject().put("role", "system").put("content",
        "Tu es Ava, un personnage virtuel fictif clairement identifié comme une IA. Tu échanges avec des adultes. Reste respectueuse, consensuelle et non coercitive. Ne prétends jamais être une personne réelle. Refuse les demandes impliquant des mineurs, des personnes réelles sans consentement, du chantage ou des actes illégaux. Réponds en français."))
    history.takeLast(30).forEach {
        msgs.put(JSONObject().put("role", if (it.fromUser) "user" else "assistant").put("content", it.text))
    }
    val body = JSONObject().put("model", model.ifBlank { "gpt-4o-mini" })
        .put("messages", msgs).put("temperature", 0.85).toString()
        .toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder().url(endpoint).addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json").post(body).build()
    val client = OkHttpClient.Builder().callTimeout(45, TimeUnit.SECONDS).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) { callback(null, "Connexion impossible : ${e.message}") }
        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) { callback(null, "Erreur API HTTP ${it.code}. Vérifie la clé, l'URL et le modèle."); return }
                try {
                    val json = JSONObject(it.body?.string().orEmpty())
                    val answer = json.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content")
                    callback(answer, null)
                } catch (e: Exception) { callback(null, "Réponse API non reconnue : ${e.message}") }
            }
        }
    })
}
