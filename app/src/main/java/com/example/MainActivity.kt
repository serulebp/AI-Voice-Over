package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Presets
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                Scaffold(
                    topBar = {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "AI Voice Ad Maker",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Buat naskah & voice-over iklan otomatis dari data produk singkat.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            )
                        }
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .background(Color(0xFF0A0F0E))
                                .navigationBarsPadding()
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Studio (Active)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.clickable { /* Active tab */ }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Studio",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Studio",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Info (Inactive)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .alpha(0.4f)
                                        .clickable {
                                            Toast.makeText(context, "AI Voice Ad Maker - v1.0", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Info",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                // Settings (Inactive)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .alpha(0.4f)
                                        .clickable {
                                            Toast.makeText(context, "Pengaturan Tambahan", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Settings",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AdMakerScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AdMakerScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Observe ViewModel state flows
    val productName by viewModel.productName.collectAsState()
    val productDescription by viewModel.productDescription.collectAsState()
    val mainBenefit by viewModel.mainBenefit.collectAsState()
    val targetAudience by viewModel.targetAudience.collectAsState()
    val callToAction by viewModel.callToAction.collectAsState()

    val selectedDurationState by viewModel.selectedDuration.collectAsState()
    val selectedVoiceStyleState by viewModel.selectedVoiceStyle.collectAsState()

    val speechRateState by viewModel.speechRate.collectAsState()
    val bgmEnabledState by viewModel.bgmEnabled.collectAsState()
    val bgmVolumeState by viewModel.bgmVolume.collectAsState()
    val voiceVolumeState by viewModel.voiceVolume.collectAsState()

    val isGeneratingScript by viewModel.isGeneratingScript.collectAsState()
    val isGeneratingVoice by viewModel.isGeneratingVoice.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    val statusMessage by viewModel.statusMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val editedScript by viewModel.editedScript.collectAsState()
    val isScriptEditingActive by viewModel.isScriptEditingActive.collectAsState()
    val isAudioReady by viewModel.isAudioReady.collectAsState()
    val isRecordingUsed by viewModel.isRecordingUsed.collectAsState()

    val waveformPeaks by viewModel.waveformPeaks.collectAsState()

    var showAdvancedSettings by remember { mutableStateOf(false) }

    // Dynamic Permission Requests for Mic Recording
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startMicRecording()
        } else {
            Toast.makeText(context, "Izin perekaman suara ditolak. Tidak dapat merekam.", Toast.LENGTH_SHORT).show()
        }
    }

    // Dynamic Status/Error Toast trigger
    LaunchedEffect(statusMessage, errorMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
        errorMessage?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
    }

    // Uniform custom colors for text fields
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.LightGray,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("main_scrollable_container"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // BAGIAN 1: ISI DATA PRODUK
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. ISI DATA PRODUK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cukup isi 3 data utama",
                        fontSize = 10.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                OutlinedTextField(
                    value = productName,
                    onValueChange = { viewModel.setProductName(it) },
                    label = { Text("Nama Produk *", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Kopi Seru", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = productDescription,
                    onValueChange = { viewModel.setProductDescription(it) },
                    label = { Text("Produk Ini Tentang Apa? *", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Kopi susu instan siap minum dari biji kopi arabika pilihan.", fontSize = 12.sp) },
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_desc_input"),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = mainBenefit,
                    onValueChange = { viewModel.setMainBenefit(it) },
                    label = { Text("Manfaat Utama *", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Memberikan energi instan dengan rasa kopi kafe mahal.", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_benefit_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = targetAudience,
                    onValueChange = { viewModel.setTargetAudience(it) },
                    label = { Text("Target Penonton (Opsional)", fontSize = 11.sp) },
                    placeholder = { Text("Default: orang awam dan calon pengguna umum", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_audience_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = callToAction,
                    onValueChange = { viewModel.setCallToAction(it) },
                    label = { Text("Ajakan Akhir / CTA (Opsional)", fontSize = 11.sp) },
                    placeholder = { Text("Default: Coba sekarang", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cta_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = textFieldColors
                )
            }
        }

        // BAGIAN 2: PILIH DURASI & GAYA SUARA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "2. PILIH DURASI & GAYA SUARA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Durasi Selection Card
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DURASI IKLAN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Presets.Durations.forEach { duration ->
                            val isSelected = selectedDurationState.id == duration.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { viewModel.selectedDuration.value = duration }
                                    .padding(vertical = 10.dp)
                                    .testTag("duration_${duration.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = duration.id.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color(0xFF040707) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = selectedDurationState.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Gaya Suara Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GAYA SUARA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Presets.VoiceStyles.forEach { style ->
                            val isSelected = selectedVoiceStyleState.id == style.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectedVoiceStyle.value = style }
                                    .padding(vertical = 10.dp)
                                    .testTag("voice_style_${style.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style.label.replace(" Natural", "").replace(" Semangat", "").replace(" Tenang", ""),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Karakter: ${selectedVoiceStyleState.character}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = selectedVoiceStyleState.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // COLLAPSIBLE PENGATURAN LANJUTAN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdvancedSettings = !showAdvancedSettings },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "PENGATURAN LANJUTAN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = if (showAdvancedSettings) "Tutup" else "Buka",
                                modifier = Modifier
                                    .rotate(if (showAdvancedSettings) 180f else 0f)
                                    .size(18.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = showAdvancedSettings,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Kecepatan Bicara Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Kecepatan Bicara", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "${String.format("%.2f", speechRateState)}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Slider(
                                        value = speechRateState,
                                        onValueChange = { viewModel.speechRate.value = it },
                                        valueRange = 0.6f..1.4f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.testTag("speech_rate_slider")
                                    )
                                }

                                // BGM Switch Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Musik Pendukung (BGM)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = "Menambahkan instrumen harmoni di latar belakang", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = bgmEnabledState,
                                        onCheckedChange = { viewModel.bgmEnabled.value = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.testTag("bgm_switch")
                                    )
                                }

                                // BGM Volume Slider
                                if (bgmEnabledState) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Volume Musik", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "${(bgmVolumeState * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Slider(
                                            value = bgmVolumeState,
                                            onValueChange = { viewModel.bgmVolume.value = it },
                                            valueRange = 0.0f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.testTag("bgm_volume_slider")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // BAGIAN 3: NASKAH DAN VOICE-OVER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. NASKAH & VOICE-OVER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Button(
                        onClick = { viewModel.handleGenerateAdScript() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("generate_script_button"),
                        enabled = !isGeneratingScript
                    ) {
                        if (isGeneratingScript) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Buat Naskah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // EDITOR NASKAH
                AnimatedVisibility(
                    visible = isScriptEditingActive,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFF0A0F0E).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Naskah Iklan Anda",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Bisa Diedit Manual",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }

                                OutlinedTextField(
                                    value = editedScript,
                                    onValueChange = { viewModel.updateEditedScript(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("script_editor_input"),
                                    minLines = 4,
                                    maxLines = 8,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = Color(0xFFCBD5E1),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }

                // HASIL SUARA & STUDIO PREVIEW
                AnimatedVisibility(
                    visible = isAudioReady || isRecordingUsed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F0E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hasil Suara",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Left
                            )

                            // WAVEFORM VISUALIZER CANVAS
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFF111D1A),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isRecording) "SEDANG MEREKAM..." else "PREVIEW AUDIO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    waveformPeaks.forEach { peak ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(peak)
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        listOf(
                                                            if (isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                                                            if (isRecording) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.tertiary
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                }
                            }

                            // PLAYBACK AND STUDIO CONTROL BUTTONS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // PLAY/STOP BUTTON (Circular Emerald Button)
                                FilledIconButton(
                                    onClick = {
                                        if (isPlaying) viewModel.stopAd() else viewModel.playAd()
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color(0xFF040707)
                                    ),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("play_button")
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = "Putar Voice-Over",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // GENERATE/REGENERATE VOICE-OVER (Main Flexible Action Button)
                                Button(
                                    onClick = { viewModel.handleGenerateVoiceover() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("generate_voice_button"),
                                    enabled = !isGeneratingVoice
                                ) {
                                    if (isGeneratingVoice) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = if (isAudioReady) "BUAT ULANG" else "BUAT VOICE-OVER",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                // DOWNLOAD WAV BUTTON (Sleek Dark Rounded Square Button)
                                FilledIconButton(
                                    onClick = { viewModel.downloadWavFile() },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color(0xFF162523),
                                        contentColor = Color(0xFFCBD5E1)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("download_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Download WAV",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // RESET STUDIO BUTTON (Sleek Dark Rounded Square Button)
                                FilledIconButton(
                                    onClick = { viewModel.resetAd() },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color(0xFF162523),
                                        contentColor = Color(0xFFCBD5E1)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("reset_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset Studio",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // REKAM SUARA SENDIRI (MIC PRACTICE)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "REKAM SUARA SENDIRI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Latih atau rekam sendiri dengan naskah di atas.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Button(
                            onClick = {
                                if (isRecording) {
                                    viewModel.stopMicRecording()
                                } else {
                                    // Request mic recording permission dynamically
                                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        viewModel.startMicRecording()
                                    } else {
                                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("record_mic_button")
                        ) {
                            Text(
                                text = if (isRecording) "Selesai" else "Mulai Rekam",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRecording) Color.White else Color(0xFF040707)
                            )
                        }
                    }
                }
            }
        }
    }
}
