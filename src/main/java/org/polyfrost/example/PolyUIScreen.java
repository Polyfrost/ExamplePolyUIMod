package org.polyfrost.example;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.polyfrost.polyui.PolyUI;
import org.polyfrost.polyui.component.Drawable;
import org.polyfrost.polyui.unit.Vec2;

public class PolyUIScreen extends Screen {
    private final Drawable[] drawables;
    private PolyUI polyUI;

    public PolyUIScreen(Drawable[] drawables) {
        super(Text.literal("PolyUIScreen"));
        this.drawables = drawables;
    }

    @Override
    @SuppressWarnings("DataFlowIssue" /* reason = "client is not-null at this point */)
    protected void init() {
        polyUI = new PolyUI(new NVGRenderer(new Vec2(client.getFramebuffer().viewportWidth, client.getFramebuffer().viewportHeight)), drawables);
        polyUI.setWindow(new MCWindow(client));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // todo: there may be glitches caused by you needing to push/pop some GL state like this
        RenderSystem.disableCull();

        polyUI.render();

        RenderSystem.enableCull();
    }

    @Override
    @SuppressWarnings("DataFlowIssue" /* reason = "client is not-null at this point */)
    public final void resize(MinecraftClient m, int width, int height) {
        polyUI.resize(client.getFramebuffer().viewportWidth, client.getFramebuffer().viewportHeight, 1f, false);
    }

    @Override
    @SuppressWarnings("DataFlowIssue" /* reason = "client is not-null at this point */)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && shouldCloseOnEsc()) {
            client.setScreen(null);
            return true;
        }
        KeyRemapper.translateKey(polyUI.getEventManager(), keyCode, (char) 0, true);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        KeyRemapper.translateKey(polyUI.getEventManager(), keyCode, (char) 0, false);
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        KeyRemapper.translateKey(polyUI.getEventManager(), 0, chr, true);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        polyUI.getEventManager().mousePressed(button);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        polyUI.getEventManager().mouseReleased(button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        polyUI.getEventManager().mouseScrolled(0f, (float) amount);
        return true;
    }

}
