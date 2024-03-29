package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.AutoReconnect;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static dev.lemonclient.LemonClient.mc;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {
    @Unique
    private ButtonWidget reconnectBtn;
    @Unique
    private double time = Modules.get().get(AutoReconnect.class).time.get() * 20;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addButtons(CallbackInfo info, GridWidget.Adder adder) {
        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);

        if (autoReconnect.lastServerConnection != null) {
            reconnectBtn = adder.add(new ButtonWidget.Builder(Text.literal(getText()), button -> tryConnecting()).build());

            adder.add(
                new ButtonWidget.Builder(Text.literal("Toggle Auto Reconnect"), button -> {
                    autoReconnect.toggle();
                    reconnectBtn.setMessage(Text.literal(getText()));
                    time = autoReconnect.time.get() * 20;
                }).build()
            );
        }
    }

    @Override
    public void tick() {
        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (!autoReconnect.isActive() || autoReconnect.lastServerConnection == null) return;

        if (time <= 0) {
            tryConnecting();
        } else {
            time--;
            if (reconnectBtn != null) reconnectBtn.setMessage(Text.literal(getText()));
        }
    }

    @Unique
    private String getText() {
        String reconnectText = "Reconnect";
        if (Modules.get().isActive(AutoReconnect.class)) reconnectText += " " + String.format("(%.1f)", time / 20);
        return reconnectText;
    }

    @Unique
    private void tryConnecting() {
        var lastServer = Modules.get().get(AutoReconnect.class).lastServerConnection;
        ConnectScreen.connect(new TitleScreen(), mc, lastServer.left(), lastServer.right(), false);
    }
}
