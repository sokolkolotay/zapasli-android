package ru.zapasli.app.ui.scanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class BarcodeRecognitionTest {
    @Test
    fun bundledModelRecognizesEan13() {
        val expectedBarcode = "3017624010701"
        val bitmap = renderEan13(expectedBarcode)
        val scanner = BarcodeScanning.getClient(groceryBarcodeScannerOptions())

        try {
            val barcodes = Tasks.await(
                scanner.process(InputImage.fromBitmap(bitmap, 0)),
                10,
                TimeUnit.SECONDS,
            )

            assertTrue(barcodes.any { it.rawValue == expectedBarcode })
        } finally {
            scanner.close()
            bitmap.recycle()
        }
    }
}

private fun renderEan13(value: String): Bitmap {
    require(value.length == 13 && value.all(Char::isDigit))
    val leftPatterns = arrayOf(
        "0001101", "0011001", "0010011", "0111101", "0100011",
        "0110001", "0101111", "0111011", "0110111", "0001011",
    )
    val alternateLeftPatterns = arrayOf(
        "0100111", "0110011", "0011011", "0100001", "0011101",
        "0111001", "0000101", "0010001", "0001001", "0010111",
    )
    val rightPatterns = arrayOf(
        "1110010", "1100110", "1101100", "1000010", "1011100",
        "1001110", "1010000", "1000100", "1001000", "1110100",
    )
    val parityPatterns = arrayOf(
        "LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG",
        "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL",
    )
    val digits = value.map(Char::digitToInt)
    val modules = buildString {
        append("101")
        parityPatterns[digits.first()].forEachIndexed { index, parity ->
            append(
                if (parity == 'L') leftPatterns[digits[index + 1]]
                else alternateLeftPatterns[digits[index + 1]],
            )
        }
        append("01010")
        digits.drop(7).forEach { digit -> append(rightPatterns[digit]) }
        append("101")
    }

    val moduleWidth = 5
    val quietZoneModules = 12
    val width = (modules.length + quietZoneModules * 2) * moduleWidth
    val height = 280
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    val paint = Paint().apply { color = Color.BLACK }
    modules.forEachIndexed { index, bit ->
        if (bit == '1') {
            val left = (quietZoneModules + index) * moduleWidth
            canvas.drawRect(
                left.toFloat(),
                20f,
                (left + moduleWidth).toFloat(),
                (height - 20).toFloat(),
                paint,
            )
        }
    }
    return bitmap
}
