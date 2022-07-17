package hybanos.addon.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

import java.lang.Math;

public class NoLeakPos extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLeak = settings.createGroup("Anti leak");

    private final Setting<Boolean> accurate = sgGeneral.add(new BoolSetting.Builder()
        .name("accurate")
        .description("Shows position with decimal points.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> oppositeDim = sgGeneral.add(new BoolSetting.Builder()
        .name("opposite-dimension")
        .description("Displays the coords of the opposite dimension (Nether or Overworld).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> noLeak = sgLeak.add(new BoolSetting.Builder()
        .name("active")
        .description("Hides your current position.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Keybind> showKeybind = sgLeak.add(new KeybindSetting.Builder()
        .name("show-keybind")
        .description("A key to press to reveal position.")
        .defaultValue(Keybind.none())
        .visible(() -> noLeak.get())
        .build()
    );

    private final String left1 = "Pos: ";
    private double left1Width;
    private String right1;

    private String left2;
    private double left2Width;
    private String right2;

    private double xOffset;
    private double yOffset;
    private double zOffset;

    private double count = 0;

    private double xOff = 0;
    private double zOff = 0;

    public NoLeakPos(HUD hud) {
        super(hud, "no leak coords", "One time was too many times");
    }

    @Override
    public void update(HudRenderer renderer) {
        left1Width = renderer.textWidth(left1);
        left2 = null;
        right2 = null;

        double height = renderer.textHeight();
        if (oppositeDim.get()) height = height * 2 + 2;

        if (isInEditor()) {
            right1 = "0, 0, 0";
            box.setSize(left1Width + renderer.textWidth(right1), height);
            return;
        }

        Freecam freecam = Modules.get().get(Freecam.class);

        double x, y, z;

        if (count % (400 * 2000) == 0 || xOff == 0 || zOff == 0) {
            xOff = randomOffset();
            zOff = randomOffset();
        }
        count += 1;

        if (accurate.get()) {
            x = freecam.isActive() ? mc.gameRenderer.getCamera().getPos().x : mc.player.getX();
            y = freecam.isActive() ?
                mc.gameRenderer.getCamera().getPos().y - mc.player.getEyeHeight(mc.player.getPose()) :
                mc.player.getY();
            z = freecam.isActive() ? mc.gameRenderer.getCamera().getPos().z : mc.player.getZ();

            if (noLeak.get() && !showKeybind.get().isPressed()) {
                if (mc.world.getDimension().isRespawnAnchorWorking()) {
                    x += xOff / 8;
                    z += zOff / 8;
                } else {
                    x += xOff;
                    z += zOff;
                }
            }

            right1 = String.format("%.1f %.1f %.1f", x, y, z);
        }
        else {
            x = freecam.isActive() ? mc.gameRenderer.getCamera().getBlockPos().getX() : mc.player.getBlockX();
            y = freecam.isActive() ? mc.gameRenderer.getCamera().getBlockPos().getY() : mc.player.getBlockY();
            z = freecam.isActive() ? mc.gameRenderer.getCamera().getBlockPos().getZ() : mc.player.getBlockZ();

            if (noLeak.get() && !showKeybind.get().isPressed()) {
                if (mc.world.getDimension().isRespawnAnchorWorking()) {
                    x += xOff / 8;
                    z += zOff / 8;
                } else {
                    x += xOff;
                    z += zOff;
                }
            }

            right1 = String.format("%d %d %d", (int) x, (int) y, (int) z);
        }

        if (oppositeDim.get()) {
            switch (PlayerUtils.getDimension()) {
                case Overworld -> {
                    left2 = "Nether Pos: ";
                    right2 = accurate.get() ?
                        String.format("%.1f %.1f %.1f", x / 8.0, y, z / 8.0) :
                        String.format("%d %d %d", (int) x / 8, (int) y, (int) z / 8);
                }
                case Nether -> {
                    left2 = "Overworld Pos: ";
                    right2 = accurate.get() ?
                        String.format("%.1f %.1f %.1f", x * 8.0, y, z * 8.0) :
                        String.format("%d %d %d", (int) x * 8, (int) y, (int) z * 8);
                }
            }
        }

        double width = left1Width + renderer.textWidth(right1);

        if (left2 != null) {
            left2Width = renderer.textWidth(left2);
            width = Math.max(width, left2Width + renderer.textWidth(right2));
        }

        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = box.getX();
        double y = box.getY();

        double xOffset = box.alignX(left1Width + renderer.textWidth(right1));
        double yOffset = 0;

        if (left2 != null) {
            renderer.text(left2, x, y, hud.primaryColor.get());
            renderer.text(right2, x + left2Width, y, hud.secondaryColor.get());
            yOffset = renderer.textHeight() + 2;
        }

        renderer.text(left1, x + xOffset, y + yOffset, hud.primaryColor.get());
        renderer.text(right1, x + xOffset + left1Width, y + yOffset, hud.secondaryColor.get());
    }

    @EventHandler
    public void onGameJoined(GameJoinedEvent event) {
        xOff = randomOffset();
        zOff = randomOffset();
    }

    private double randomOffset() {
        return (double)((Math.random() * 2 - 1) * 4000000 - 2000000);
    }

}
