package org.polyfrost.example

import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW.*
import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.renderer.Window
import org.polyfrost.polyui.renderer.data.Cursor

class MCWindow(private val mc: MinecraftClient) : Window(
    mc.framebuffer.viewportWidth, mc.framebuffer.viewportHeight
) {
    private val handle = mc.window.handle

    override fun close() {
        mc.setScreen(null)
    }

    override fun createCallbacks() {}

    override fun getClipboard(): String? {
        return glfwGetClipboardString(handle)
    }

    override fun getKeyName(key: Int): String {
        return glfwGetKeyName(key, 0) ?: "Unknown"
    }

    override fun open(polyUI: PolyUI): Window {
        throw UnsupportedOperationException("cannot be opened in this way")
    }

    override fun setClipboard(text: String?) {
        glfwSetClipboardString(handle, text ?: "")
    }

    override fun setCursor(cursor: Cursor) {
        glfwSetCursor(
            handle,
            glfwCreateStandardCursor(
                when (cursor) {
                    Cursor.Pointer -> GLFW_ARROW_CURSOR
                    Cursor.Clicker -> GLFW_HAND_CURSOR
                    Cursor.Text -> GLFW_IBEAM_CURSOR
                }
            )
        )
    }

    override fun supportsRenderPausing() = false
}