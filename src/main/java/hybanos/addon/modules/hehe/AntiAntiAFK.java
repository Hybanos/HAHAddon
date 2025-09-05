package hybanos.addon.modules.hehe;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

// Made by Cookie

public class AntiAntiAFK extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgHorizon = settings.createGroup("Horizon");

    public AntiAntiAFK() {
        super(HAHAddon.COOKIE, "Anti Anti AFK", "Automatically disconnects you after a certain time.");
    }

    private final Setting<Boolean> togglekick = sgGeneral.add(new BoolSetting.Builder()
            .name("Toggle Kick")
            .description("Toggle Kick for AFK.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> time = sgGeneral.add(new IntSetting.Builder()
            .name("AFK Time in minutes")
            .description("Time that has to pass until you get disconnected (If you want a bigger number, type it inside).")
            .defaultValue(20)
            .min(1)
            .sliderMax(120)
            .build()
    );

    private final Setting<Boolean> noinput = sgGeneral.add(new BoolSetting.Builder()
            .name("No Input")
            .description("Disconnects you after the set time if no input was made until then.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
            .name("Toggle off")
            .description("Disables Anti Anti AFK after usage.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> toggledm = sgHorizon.add(new BoolSetting.Builder()
            .name("Toggle DM")
            .description("Toggle dm to Ducky.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Integer> Message = sgHorizon.add(new IntSetting.Builder()
            .name("DM Ducky")
            .description("Tell Ducky that you are AFK in dm after the set time.")
            .defaultValue(5)
            .min(1)
            .sliderMax(30)
            .build()
    );

    private final Setting<Boolean> back = sgHorizon.add(new BoolSetting.Builder()
            .name("Input = back")
            .description("Tell Ducky that you are back in dm after you make any input.")
            .defaultValue(true)
            .build()
    );

    private int kickTimer = 0;
    private int afkTimer = 0;
    private boolean isAfk = false;

    @EventHandler
    public void onGameJoined(GameJoinedEvent event) {
        kickTimer = 0;
        afkTimer = 0;
    }

    @EventHandler
    public void KeyAction(KeyEvent event) {
        if (noinput.get()) {
            kickTimer = 0;
        }
        if (back.get()) {
            if (isAfk) {
                mc.player.networkHandler.sendChatCommand("msg DuckIsTheBot !afk " + mc.player.getName().getString());
            }
            afkTimer = 0;
            isAfk = false;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (kickTimer < time.get() * 20 * 60 && togglekick.get()) {
            kickTimer++;
        } else if (togglekick.get()) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Anti Anti AFK] " + time.get() + " minutes have passed.")));
            if (toggleOff.get()) this.toggle();
            kickTimer = 0;
        }
        if (afkTimer < Message.get() * 20 * 60 && toggledm.get()) {
            afkTimer++;
        } else if (!isAfk && toggledm.get()) {
            mc.player.networkHandler.sendChatCommand("msg DuckIsTheBot !afk " + mc.player.getName().getString());
            isAfk = true;
        }
    }
}
