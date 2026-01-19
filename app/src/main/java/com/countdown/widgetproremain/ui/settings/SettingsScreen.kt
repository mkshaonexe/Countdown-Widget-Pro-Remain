package com.countdown.widgetproremain.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.countdown.widgetproremain.ui.viewmodel.BackupState
import com.countdown.widgetproremain.ui.viewmodel.CountdownViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CountdownViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupState by viewModel.backupState.collectAsState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearDataWarning by remember { mutableStateOf(false) }
    
    // Create file launcher for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = viewModel.exportToBackup()
                result.onSuccess { jsonString ->
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    showExportDialog = true
                }
            }
        }
    }
    
    // File picker launcher for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val jsonString = context.contentResolver.openInputStream(it)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            jsonString?.let { json ->
                viewModel.importFromBackup(json, clearExisting = false)
                showImportDialog = true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Management Section
            SettingsSectionHeader("Data Management")
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(
                        icon = Icons.Default.Add,
                        title = "Export All Events",
                        description = "Save your events to a JSON file",
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            exportLauncher.launch("countdown_backup_$timestamp.json")
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.MoreVert,
                        title = "Import Events",
                        description = "Restore events from a backup file",
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    )
                }
            }
            
            // About Section
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader("About")
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Version",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "1.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "Build Number: 1",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Loading indicator
            if (backupState is BackupState.Exporting || backupState is BackupState.Importing) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Export Success Dialog
    if (showExportDialog && backupState is BackupState.ExportSuccess) {
        val count = (backupState as BackupState.ExportSuccess).eventCount
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
                viewModel.resetBackupState()
            },
            title = { Text("Export Successful") },
            text = { Text("Successfully exported $count event${if (count != 1) "s" else ""}.") },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    viewModel.resetBackupState()
                }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Import Success Dialog
    if (showImportDialog && backupState is BackupState.ImportSuccess) {
        val count = (backupState as BackupState.ImportSuccess).eventCount
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                viewModel.resetBackupState()
            },
            title = { Text("Import Successful") },
            text = { Text("Successfully imported $count event${if (count != 1) "s" else ""}.") },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    viewModel.resetBackupState()
                }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error Dialog
    if (backupState is BackupState.Error) {
        val errorMessage = (backupState as BackupState.Error).message
        AlertDialog(
            onDismissRequest = { viewModel.resetBackupState() },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetBackupState() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
