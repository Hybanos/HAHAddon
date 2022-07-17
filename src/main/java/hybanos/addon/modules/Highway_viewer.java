package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;

public class Highway_viewer extends Module {
    private final SettingGroup sgOW = settings.createGroup("Overworld");
    private final SettingGroup sgNether = settings.createGroup("Nether");
    private final SettingGroup sgColor = settings.createGroup("Color");

    private final Setting<Boolean> overworld = sgOW.add(new BoolSetting.Builder()
        .name("overworld")
        .description("Displays a line on overworld axis.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> overY = sgOW.add(new IntSetting.Builder()
        .name("height")
        .description("Y position of the line.")
        .defaultValue(120)
        .sliderMin(-32)
        .sliderMax(319)
        .visible(() -> overworld.get())
        .build()
    );

    private final Setting<Boolean> overDiag = sgOW.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Displays lines of diagonals.")
        .defaultValue(false)
        .visible(() -> overworld.get())
        .build()
    );

    private final Setting<Boolean> nether = sgNether.add(new BoolSetting.Builder()
        .name("nether")
        .description("Displays a line on nether axis.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> netherY = sgNether.add(new IntSetting.Builder()
        .name("height")
        .description("Y position of the line.")
        .defaultValue(120)
        .sliderMin(0)
        .sliderMax(255)
        .visible(() -> nether.get())
        .build()
    );

    private final Setting<Boolean> netherDiag = sgNether.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Displays lines of diagonals.")
        .defaultValue(false)
        .visible(() -> nether.get())
        .build()
    );

    private final Setting<SettingColor> color = sgColor.add(new ColorSetting.Builder()
        .name("color")
        .description("The line's color.")
        .defaultValue(new SettingColor(73, 107, 255, 255))
        .build()
    );

    private int len = 30000000;

    public Highway_viewer() {
        super(HAHAddon.CATEGORY, "Highway viewer", "render culling fucks it up :skull:");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.options.hudHidden) return;
        switch (PlayerUtils.getDimension()) {
            case Overworld -> {
                if (overworld.get()) {
                    event.renderer.line(0, overY.get(), 0, 0, overY.get(), len, color.get());
                    event.renderer.line(0, overY.get(), 0, len, overY.get(), 0, color.get());
                    event.renderer.line(0, overY.get(), 0, 0, overY.get(), -len, color.get());
                    event.renderer.line(0, overY.get(), 0, -len, overY.get(), 0, color.get());

                    if (overDiag.get()) {
                        event.renderer.line(0, overY.get(), 0, len, overY.get(), len, color.get());
                        event.renderer.line(0, overY.get(), 0, len, overY.get(), -len, color.get());
                        event.renderer.line(0, overY.get(), 0, -len, overY.get(), len, color.get());
                        event.renderer.line(0, overY.get(), 0, -len, overY.get(), -len, color.get());
                    }
                }
            }
            case Nether -> {
                if (nether.get()) {
                    event.renderer.line(0, netherY.get(), 0, 0, netherY.get(), len, color.get());
                    event.renderer.line(0, netherY.get(), 0, len, netherY.get(), 0, color.get());
                    event.renderer.line(0, netherY.get(), 0, 0, netherY.get(), -len, color.get());
                    event.renderer.line(0, netherY.get(), 0, -len, netherY.get(), 0, color.get());

                    if (overDiag.get()) {
                        event.renderer.line(0, netherY.get(), 0, len, netherY.get(), len, color.get());
                        event.renderer.line(0, netherY.get(), 0, len, netherY.get(), -len, color.get());
                        event.renderer.line(0, netherY.get(), 0, -len, netherY.get(), len, color.get());
                        event.renderer.line(0, netherY.get(), 0, -len, netherY.get(), -len, color.get());
                    }
                }
            }
        }


    }
}
