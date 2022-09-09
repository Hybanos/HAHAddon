package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;

// todo pause on kill aura

public class RedstonePlacement extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Direc> direction = sgGeneral.add(new EnumSetting.Builder<Direc>()
        .name("Direction")
        .description("The direction to look in")
        .defaultValue(Direc.UP)
        .build()
    );

    private final Setting<Keybind> keybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("keybind")
        .description("A key to press to toggle off.")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Boolean> invert = sgGeneral.add(new BoolSetting.Builder()
        .name("Invert Keybind")
        .description("Makes the keybind toggle on instead of off.")
        .defaultValue(false)
        .build()
    );

    public RedstonePlacement() {
        super(HAHAddon.CATEGORY, "Redstone Placement", "Helps you place blocks at a certain orientation.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {

        if ((keybind.get().isPressed() && !invert.get()) || (!keybind.get().isPressed() && invert.get())) return;

        float pitch = mc.player.getPitch();
        float yaw = mc.player.getYaw();

        if (direction.get() == Direc.UP) {
            pitch = -90;
        }
        if (direction.get() == Direc.DOWN) {
            pitch = 90;
        }
        if (direction.get() == Direc.NORTH) {
            pitch = 0;
            yaw = 180;
        }
        if (direction.get() == Direc.SOUTH) {
            pitch = 0;
            yaw = 0;
        }
        if (direction.get() == Direc.EAST) {
            pitch = 0;
            yaw = -90;
        }
        if (direction.get() == Direc.WEST) {
            pitch = 0;
            yaw = 90;
        }

        // switch(direction.get()) {
        //     case UP:
        //         pitch = 90;
        //     case DOWN:
        //         pitch = -90;
        //     case NORTH:
        //         pitch = 180;
        //         yaw = 0;
        //     case SOUTH:
        //         pitch = 0;
        //         yaw = 0;
        //     case EAST:
        //         pitch = 0;
        //         yaw = -90;
        //     case WEST:
        //         pitch = 0;
        //         yaw = 90;
        // }

        // info("" + pitch + ", " + yaw + ", " + direction.get());

        Rotations.rotate(yaw, pitch, -75, null);
    }

    public enum Direc {
        UP,
        DOWN,
        EAST,
        WEST,
        NORTH,
        SOUTH
    }
}
