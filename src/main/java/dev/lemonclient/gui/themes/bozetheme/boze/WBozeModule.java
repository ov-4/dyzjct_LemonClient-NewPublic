package dev.lemonclient.gui.themes.bozetheme.boze;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;
import dev.lemonclient.gui.widgets.pressable.WPressable;
import dev.lemonclient.systems.modules.Module;
import net.minecraft.util.math.MathHelper;

import static dev.lemonclient.LemonClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBozeModule extends WPressable implements LCGuiWidget {
    private final Module module;

    private double titleWidth;

    private double animationProgress1;

    private double animationProgress2;

    public WBozeModule(Module module) {
        this.module = module;
        this.tooltip = module.description;

        if (module.isActive()) {
            animationProgress1 = 1;
            animationProgress2 = 1;
        } else {
            animationProgress1 = 0;
            animationProgress2 = 0;
        }
    }

    @Override
    public double pad() {
        return theme.scale(4);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        if (titleWidth == 0) titleWidth = theme.textWidth(module.title);

        width = pad + titleWidth + pad + 38;
        height = pad + theme.textHeight() + pad;
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) mc.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        int originalWidth = (int) width;
        LCGuiTheme theme = theme();
        double pad = pad();

        animationProgress1 += delta * 4 * ((module.isActive() || mouseOver) ? 1 : -1);
        animationProgress1 = MathHelper.clamp(animationProgress1, 0, 1);

        animationProgress2 += delta * 6 * (module.isActive() ? 1 : -1);
        animationProgress2 = MathHelper.clamp(animationProgress2, 0, 1);

        if (animationProgress1 > 0) {
            double progress = animationProgress2;
            if ((!module.isActive() && progress == 0) || mouseOver) {
                progress = 1.0;
            }
            renderer.quad(x, y, width * animationProgress1, height, theme.moduleBackground.get());
        }
        if (animationProgress2 > 0) {
            renderer.quadRounded(x, y + height * (1 - animationProgress2), theme.scale(2), height * animationProgress2, theme.moduleBackground.get(), 8, true);
        }

        renderer.text(module.title, x + 8, y + pad, theme.textColor.get(), false);
        renderer.quadRounded(x + originalWidth - 34, y + pad - 1.5, theme.textWidth(module.keybind.isSet() ? module.keybind.toString() : "A") + 12, theme.textHeight() + 3, theme.placeholderColor.get(), 8, true);

        if (module.keybind.isSet()) {
            renderer.text(module.keybind.toString(), x + originalWidth - 28, y + pad, theme.textColor.get(), false);
        }
    }
}
