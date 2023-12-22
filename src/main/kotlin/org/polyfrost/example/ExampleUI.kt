@file:JvmName("ExampleUI")

package org.polyfrost.example

import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.component.impl.Block
import org.polyfrost.polyui.component.impl.Text

fun create() = arrayOf(
    Block(
        children = arrayOf(
            Text("hello, world!", font = PolyUI.defaultFonts.medium, fontSize = 24f),
        )
    )
)
