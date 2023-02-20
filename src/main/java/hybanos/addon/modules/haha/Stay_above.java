package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;

// TODO : work in more screens

public class Stay_above extends Module {

    private final SettingGroup sgJump = settings.createGroup("Jump");
    private final SettingGroup sgDisconnect = settings.createGroup("Disconnect");

    // Jump
    private final Setting<Boolean> jump_active = sgJump.add(new BoolSetting.Builder()
        .name("jump")
        .description("Enables jump mode")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> lower = sgJump.add(new IntSetting.Builder()
        .name("lower bound")
        .description("The height to press the spacebar")
        .defaultValue(-30)
        .sliderMin(-128)
        .sliderMax(320)
        .visible(() -> jump_active.get())
        .build()
    );

    private final Setting<Integer> upper = sgJump.add(new IntSetting.Builder()
        .name("upper bound")
        .description("The height to release the spacebar")
        .defaultValue(-10)
        .sliderMin(-128)
        .sliderMax(320)
        .visible(() -> jump_active.get())
        .build()
    );

    private final Setting<Boolean> anti_kick = sgJump.add(new BoolSetting.Builder()
        .name("anti kick")
        .description("releases space for 1 tick to bypass kick")
        .defaultValue(false)
        .visible(() -> jump_active.get())
        .build()
    );

    private final Setting<Integer> trigger = sgJump.add(new IntSetting.Builder()
    .name("ticks")
    .description("Numbers of ticks between releases")
    .defaultValue(40)
    .sliderMin(5)
    .sliderMax(100)
    .visible(() -> jump_active.get() && anti_kick.get())
    .build()
    );

    //Disconnect
    private final Setting<Boolean> disconnect_active = sgDisconnect.add(new BoolSetting.Builder()
        .name("disconnect")
        .description("Enables disconnect mode")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> disconnect_height = sgDisconnect.add(new IntSetting.Builder()
        .name("height")
        .description("The Y value to log you out")
        .defaultValue(-30)
        .sliderMin(-128)
        .sliderMax( 128)
        .visible(() -> disconnect_active.get())
        .build()
    );

    private final Setting<Boolean> disable = sgDisconnect.add(new BoolSetting.Builder()
        .name("disable")
        .description("Disables this module after triggered")
        .visible(() -> disconnect_active.get())
        .build()
    );

    private boolean is_pressed = false;
    private int count = 0;

    public Stay_above() {
        super(HAHAddon.CATEGORY, "Stay Above", "Prevents you from getting under a certain height.");
    }

    @Override
    public void onDeactivate() {
        mc.options.jumpKey.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (disconnect_active.get() && mc.player.getY() < disconnect_height.get()) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Stay above] Height was lower than " + disconnect_height.get() + ".")));
            if (disable.get()) this.toggle();
        }
        if ( mc.player.isOnGround()) return;
        if (jump_active.get()) {
            if (lower.get() > upper.get()) return;

            if (mc.player.getY() < lower.get()) is_pressed = true;
            if (mc.player.getY() > upper.get()) is_pressed = false;

            if (Input.isPressed(mc.options.jumpKey)) is_pressed = true;

            if (is_pressed && !(count % trigger.get() == 0)) mc.options.jumpKey.setPressed(true);
            else {
                mc.options.jumpKey.setPressed(false);
                count = 0;
            }
            count++;
        }
    }
}
