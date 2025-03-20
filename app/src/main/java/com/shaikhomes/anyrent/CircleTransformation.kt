package com.shaikhomes.anyrent

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest

class CircleTransformation : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return circleCrop(pool, toTransform)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("circle".toByteArray(Charset.forName("UTF-8")))
    }

    private fun circleCrop(pool: BitmapPool, source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squared = Bitmap.createBitmap(source, x, y, size, size)
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(result)
        val paint = android.graphics.Paint()
        val shader = android.graphics.BitmapShader(
            squared,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.isAntiAlias = true
        paint.shader = shader
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        return result
    }
}