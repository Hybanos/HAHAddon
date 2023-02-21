package hybanos.addon.hud;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.time.Instant;
import java.lang.Math;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class NoLeakPos extends HudElement {
    public static final HudElementInfo<NoLeakPos> INFO = new HudElementInfo<>(HAHAddon.HUD_GROUP, "No Leak Coords", "One time was too many times.", NoLeakPos::new);
    private static final Color WHITE = new Color();

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

    private final Setting<Boolean> textShadow = sgGeneral.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Renders shadow behind text.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> leftColor = sgGeneral.add(new ColorSetting.Builder()
        .name("west color")
        .description("Color of the west text.")
        .defaultValue(new SettingColor(255,255,255,255))
        .build()
    );

    private final Setting<SettingColor> rightColor = sgGeneral.add(new ColorSetting.Builder()
        .name("west color")
        .description("Color of the west text.")
        .defaultValue(new SettingColor(73,107,255, 255))
        .build()
    );

    private final Setting<Boolean> noLeak = sgLeak.add(new BoolSetting.Builder()
        .name("active")
        .description("Hides your current position.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> refresh = sgLeak.add(new IntSetting.Builder()
        .name("refresh")
        .description("The time in minutes between offset refresh.")
        .defaultValue(10)
        .sliderMin(1)
        .sliderMax(60)
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

    private int count = 0;

    private double xOff = 0;
    private double zOff = 0;
    private long unixTime;

    public NoLeakPos() {
        super(INFO);
    }

    @Override
    public void setSize(double h, double w) {
        super.setSize(h, w);
    }

    @Override
    public void tick(HudRenderer renderer) {
        left1Width = renderer.textWidth(left1);
        left2 = null;
        right2 = null;

        double height = renderer.textHeight();
        if (oppositeDim.get()) height = height * 2 + 2;

        if (isInEditor()) {
            right1 = "0, 0, 0";
            setSize(left1Width + renderer.textWidth(right1), height);
            return;
        }

        Freecam freecam = Modules.get().get(Freecam.class);

        double x, y, z;

        unixTime = Instant.now().getEpochSecond();
        if (unixTime % (60*refresh.get()) == 0 || xOff == 0 || zOff == 0) {
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
                if (mc.world.getDimension().respawnAnchorWorks()) {
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
                if (mc.world.getDimension().respawnAnchorWorks()) {
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

    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.getX();
        double y = this.getY();

        if (right1 == null) {
            right1 = "0, 0, 0";
        }

        double xOffset = this.alignX(left1Width + renderer.textWidth(right1), Alignment.Auto);
        double yOffset = 0;

        if (left2 != null) {
            renderer.text(left2, x, y, leftColor.get(), textShadow.get());
            renderer.text(right2, x + left2Width, y, rightColor.get(), textShadow.get());
            yOffset = renderer.textHeight() + 2;
        }

        renderer.text(left1, x + xOffset, y + yOffset, leftColor.get(), textShadow.get());
        renderer.text(right1, x + xOffset + left1Width, y + yOffset, rightColor.get(), textShadow.get());
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
