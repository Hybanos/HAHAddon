package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

public class Nav_tracer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<BlockPos> pos = sgGeneral.add(new BlockPosSetting.Builder()
        .name("Position")
        .description("Position of the tracer")
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The tracer's color.")
        .defaultValue(new SettingColor(73, 107, 255, 255))
        .build()
    );

    public Nav_tracer() {
        super(HAHAddon.CATEGORY, "Nav tracer", "Draws a tracer to a point.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.options.hudHidden) return;
        event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, pos.get().getX() + 0.5, pos.get().getY() + 1.5, pos.get().getZ() + 0.5, color.get());
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

        multiply.action = () -> pos.set(new BlockPos(pos.get().getX() * 8, pos.get().getY(), pos.get().getZ() * 8));
        divide.action = () -> pos.set(new BlockPos(pos.get().getX() / 8, pos.get().getY(), pos.get().getZ() / 8));
    }
}
