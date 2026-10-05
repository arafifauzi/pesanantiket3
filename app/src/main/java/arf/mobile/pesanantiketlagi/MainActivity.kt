package arf.mobile.pesanantiketlagi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arf.mobile.pesanantiketlagi.ui.theme.PesanantiketlagiTheme
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

// Enum untuk mengelola status transaksi
enum class BookingStatus {
    IDLE,
    EMPTY_NAME,
    PROCESSING,
    SUCCESS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PesanantiketlagiTheme {
                TicketBookingParentScreen()
            }
        }
    }
}

// -------------------------------------------------------------
// 1. PARENT COMPOSABLE (State Hoisting)
// -------------------------------------------------------------
@Composable
fun TicketBookingParentScreen() {
    // State yang dikelola oleh Parent
    var namaPembeli by remember { mutableStateOf("") }
    var jumlahTiket by remember { mutableIntStateOf(1) }
    val hargaTiket = 25000 // Harga per tiket

    // State internal untuk alur pemrosesan & status
    var status by remember { mutableStateOf(BookingStatus.IDLE) }
    var isProcessingTriggered by remember { mutableStateOf(false) }

    // LaunchedEffect untuk efek samping async (delay 3 detik)
    LaunchedEffect(isProcessingTriggered) {
        if (isProcessingTriggered) {
            status = BookingStatus.PROCESSING
            delay(3.seconds) // Delay selama 3 detik
            status = BookingStatus.SUCCESS
            isProcessingTriggered = false
        }
    }

    // Mengirim state dan callback event ke Child
    TicketBookingContent(
        namaPembeli = namaPembeli,
        onNamaChange = { 
            namaPembeli = it 
            if (status == BookingStatus.EMPTY_NAME) {
                status = BookingStatus.IDLE
            }
        },
        jumlahTiket = jumlahTiket,
        onJumlahIncrement = { jumlahTiket++ },
        onJumlahDecrement = { if (jumlahTiket > 1) jumlahTiket-- },
        hargaTiket = hargaTiket,
        status = status,
        onPesanClick = {
            if (namaPembeli.trim().isEmpty()) {
                status = BookingStatus.EMPTY_NAME
            } else {
                isProcessingTriggered = true
            }
        },
        onResetClick = {
            namaPembeli = ""
            jumlahTiket = 1
            status = BookingStatus.IDLE
        }
    )
}

// -------------------------------------------------------------
// 2. CHILD COMPOSABLE (Stateless UI Component)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketBookingContent(
    namaPembeli: String,
    onNamaChange: (String) -> Unit,
    jumlahTiket: Int,
    onJumlahIncrement: () -> Unit,
    onJumlahDecrement: () -> Unit,
    hargaTiket: Int,
    status: BookingStatus,
    onPesanClick: () -> Unit,
    onResetClick: () -> Unit
) {
    val totalHarga = hargaTiket * jumlahTiket
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Pemesanan Tiket Konser",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section: Informasi Pemesan
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Informasi Pemesan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = namaPembeli,
                        onValueChange = onNamaChange,
                        label = { Text("Nama Lengkap") },
                        placeholder = { Text("Contoh: Budi Santoso") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Nama")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = status != BookingStatus.PROCESSING && status != BookingStatus.SUCCESS,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Section: Detail Tiket & Pembayaran
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Detail Tiket",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Harga Satuan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Harga per Tiket", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Rp${String.format(Locale.getDefault(), "%,d", hargaTiket).replace(',', '.')}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Jumlah Tiket Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Jumlah Tiket", style = MaterialTheme.typography.bodyMedium)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedIconButton(
                                onClick = onJumlahDecrement,
                                enabled = jumlahTiket > 1 && status != BookingStatus.PROCESSING && status != BookingStatus.SUCCESS
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = jumlahTiket.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.widthIn(min = 24.dp),
                            )

                            OutlinedIconButton(
                                onClick = onJumlahIncrement,
                                enabled = status != BookingStatus.PROCESSING && status != BookingStatus.SUCCESS
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider()

                    // Total Harga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Pembayaran",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rp${String.format(Locale.getDefault(), "%,d", totalHarga).replace(',', '.')}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Status Banner
            StatusBanner(status = status)

            // Aksi Button
            if (status == BookingStatus.SUCCESS) {
                Button(
                    onClick = onResetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = "Pesan Tiket Lain", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onPesanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = status != BookingStatus.PROCESSING,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (status == BookingStatus.PROCESSING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Memproses Pesanan...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "Pesan Tiket Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. STATUS BANNER COMPOSABLE
// -------------------------------------------------------------
@Composable
fun StatusBanner(status: BookingStatus) {
    when (status) {
        BookingStatus.IDLE -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Silakan isi nama dan pilih jumlah tiket untuk melanjutkan pemesanan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        BookingStatus.EMPTY_NAME -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Nama pemesan masih kosong. Mohon isi nama Anda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        BookingStatus.PROCESSING -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sedang memproses transaksi Anda...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        BookingStatus.SUCCESS -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Yeay! Pesanan tiket berhasil dipesan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
