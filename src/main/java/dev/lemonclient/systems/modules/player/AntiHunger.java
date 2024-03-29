package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IPlayerMoveC2SPacket;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {
    public AntiHunger() {
        super(Categories.Player, "Anti Hunger", "Reduces (does NOT remove) hunger consumption.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> sprint = sgGeneral.add(new BoolSetting.Builder()
        .name("sprint")
        .description("Spoofs sprinting packets.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onGround = sgGeneral.add(new BoolSetting.Builder()
        .name("on-ground")
        .description("Spoofs the onGround flag.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> waterCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("water-check")
        .description("Pauses the module if you are in water")
        .defaultValue(true)
        .build()
    );

    private boolean lastOnGround;
    private boolean sendOnGroundTruePacket;
    private boolean ignorePacket;

    @Override
    public void onActivate() {
        lastOnGround = mc.player.isOnGround();
        sendOnGroundTruePacket = true;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (ignorePacket) return;

        if (event.packet instanceof ClientCommandC2SPacket && sprint.get()) {
            ClientCommandC2SPacket.Mode mode = ((ClientCommandC2SPacket) event.packet).getMode();

            if (mode == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                event.cancel();
            }
        }

        if (event.packet instanceof PlayerMoveC2SPacket && onGround.get() && mc.player.isOnGround() && mc.player.fallDistance <= 0.0 && !mc.interactionManager.isBreakingBlock()) {
            ((IPlayerMoveC2SPacket) event.packet).setOnGround(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (waterCheck.get() && mc.player.isTouchingWater()) {
            ignorePacket = true;
            return;
        }
        if (mc.player.isOnGround() && !lastOnGround && !sendOnGroundTruePacket) sendOnGroundTruePacket = true;

        if (mc.player.isOnGround() && sendOnGroundTruePacket && onGround.get()) {
            ignorePacket = true;
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            ignorePacket = false;

            sendOnGroundTruePacket = false;
        }

        lastOnGround = mc.player.isOnGround();
    }
}
