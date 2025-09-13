package com.anytypeio.anytype.ui.qrcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.anytypeio.anytype.core_ui.extensions.toast
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

        val options = createScannerOptions()
        scanner = GmsBarcodeScanning.getClient(this, options)

        setContent {
            QrScannerScreen(
                onStartScan = { startScanning() },
            )
        }
    }

    private fun createScannerOptions(): GmsBarcodeScannerOptions =
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

    @Composable
    private fun QrScannerScreen(
        onStartScan: () -> Unit,
    ) {
        LaunchedEffect(Unit) {
            installScannerModule { installed ->
                if (installed) {
                    onStartScan()
                }
            }
        }
    }

    private fun installScannerModule(onComplete: (Boolean) -> Unit) {
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
                toast("Failed to install code scanner module")
                onComplete(false)
                finish()
            }
    }

    private fun startScanning() {
        if (!scanningEnabled) return
        scanningEnabled = false

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrBlank()) {
                    returnResult(rawValue)
                } else {
                    Timber.w("QR code scan failed: empty barcode")
                    scanningEnabled = true
                    finish()
                }
            }
            .addOnCanceledListener {
                Timber.d("QR code scan cancelled by user")
                toast("QR code scan cancelled")
                scanningEnabled = true
                finish()
            }
            .addOnFailureListener { e ->
                Timber.e(e, "QR code scan failed")
                toast("QR code scan failed: ${e.message}")
                scanningEnabled = true
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