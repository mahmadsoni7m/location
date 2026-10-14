package com.phonelocation.twogis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF0B0C0F)
private val Panel = Color(0xFF17191F)
private val Gold = Color(0xFFD8B15A)
private val Muted = Color(0xFFB5B8C0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(
            primary = Gold, background = Ink, surface = Panel,
            onPrimary = Ink, onBackground = Color.White, onSurface = Color.White
        )) { PhoneLocationApp() } }
    }
}

@Composable
private fun PhoneLocationApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var accuracy by remember { mutableStateOf<Float?>(null) }
    var updated by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("Ҷойгиршавӣ ҳоло гирифта нашудааст.") }
    var busy by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("+992") }
    var sharing by remember { mutableStateOf(false) }
    var consent by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf("home") }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allowed = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (allowed) {
            scope.launch {
                busy = true
                try {
                    val client = LocationServices.getFusedLocationProviderClient(context)
                    val loc = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                    if (loc != null) {
                        latitude = loc.latitude
                        longitude = loc.longitude
                        accuracy = loc.accuracy
                        updated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        status = "Ҷойгиршавӣ нав шуд."
                    } else status = "GPS ҳоло координата надод. GPS ва интернетро санҷед."
                } catch (e: Exception) {
                    status = "Ҷойгиршавӣ дастрас нест: иҷозат ё GPS-ро санҷед."
                } finally { busy = false }
            }
        } else status = "Иҷозати ҷойгиршавӣ дода нашуд."
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(Gold, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                Text("⌖", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("PHONE LOCATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text("2GIS • Мубодилаи бехатари макон", color = Gold, fontSize = 12.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Махфият пеш аз ҳама", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Рақами телефон танҳо маконро муайян карда наметавонад. Макон танҳо аз дастгоҳи шахсе гирифта мешавад, ки иҷозат медиҳад.",
                    color = Muted, lineHeight = 21.sp)
                Text("Ҳолат: $status", color = if (latitude != null) Gold else Muted)
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold)
                Button(onClick = {
                    locationPermission.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text(if (busy) "Лутфан интизор шавед…" else "Гирифтани макони ман")
                }
                if (latitude != null && longitude != null) {
                    Text("Арзи ҷуғрофӣ: $latitude")
                    Text("Тӯли ҷуғрофӣ: $longitude")
                    Text("Дақиқӣ: ${accuracy?.let { "%.1f м".format(it) } ?: "номаълум"}")
                    Text("Навсозӣ: ${updated ?: "номаълум"}", color = Muted, fontSize = 12.sp)
                    OutlinedButton(onClick = {
                        val uri = Uri.parse("dgis://2gis.ru/routeSearch/rsType/pedestrian/to/$longitude,$latitude")
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        catch (_: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")))
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Кушодан дар 2GIS") }
                    OutlinedButton(onClick = {
                        val text = "Ҷойгиршавии ман (2GIS): dgis://2gis.ru/routeSearch/rsType/pedestrian/to/$longitude,$latitude\nКоординатаҳо: $latitude, $longitude"
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                        }, "Мубодилаи ҷойгиршавӣ"))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Мубодилаи макон") }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Даъват кардани шахс", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Ин прототип рақамро барои пайгирӣ истифода намекунад. Барои даъвати воқеӣ, сервери HTTPS ва пайванди даъвати якдафъаина бояд танзим шаванд.",
                    color = Muted, lineHeight = 20.sp)
                OutlinedTextField(value = phone, onValueChange = { phone = it.take(20) },
                    label = { Text("Рақами телефон (+992…)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    val normalized = phone.replace(" ", "").replace("-", "")
                    if (!Regex("^\\+[1-9][0-9]{7,14}$").matches(normalized)) {
                        status = "Рақамро бо рамзи кишвар ворид кунед, мисол: +992900000000"
                    } else {
                        val msg = "Салом! Агар хоҳед, макони худро ихтиёран мубодила кунед. Ин даъват ҳоло танҳо намунавӣ аст; сервер танзим нашудааст."
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, msg)
                        }, "Фиристодани даъват"))
                        status = "Даъват фиристода шуд танҳо баъди интихоби шумо."
                    }
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text("Сохтан ва мубодила кардани даъват")
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Иҷозат ва мубодила", fontWeight = FontWeight.Bold)
                Text("Мубодила фаъол нест: ин сохтмон пайгирии пасзамина ё сервери ҷойгиршавиро дар бар намегирад. Барои сессияҳои зинда аввал backend-и ҳуҷҷатшуда ва саҳифаи розигии қабулкунанда бояд ҷойгир карда шавад.",
                    color = Muted, lineHeight = 21.sp)
                Text("• Ҳеҷ координатаи сохта намоиш дода намешавад.", color = Gold)
                Text("• Макони дигаронро аз рақами телефон ёфтан мумкин нест.", color = Gold)
                Text("• Барои бекор кардани иҷозат: Танзимоти Android → Барномаҳо → Phone Location → Иҷозатҳо.", color = Muted)
            }
        }
        Text("Phone Location — 2GIS • Версия 1.0.0", color = Muted, fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
