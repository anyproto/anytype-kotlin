package com.anytypeio.anytype.ui.qrcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import timber.log.Timber

class QrScannerActivity : ComponentActivity() {

    private lateinit var scanner: GmsBarcodeScanner
    private var scanningEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        scanner = GmsBarcodeScanning.getClient(this, options)

        setContent {
            QrScannerScreen(
                onStartScan = { startScanning() },
                onCancel = { finish() }
            )
        }
    }

    @Composable
    private fun QrScannerScreen(
        onStartScan: () -> Unit,
        onCancel: () -> Unit
    ) {
        var isLoading by remember { mutableStateOf(false) }
        var hasModuleInstalled by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            checkAndInstallModule { installed ->
                hasModuleInstalled = installed
                if (installed) {
                    onStartScan()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading || !hasModuleInstalled) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.qr_scanner_preparing),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(R.string.qr_scanner_ready),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onStartScan) {
                    Text(stringResource(R.string.qr_scanner_scan_button))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    }

    private fun checkAndInstallModule(onComplete: (Boolean) -> Unit) {
        val moduleInstall = ModuleInstall.getClient(this)
        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(scanner)
            .build()

        moduleInstall
            .installModules(moduleInstallRequest)
            .addOnSuccessListener {
                Timber.d("Code scanner module installed successfully")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Failed to install code scanner module")
                onComplete(false)
                finish()
            }
    }

    private fun startScanning() {
        if (!scanningEnabled) return

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                if (scanningEnabled) {
                    scanningEnabled = false
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        returnResult(rawValue)
                    } else {
                        Timber.w("QR code scan failed: empty barcode")
                        finish()
                    }
                }
            }
            .addOnCanceledListener {
                Timber.d("QR code scan cancelled by user")
                finish()
            }
            .addOnFailureListener { e ->
                Timber.e(e, "QR code scan failed")
                finish()
            }
    }

    private fun returnResult(qrCode: String) {
        val resultIntent = Intent().apply {
            putExtra(SCAN_RESULT, qrCode)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val SCAN_RESULT = "SCAN_RESULT"
    }
}