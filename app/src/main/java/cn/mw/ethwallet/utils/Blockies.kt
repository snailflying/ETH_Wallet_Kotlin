package cn.mw.ethwallet.utils

import android.graphics.*
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:13
 * @description
 */
object Blockies {

    private val size = 8
    private val randseed = LongArray(4)

    @JvmOverloads
    fun createIcon(address: String, scale: Int = 16): Bitmap {
        seedrand(address)
        val color = createColor()
        val bgColor = createColor()
        val spotColor = createColor()

        val imgdata = createImageData()
        return createCanvas(imgdata, color, bgColor, spotColor, scale)
    }

    private fun createCanvas(imgData: DoubleArray, color: HSL, bgcolor: HSL, spotcolor: HSL, scale: Int): Bitmap {
        val width = Math.sqrt(imgData.size.toDouble()).toInt()

        val w = width * scale
        val h = width * scale

        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(w, h, conf)
        val canvas = Canvas(bmp)

        val background = toRGB(bgcolor.h.toInt().toFloat(), bgcolor.s.toInt().toFloat(), bgcolor.l.toInt().toFloat())

        var paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = background
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val main = toRGB(color.h.toInt().toFloat(), color.s.toInt().toFloat(), color.l.toInt().toFloat())
        val scolor = toRGB(spotcolor.h.toInt().toFloat(), spotcolor.s.toInt().toFloat(), spotcolor.l.toInt().toFloat())

        for (i in imgData.indices) {
            val row = Math.floor((i / width).toDouble()).toInt()
            val col = i % width
            paint = Paint()

            paint.color = if (imgData[i] == 1.0) main else scolor

            if (imgData[i] > 0.0) {
                canvas.drawRect((col * scale).toFloat(), (row * scale).toFloat(), (col * scale + scale).toFloat(), (row * scale + scale).toFloat(), paint)
            }
        }
        return getCroppedBitmap(bmp)
    }

    private fun rand(): Double {
        val t = (randseed[0] xor (randseed[0] shl 11)).toInt()
        randseed[0] = randseed[1]
        randseed[1] = randseed[2]
        randseed[2] = randseed[3]
        randseed[3] = randseed[3] xor (randseed[3] shr 19) xor t.toLong() xor (t shr 8).toLong()
        val t1 = Math.abs(randseed[3]).toDouble()

        return t1 / Integer.MAX_VALUE
    }

    private fun createColor(): HSL {
        val h = Math.floor(rand() * 360.0)
        val s = rand() * 60.0 + 40.0
        val l = (rand() + rand() + rand() + rand()) * 25.0
        return HSL(h, s, l)
    }

    private fun createImageData(): DoubleArray {
        val width = size
        val height = size

        val dataWidth = Math.ceil((width / 2).toDouble())
        val mirrorWidth = width - dataWidth

        val data = DoubleArray(size * size)
        var dataCount = 0
        for (y in 0 until height) {
            var row = DoubleArray(dataWidth.toInt())
            var x = 0
            while (x < dataWidth) {
                row[x] = Math.floor(rand() * 2.3)
                x++

            }
            var r = Arrays.copyOfRange(row, 0, mirrorWidth.toInt())
            r = reverse(r)
            row = concat(row, r)

            for (i in row.indices) {
                data[dataCount] = row[i]
                dataCount++
            }
        }

        return data
    }

    fun concat(a: DoubleArray, b: DoubleArray): DoubleArray {
        val aLen = a.size
        val bLen = b.size
        val c = DoubleArray(aLen + bLen)
        System.arraycopy(a, 0, c, 0, aLen)
        System.arraycopy(b, 0, c, aLen, bLen)
        return c
    }

    private fun reverse(data: DoubleArray): DoubleArray {
        for (i in 0 until data.size / 2) {
            val temp = data[i]
            data[i] = data[data.size - i - 1]
            data[data.size - i - 1] = temp
        }
        return data
    }

    private fun seedrand(seed: String) {
        for (i in randseed.indices) {
            randseed[i] = 0
        }
        for (i in 0 until seed.length) {
            var test = randseed[i % 4] shl 5
            if (test > Integer.MAX_VALUE shl 1 || test < Integer.MIN_VALUE shl 1)
                test = test.toInt().toLong()

            val test2 = test - randseed[i % 4]
            randseed[i % 4] = test2 + Character.codePointAt(seed, i)
        }

        for (i in randseed.indices)
            randseed[i] = randseed[i].toInt().toLong()
    }

    private fun toRGB(h: Float, s: Float, l: Float): Int {
        var h = h
        var s = s
        var l = l
        h = h % 360.0f
        h /= 360f
        s /= 100f
        l /= 100f

        var q = 0f

        if (l < 0.5)
            q = l * (1 + s)
        else
            q = l + s - s * l

        val p = 2 * l - q

        var r = Math.max(0f, HueToRGB(p, q, h + 1.0f / 3.0f))
        var g = Math.max(0f, HueToRGB(p, q, h))
        var b = Math.max(0f, HueToRGB(p, q, h - 1.0f / 3.0f))

        r = Math.min(r, 1.0f)
        g = Math.min(g, 1.0f)
        b = Math.min(b, 1.0f)

        val red = (r * 255).toInt()
        val green = (g * 255).toInt()
        val blue = (b * 255).toInt()
        return Color.rgb(red, green, blue)
    }

    private fun HueToRGB(p: Float, q: Float, h: Float): Float {
        var h = h
        if (h < 0) h += 1f
        if (h > 1) h -= 1f
        if (6 * h < 1) {
            return p + (q - p) * 6f * h
        }
        if (2 * h < 1) {
            return q
        }
        return if (3 * h < 2) {
            p + (q - p) * 6f * (2.0f / 3.0f - h)
        } else p
    }

    fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                (bitmap.width / 2).toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    internal class HSL(var h: Double, var s: Double, var l: Double) {

        override fun toString(): String {
            return "HSL [h=$h, s=$s, l=$l]"
        }

    }
}