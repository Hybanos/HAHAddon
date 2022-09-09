package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.lang.Math;

public class More_tracers extends Module {
    private final SettingGroup sgHighway = settings.createGroup("Highways");
    private final SettingGroup sgHTracer = settings.createGroup("Highway Tracer");
    private final SettingGroup sgNav = settings.createGroup("Nav Tracer");

    // Highway viewer

    private final Setting<Boolean> overworld = sgHighway.add(new BoolSetting.Builder()
        .name("overworld")
        .description("Displays a line on overworld axis.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> overY = sgHighway.add(new IntSetting.Builder()
        .name("height")
        .description("Y position of the line.")
        .defaultValue(120)
        .sliderMin(-32)
        .sliderMax(319)
        .visible(() -> overworld.get())
        .build()
    );

    private final Setting<Boolean> overDiag = sgHighway.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Displays lines of diagonals.")
        .defaultValue(false)
        .visible(() -> overworld.get())
        .build()
    );

    private final Setting<Boolean> nether = sgHighway.add(new BoolSetting.Builder()
        .name("nether")
        .description("Displays a line on nether axis.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> netherY = sgHighway.add(new IntSetting.Builder()
        .name("height")
        .description("Y position of the line.")
        .defaultValue(120)
        .sliderMin(0)
        .sliderMax(255)
        .visible(() -> nether.get())
        .build()
    );

    private final Setting<Boolean> netherDiag = sgHighway.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Displays lines of diagonals.")
        .defaultValue(false)
        .visible(() -> nether.get())
        .build()
    );


    private final Setting<SettingColor> highwayColor = sgHighway.add(new ColorSetting.Builder()
        .name("color")
        .description("The line's color.")
        .defaultValue(new SettingColor(73, 107, 255, 255))
        .visible(() -> nether.get() || overworld.get())
        .build()
    );

    // HighwayTracer

    private final Setting<Boolean> highwayTracer = sgHTracer.add(new BoolSetting.Builder()
        .name("active")
        .description("Draws a tracer to the nearest highway.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> highwayTracerDiag = sgHTracer.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Include diagonals.")
        .defaultValue(false)
        .visible(highwayTracer::get)
        .build()
    );

    private final Setting<SettingColor> highwayTracerColor = sgHTracer.add(new ColorSetting.Builder()
        .name("color")
        .description("The line's color.")
        .defaultValue(new SettingColor(73, 107, 255, 255))
        .visible(highwayTracer::get)
        .build()
    );

    // Nav tracer

    private final Setting<Boolean> nav = sgNav.add(new BoolSetting.Builder()
        .name("active")
        .description("Draws a tracer to a point.")
        .defaultValue(false)
        .build()
    );

    private final Setting<BlockPos> navPos = sgNav.add(new BlockPosSetting.Builder()
        .name("Position")
        .description("Position of the tracer.")
        .defaultValue(new BlockPos(0,0,0))
        .visible(nav::get)
        .build()
    );

    private final Setting<SettingColor> navColor = sgNav.add(new ColorSetting.Builder()
        .name("color")
        .description("The tracer's color.")
        .defaultValue(new SettingColor(73, 107, 255, 255))
        .visible(nav::get)
        .build()
    );

    public More_tracers() {
        super(HAHAddon.CATEGORY, "More Tracers", "tracer magic.");
    }

    private ArrayList<ArrayList<Double>> highways = new ArrayList<ArrayList<Double>>();
    private int len = 30000000;

    @Override
    public void onActivate() {
        highways.clear();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.options.hudHidden) return;
        if (nav.get()) {
            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, navPos.get().getX() + 0.5, navPos.get().getY() + 1.5, navPos.get().getZ() + 0.5, navColor.get());
        }

        switch (PlayerUtils.getDimension()) {
            case Overworld -> {
                if (overworld.get()) {
                    event.renderer.line(0, overY.get(), 0, 0, overY.get(), len, highwayColor.get());
                    event.renderer.line(0, overY.get(), 0, len, overY.get(), 0, highwayColor.get());
                    event.renderer.line(0, overY.get(), 0, 0, overY.get(), -len, highwayColor.get());
                    event.renderer.line(0, overY.get(), 0, -len, overY.get(), 0, highwayColor.get());

                    if (overDiag.get()) {
                        event.renderer.line(0, overY.get(), 0, len, overY.get(), len, highwayColor.get());
                        event.renderer.line(0, overY.get(), 0, len, overY.get(), -len, highwayColor.get());
                        event.renderer.line(0, overY.get(), 0, -len, overY.get(), len, highwayColor.get());
                        event.renderer.line(0, overY.get(), 0, -len, overY.get(), -len, highwayColor.get());
                    }
                }
            }
            case Nether -> {
                if (nether.get()) {
                    event.renderer.line(0, netherY.get(), 0, 0, netherY.get(), len, highwayColor.get());
                    event.renderer.line(0, netherY.get(), 0, len, netherY.get(), 0, highwayColor.get());
                    event.renderer.line(0, netherY.get(), 0, 0, netherY.get(), -len, highwayColor.get());
                    event.renderer.line(0, netherY.get(), 0, -len, netherY.get(), 0, highwayColor.get());

                    if (netherDiag.get()) {
                        event.renderer.line(0, netherY.get(), 0, len, netherY.get(), len, highwayColor.get());
                        event.renderer.line(0, netherY.get(), 0, len, netherY.get(), -len, highwayColor.get());
                        event.renderer.line(0, netherY.get(), 0, -len, netherY.get(), len, highwayColor.get());
                        event.renderer.line(0, netherY.get(), 0, -len, netherY.get(), -len, highwayColor.get());
                    }
                }
            }
            default -> {}
        }

        if (highwayTracer.get()) {
            highways = getHighways();
            ArrayList<Double> tmp = highways.get(0);

            for (ArrayList<Double> a : highways) {
                if (tmp.get(0) > a.get(0)) {
                    tmp = a;
                }
            }

            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, tmp.get(1), netherY.get(), tmp.get(2), highwayTracerColor.get());
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (!Utils.canUpdate()) return theme.label("You need to be in a world.");

        WTable table = theme.table();
        createButtons(theme, table);
        return table;
    }

    private void createButtons(GuiTheme theme, WTable table) {

        WButton multiply = table.add(theme.button("Multiply by 8")).expandX().widget();
        WButton divide = table.add(theme.button("Divide by 8")).expandX().widget();

        multiply.visible = nav.get();
        divide.visible = nav.get();

        multiply.action = () -> navPos.set(new BlockPos(navPos.get().getX() * 8, navPos.get().getY(), navPos.get().getZ() * 8));
        divide.action = () -> navPos.set(new BlockPos(navPos.get().getX() / 8, navPos.get().getY(), navPos.get().getZ() / 8));
    }

    private ArrayList getHighways() {
        ArrayList<ArrayList<Double>> dists = new ArrayList<ArrayList<Double>>();

        dists.add(coords(mc.player.getX(), mc.player.getZ(), 0,1,0));
        dists.add(coords(mc.player.getX(), mc.player.getZ(), 1,0,0));
        if (highwayTracerDiag.get()) {
            dists.add(coords(mc.player.getX(), mc.player.getZ(), 1,1,0));
            dists.add(coords(mc.player.getX(), mc.player.getZ(), 1,-1,0));
        }

        return dists;
    }

    private Double distance(Double x, Double y, int a, int b, int c) {
        Double top = Math.abs(a*x + b*y + c);
        Double bot = Math.sqrt(a*a + b*b);

        return top/bot;
    }

    private ArrayList<Double> coords(Double x, Double y, int a, int b, int c) {
        Double rx = (b*(b*x - a*y)-a*c) / (a*a+b*b);
        Double ry = (a*(-b*x + a*y)-b*c) / (a*a+b*b);

        ArrayList<Double> tmp = new ArrayList<Double>();
        tmp.add(distance(x, y, a, b, c));
        tmp.add(rx);
        tmp.add(ry);

        return tmp;
    }


}
