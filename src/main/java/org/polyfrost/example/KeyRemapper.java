package org.polyfrost.example;

import org.jetbrains.annotations.Unmodifiable;
import org.polyfrost.polyui.event.EventManager;
import org.polyfrost.polyui.input.Keys;
import org.polyfrost.polyui.input.Modifiers;

import java.util.Map;

import static java.util.Map.entry;
import static org.lwjgl.glfw.GLFW.*;

public class KeyRemapper {
    @Unmodifiable
    public static final Map<Integer, Modifiers> modsMap;

    @Unmodifiable
    public static final Map<Integer, Keys> keyMap;

    static {
        modsMap = Map.of(
                GLFW_KEY_LEFT_SHIFT, Modifiers.LSHIFT,
                GLFW_KEY_RIGHT_SHIFT, Modifiers.RSHIFT,
                GLFW_KEY_LEFT_CONTROL, Modifiers.LCONTROL,
                GLFW_KEY_RIGHT_CONTROL, Modifiers.RCONTROL,
                GLFW_KEY_LEFT_SUPER, Modifiers.LMETA,
                GLFW_KEY_RIGHT_SUPER, Modifiers.RMETA,
                GLFW_KEY_LEFT_ALT, Modifiers.LALT,
                GLFW_KEY_RIGHT_ALT, Modifiers.RALT
        );
        keyMap = Map.ofEntries(
                entry(GLFW_KEY_F1, Keys.F1),
                entry(GLFW_KEY_F2, Keys.F2),
                entry(GLFW_KEY_F3, Keys.F3),
                entry(GLFW_KEY_F4, Keys.F4),
                entry(GLFW_KEY_F5, Keys.F5),
                entry(GLFW_KEY_F6, Keys.F6),
                entry(GLFW_KEY_F7, Keys.F7),
                entry(GLFW_KEY_F8, Keys.F8),
                entry(GLFW_KEY_F9, Keys.F9),
                entry(GLFW_KEY_F10, Keys.F10),
                entry(GLFW_KEY_F11, Keys.F11),
                entry(GLFW_KEY_F12, Keys.F12),
                entry(GLFW_KEY_ESCAPE, Keys.ESCAPE),
                entry(GLFW_KEY_ENTER, Keys.ENTER),
                entry(GLFW_KEY_BACKSPACE, Keys.BACKSPACE),
                entry(GLFW_KEY_TAB, Keys.TAB),
                entry(GLFW_KEY_END, Keys.END),
                entry(GLFW_KEY_HOME, Keys.HOME),
                entry(GLFW_KEY_LEFT, Keys.LEFT),
                entry(GLFW_KEY_UP, Keys.UP),
                entry(GLFW_KEY_RIGHT, Keys.RIGHT),
                entry(GLFW_KEY_DOWN, Keys.DOWN)
        );
    }
    public static void translateKey(EventManager ev, int keyCode, char typedChar, boolean down) {
        if (typedChar != 0) {
            ev.keyTyped(typedChar);
            return;
        }

        Modifiers m = modsMap.get(keyCode);
        if (m != null) {
            if (down) ev.addModifier(m.getValue());
            else ev.removeModifier(m.getValue());
            return;
        }

        Keys k = keyMap.get(keyCode);
        if (k != null) {
            if (down) ev.keyDown(k);
            else ev.keyUp(k);
            return;
        }

        if (down) ev.keyDown(keyCode);
        else ev.keyUp(keyCode);
    }
}
