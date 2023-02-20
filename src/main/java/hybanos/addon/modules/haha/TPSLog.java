package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;

public class TPSLog extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> limit = sgGeneral.add(new DoubleSetting.Builder()
        .name("limit")
        .description("The TPS value to disconnects you at.")
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> disable = sgGeneral.add(new BoolSetting.Builder()
        .name("disable")
        .description("Disables the module on disconnect.")
        .defaultValue(true)
        .build()
    );

    public TPSLog() {
        super(HAHAddon.CATEGORY, "TPS Log", "Disconnects you from the server when tps drops.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (limit.get() > TickRate.INSTANCE.getTickRate()) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[TPS Log] TPS was lower than " + limit.get() + ".")));
            if (disable.get()) this.toggle();
        }
    }
}
