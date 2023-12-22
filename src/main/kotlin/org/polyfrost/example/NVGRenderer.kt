package org.polyfrost.example

import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoSVG
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL3.nvgCreate
import org.lwjgl.stb.STBImage
import org.lwjgl.stb.STBImageResize
import org.lwjgl.system.MemoryUtil
import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.property.Settings
import org.polyfrost.polyui.renderer.Renderer
import org.polyfrost.polyui.renderer.data.Font
import org.polyfrost.polyui.renderer.data.Framebuffer
import org.polyfrost.polyui.renderer.data.PolyImage
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.toByteBuffer
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.max
import org.polyfrost.polyui.color.PolyColor as Color

class NVGRenderer(size: Vec2) : Renderer(size) {
    private val nvgPaint: NVGPaint = NVGPaint.malloc()
    private val nvgColor: NVGColor = NVGColor.malloc()
    private val nvgColor2: NVGColor = NVGColor.malloc()
    private val images = IdentityHashMap<PolyImage, Int>()
    private val fonts = IdentityHashMap<Font, Int>()
    private var vg: Long = -1

    override fun init() {
        vg = nvgCreate(if (settings.useAntialiasing) NVG_ANTIALIAS else 0)
        require(vg != -1L) { "Could not initialize NanoVG" }
    }

    override fun beginFrame() {
        nvgBeginFrame(vg, size.x, size.y, pixelRatio)
    }

    override fun endFrame() = nvgEndFrame(vg)

    override fun gblAlpha(alpha: Float) = nvgGlobalAlpha(vg, alpha)

    override fun translate(x: Float, y: Float) = nvgTranslate(vg, x, y)

    override fun scale(x: Float, y: Float) = nvgScale(vg, x, y)

    override fun rotate(angleRadians: Double) = nvgRotate(vg, angleRadians.toFloat())

    override fun skewX(angleRadians: Double) = nvgSkewX(vg, angleRadians.toFloat())

    override fun skewY(angleRadians: Double) = nvgSkewY(vg, angleRadians.toFloat())

    override fun push() = nvgSave(vg)

    override fun pop() = nvgRestore(vg)

    override fun pushScissor(x: Float, y: Float, width: Float, height: Float) = nvgScissor(vg, x, y, width, height)

    override fun pushScissorIntersecting(x: Float, y: Float, width: Float, height: Float) = nvgIntersectScissor(vg, x, y, width, height)

    override fun popScissor() = nvgResetScissor(vg)

    override fun drawFramebuffer(fbo: Framebuffer, x: Float, y: Float, width: Float, height: Float) {
        throw NotImplementedError()
    }

    override fun supportsFramebuffers() = false

    override fun text(
        font: Font,
        x: Float,
        y: Float,
        text: String,
        color: Color,
        fontSize: Float,
    ) {
        if (color === Color.TRANSPARENT) return
        nvgBeginPath(vg)
        nvgFontSize(vg, fontSize)
        nvgFontFaceId(vg, getFont(font))
        nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgText(vg, x, y, text)
    }

    override fun image(
        image: PolyImage,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        colorMask: Int,
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomLeftRadius: Float,
        bottomRightRadius: Float,
    ) {
        val img = getImage(image)
        nvgImagePattern(vg, x, y, width, height, 0f, img, 1f, nvgPaint)
        if (colorMask != 0) {
            nvgRGBA(
                (colorMask shr 16 and 0xFF).toByte(),
                (colorMask shr 8 and 0xFF).toByte(),
                (colorMask and 0xFF).toByte(),
                (colorMask shr 24 and 0xFF).toByte(),
                nvgPaint.innerColor(),
            )
        }
        nvgBeginPath(vg)
        nvgRoundedRectVarying(
            vg,
            x,
            y,
            width,
            height,
            topLeftRadius,
            topRightRadius,
            bottomRightRadius,
            bottomLeftRadius,
        )
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    override fun createFramebuffer(width: Float, height: Float): Framebuffer {
        throw NotImplementedError()
    }

    override fun delete(fbo: Framebuffer?) {
        throw NotImplementedError()
    }

    override fun delete(font: Font?) {
        fonts.remove(font)
    }

    override fun delete(image: PolyImage?) {
        images.remove(image)
    }

    override fun bindFramebuffer(fbo: Framebuffer?) {
        throw NotImplementedError()
    }

    override fun unbindFramebuffer(fbo: Framebuffer?) {
        throw NotImplementedError()
    }

    override fun initImage(image: PolyImage) {
        getImage(image)
    }

    override fun rect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomLeftRadius: Float,
        bottomRightRadius: Float,
    ) {
        if (color === Color.TRANSPARENT) return
        // note: nvg checks params and draws classic rect if 0, so we don't need to
        nvgBeginPath(vg)
        nvgRoundedRectVarying(
            vg,
            x,
            y,
            width,
            height,
            topLeftRadius,
            topRightRadius,
            bottomRightRadius,
            bottomLeftRadius,
        )
        if (color(color, x, y, width, height)) {
            nvgFillPaint(vg, nvgPaint)
        } else {
            nvgFillColor(vg, nvgColor)
        }
        nvgFill(vg)
    }

    override fun hollowRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        lineWidth: Float,
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomLeftRadius: Float,
        bottomRightRadius: Float,
    ) {
        if (color === Color.TRANSPARENT) return
        nvgBeginPath(vg)
        nvgRoundedRectVarying(
            vg,
            x,
            y,
            width,
            height,
            topLeftRadius,
            topRightRadius,
            bottomRightRadius,
            bottomLeftRadius,
        )
        nvgStrokeWidth(vg, lineWidth)
        if (color(color, x, y, width, height)) {
            nvgStrokePaint(vg, nvgPaint)
        } else {
            nvgStrokeColor(vg, nvgColor)
        }
        nvgStroke(vg)
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Color, width: Float) {
        if (color === Color.TRANSPARENT) return
        nvgBeginPath(vg)
        nvgMoveTo(vg, x1, y1)
        nvgLineTo(vg, x2, y2)
        nvgStrokeWidth(vg, width)
        if (color(color, x1, y1, x2, y2)) {
            nvgStrokePaint(vg, nvgPaint)
        } else {
            nvgStrokeColor(vg, nvgColor)
        }
        nvgStroke(vg)
    }

    override fun dropShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        blur: Float,
        spread: Float,
        radius: Float,
    ) {
        nvgBoxGradient(vg, x - spread, y - spread, width + spread * 2f, height + spread * 2f, radius + spread, blur, nvgColor, nvgColor2, nvgPaint)
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x - spread, y - spread - blur, width + spread * 2f + blur * 2f, height + spread * 2f + blur * 2f, radius + spread)
        nvgRoundedRect(vg, x, y, width, height, radius)
        nvgPathWinding(vg, NVG_HOLE)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    @Suppress("NAME_SHADOWING")
    override fun textBounds(font: Font, text: String, fontSize: Float): Vec2 {
        // nanovg trims single whitespace, so add an extra one (lol)
        var text = text
        if (text.endsWith(' ')) {
            text += ' '
        }
        val out = FloatArray(4)
        nvgFontFaceId(vg, getFont(font))
        nvgTextAlign(vg, NVG_ALIGN_TOP or NVG_ALIGN_LEFT)
        nvgFontSize(vg, fontSize)
        nvgTextBounds(vg, 0f, 0f, text, out)
        val w = out[2] - out[0]
        val h = out[3] - out[1]
        return Vec2(w, h)
    }

    private fun color(color: Color) {
        if (color is Color.Gradient) {
            nvgRGBA(color.r.toByte(), color.g.toByte(), color.b.toByte(), color.a.toByte(), nvgColor2)
            nvgRGBA(
                color.color1.r.toByte(),
                color.color1.g.toByte(),
                color.color1.b.toByte(),
                color.color1.a.toByte(),
                nvgColor,
            )
        } else {
            nvgRGBA(color.r.toByte(), color.g.toByte(), color.b.toByte(), color.a.toByte(), nvgColor)
        }
    }

    private fun color(
        color: Color,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ): Boolean {
        color(color)
        if (color !is Color.Gradient) return false
        when (color.type) {
            is Color.Gradient.Type.TopToBottom -> nvgLinearGradient(
                vg,
                x,
                y,
                x,
                y + height,
                nvgColor,
                nvgColor2,
                nvgPaint,
            )

            is Color.Gradient.Type.TopLeftToBottomRight -> nvgLinearGradient(
                vg,
                x,
                y,
                x + width,
                y + height,
                nvgColor,
                nvgColor2,
                nvgPaint,
            )

            is Color.Gradient.Type.LeftToRight -> nvgLinearGradient(
                vg,
                x,
                y,
                x + width,
                y,
                nvgColor,
                nvgColor2,
                nvgPaint,
            )

            is Color.Gradient.Type.BottomLeftToTopRight -> nvgLinearGradient(
                vg,
                x,
                y + height,
                x + width,
                y,
                nvgColor,
                nvgColor2,
                nvgPaint,
            )

            is Color.Gradient.Type.Radial -> {
                val t = color.type as Color.Gradient.Type.Radial
                nvgRadialGradient(
                    vg,
                    if (t.centerX == -1f) x + (width / 2f) else t.centerX,
                    if (t.centerY == -1f) y + (height / 2f) else t.centerY,
                    t.innerRadius,
                    t.outerRadius,
                    nvgColor,
                    nvgColor2,
                    nvgPaint,
                )
            }

            is Color.Gradient.Type.Box -> nvgBoxGradient(
                vg,
                x,
                y,
                width,
                height,
                (color.type as Color.Gradient.Type.Box).radius,
                (color.type as Color.Gradient.Type.Box).feather,
                nvgColor,
                nvgColor2,
                nvgPaint,
            )
        }
        return true
    }

    private fun getFont(font: Font): Int {
        return fonts.getOrPut(font) {
            val data = fontData.getOrPut(font) {
                font.stream?.toByteBuffer() ?: if (settings.resourcePolicy == Settings.ResourcePolicy.WARN) {
                    PolyUI.LOGGER.warn(
                        "Failed to get font: {}, falling back to default font!",
                        font.resourcePath,
                    )
                    PolyUI.defaultFonts.regular.get().toByteBuffer()
                } else {
                    throw ExceptionInInitializerError("Failed to get font: ${font.resourcePath}")
                }
            }
            nvgCreateFontMem(vg, font.name, data, 0)
        }
    }

    private fun getImage(image: PolyImage): Int {
        return images.getOrPut(image) {
            val data = imageData.getOrPut(image) {
                val stream = image.stream ?: if (settings.resourcePolicy == Settings.ResourcePolicy.WARN) {
                    PolyUI.LOGGER.warn(
                        "Failed to get image: {}, falling back to default image!",
                        image.resourcePath,
                    )
                    PolyUI.defaultImage.stream ?: throw IllegalStateException("Default image not found!")
                } else {
                    throw ExceptionInInitializerError("Failed to get image: ${image.resourcePath}")
                }
                val data: ByteBuffer
                when (image.type) {
                    // let stb figure it out
                    PolyImage.Type.Unknown, PolyImage.Type.Raster -> {
                        val w = IntArray(1)
                        val h = IntArray(1)
                        data = STBImage.stbi_load_from_memory(
                            stream.toByteBuffer(),
                            w,
                            h,
                            IntArray(1),
                            4,
                        ).also {
                            if (it == null) {
                                PolyUI.LOGGER.error("STB error: ${STBImage.stbi_failure_reason()}")
                                throw Exception("Failed to initialize $image")
                            }
                            if (image.width == -1f || image.height == -1f) {
                                val sh = image.height != -1f
                                val sw = image.width != -1f
                                if (!sw) {
                                    if (!sh) {
                                        image.width = w[0].toFloat()
                                        image.height = h[0].toFloat()
                                        return@also
                                    } else {
                                        // !sw, sh
                                        val ratio = image.height / h[0].toFloat()
                                        image.width = w[0].toFloat() * ratio
                                    }
                                } else {
                                    // !sh, sw
                                    val ratio = image.width / w[0].toFloat()
                                    image.height = h[0].toFloat() * ratio
                                }
                            }
                            PolyUI.LOGGER.info("resizing image ${image.resourcePath}: ${w[0]}x${h[0]} -> ${image.width}x${image.height}")
                            STBImageResize.stbir_resize_uint8(
                                it, w[0], h[0],
                                0, it,
                                image.width.toInt(), image.height.toInt(),
                                0, 4,
                            )
                        } ?: throw Exception("Failed to initialize $image")
                    }

                    PolyImage.Type.Vector -> {
                        val d = InputStreamReader(stream).use {
                            val t = it.readText()
                            it.close()
                            t
                        } as CharSequence
                        val svg = NanoSVG.nsvgParse(d, "px", 96f) ?: throw Exception("Failed to open SVG: $image (invalid data?)")
                        val raster = NanoSVG.nsvgCreateRasterizer()
                        val scale = if (image.width != -1f || image.height != -1f) {
                            max(image.width / svg.width(), image.height / svg.height())
                        } else {
                            1f
                        }
                        image.width = (svg.width() * scale).toInt().toFloat()
                        image.height = (svg.height() * scale).toInt().toFloat()
                        data = MemoryUtil.memAlloc(image.width.toInt() * image.height.toInt() * 4)
                        NanoSVG.nsvgRasterize(
                            raster, svg,
                            0f, 0f,
                            scale, data,
                            image.width.toInt(), image.height.toInt(),
                            image.width.toInt() * 4,
                        )
                        NanoSVG.nsvgDeleteRasterizer(raster)
                        NanoSVG.nsvgDelete(svg)
                    }
                }
                data
            }
            nvgCreateImageRGBA(vg, image.width.toInt(), image.height.toInt(), 0, data)
        }
    }

    override fun cleanup() {
        images.clear()
        fonts.clear()
        nvgColor.free()
        nvgPaint.free()
        vg = -1
    }

    companion object {
        /**
         * permanent cache of images for speed + stop reading files lots
         */
        @JvmStatic
        private val imageData = HashMap<PolyImage, ByteBuffer>()

        /**
         * permanent cache of fonts for speed + stop reading lots of files
         */
        @JvmStatic
        private val fontData = HashMap<Font, ByteBuffer>()
    }
}