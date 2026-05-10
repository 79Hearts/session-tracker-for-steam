package com.steamtimeline.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.steamtimeline.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var apiKeyInput by remember(uiState.apiKey) { mutableStateOf(uiState.apiKey) }
    var steamIdInput by remember(uiState.steamId) { mutableStateOf(uiState.steamId) }
    var showApiKey by remember { mutableStateOf(false) }

    // Reset test status if user edits either field
    LaunchedEffect(apiKeyInput, steamIdInput) {
        if (uiState.testStatus !is TestStatus.Idle) viewModel.clearTestStatus()
    }

    if (uiState.saved) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1200)
            viewModel.clearSaved()
            onBack()
        }
    }

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SteamDarkSurface,
                    titleContentColor = SteamLightGray
                )
            )
        },
        containerColor = SteamNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Monitoring toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SteamCardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Background monitoring",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SteamLightGray
                        )
                        Text(
                            if (uiState.monitoringEnabled) "Polling every ${uiState.pollIntervalSeconds}s"
                            else "Off — sessions will not be recorded",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteamLightGray.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = uiState.monitoringEnabled,
                        onCheckedChange = { viewModel.setMonitoring(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SteamGreen,
                            checkedTrackColor = SteamGreen.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            PollIntervalCard(
                selectedInterval = uiState.pollIntervalSeconds,
                onIntervalSelected = { viewModel.updatePollInterval(it) }
            )

            Text(
                "Steam API Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SteamLightGray
            )

            // Quick-link buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { openUrl("https://steamcommunity.com/dev/apikey") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Get API Key", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { openUrl("https://steamcommunity.com/my/profile") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Profile", style = MaterialTheme.typography.labelMedium)
                }
            }

            // API Key field with paste + visibility toggles
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Steam API Key") },
                placeholder = { Text("32-character hex key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showApiKey) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            clipboard.getText()?.text?.let { apiKeyInput = it.trim() }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                        }
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    }
                },
                colors = steamFieldColors()
            )

            // Steam ID field with paste
            OutlinedTextField(
                value = steamIdInput,
                onValueChange = { steamIdInput = it },
                label = { Text("Steam64 ID or profile URL") },
                placeholder = { Text("76561198… or steamcommunity.com/id/yourname") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.getText()?.text?.let { steamIdInput = it.trim() }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                    }
                },
                colors = steamFieldColors()
            )

            // Test connection button + status
            OutlinedButton(
                onClick = { viewModel.testConnection(apiKeyInput, steamIdInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = apiKeyInput.isNotBlank() && steamIdInput.isNotBlank()
                    && uiState.testStatus !is TestStatus.Testing
            ) {
                when (val s = uiState.testStatus) {
                    TestStatus.Idle -> Text("Test connection")
                    TestStatus.Testing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Testing…")
                    }
                    is TestStatus.Success -> {
                        Icon(Icons.Default.Check, null, tint = SteamGreen,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Connected as ${s.personaName}")
                    }
                    is TestStatus.Failure -> {
                        Icon(Icons.Default.Error, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.reason, maxLines = 1)
                    }
                }
            }

            Button(
                onClick = { viewModel.save(apiKeyInput, steamIdInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = apiKeyInput.isNotBlank() && steamIdInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SteamBlue)
            ) {
                Text(if (uiState.saved) "Saved!" else "Save", fontWeight = FontWeight.Bold)
            }

            HelpSection()

            AboutSection()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PollIntervalCard(
    selectedInterval: Int,
    onIntervalSelected: (Int) -> Unit
) {
    val options = listOf(30 to "30s", 60 to "60s", 120 to "120s")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Poll interval",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SteamLightGray
            )
            Text(
                "How often the app checks Steam. Faster = more responsive, more battery.",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.6f)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (seconds, label) ->
                    SegmentedButton(
                        selected = selectedInterval == seconds,
                        onClick = { onIntervalSelected(seconds) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = SteamBlue.copy(alpha = 0.2f),
                            activeContentColor = SteamBlue,
                            activeBorderColor = SteamBlue
                        )
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSection() {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("About", fontWeight = FontWeight.SemiBold, color = SteamLightGray)
            Text(
                "Session Tracker for Steam v$versionName",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.75f)
            )
            Text(
                "Not affiliated with Valve Corporation. " +
                    "Steam and the Steam logo are trademarks of Valve. This app uses Steam's " +
                    "public Web API to read your own publicly visible game activity.",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.6f)
            )
            Text(
                "All session data is stored locally on this device. No analytics, no ads, no third-party tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun steamFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SteamBlue,
    focusedLabelColor = SteamBlue,
    cursorColor = SteamBlue,
    focusedTextColor = SteamLightGray,
    unfocusedTextColor = SteamLightGray
)

@Composable
private fun HelpSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Quick guide", fontWeight = FontWeight.SemiBold, color = SteamLightGray)
            Text(
                "1. Tap Get API Key → log in → register a key → long-press the key on the page to copy → tap Paste here.",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.75f)
            )
            Text(
                "2. Tap Open Profile → copy the URL from the address bar → tap Paste in the Steam64 ID field. The app will resolve it automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.75f)
            )
            Text(
                "3. Tap Test connection to verify, then Save.",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.75f)
            )
            Text(
                "Profile must be Public (Edit Profile → Privacy → Game details: Public).",
                style = MaterialTheme.typography.bodySmall,
                color = SteamLightGray.copy(alpha = 0.6f)
            )
        }
    }
}
