package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.LiteralText;

// Made by Cookie

public class AntiAntiAFK extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public AntiAntiAFK() {
        super(HAHAddon.COOKIE, "Anti Anti AFK", "Automatically disconnects you after a certain time.");
    }

    private final Setting<Integer> time = sgGeneral.add(new IntSetting.Builder()
        .name("AFK Time in minutes")
        .description("Time that has to pass until you get disconnected (If you want a bigger number, type it inside).")
        .defaultValue(20)
        .min(1)
        .sliderMax(120)
        .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-off")
        .description("Disables Anti Anti AFK after usage.")
        .defaultValue(false)
        .build()
    );

    private int timer;

    @EventHandler
    public void onGameJoined(GameJoinedEvent event) {
        timer = 0;
    }

    @EventHandler
    public void onKeyPress(KeyEvent event) {
        info("key : " + event.key + " mod : " + event.modifiers + " action : " + event.action);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        
        if (timer < time.get() * 20 * 60){
            timer++;			
            return;	
        } else {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("[Anti Anti AFK] " + time.get() + " minutes have passed.")));
            if (toggleOff.get()) this.toggle();
            timer = 0;
        }	
    }
}