package com.shaikhomes.smartdiary.ui.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

object QRGenerator {

    /**
     * Generates a QR code as a Bitmap.
     *
     * @param text The content to encode in the QR code.
     * @param width The desired width of the QR code.
     * @param height The desired height of the QR code.
     * @return A Bitmap containing the generated QR code.
     */
    fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}