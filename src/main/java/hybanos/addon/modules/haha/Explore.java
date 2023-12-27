package hybanos.addon.modules.haha;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.WaypointsModule;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;

import javax.swing.text.JTextComponent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Explore extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgArea = settings.createGroup("Area");
    private final SettingGroup sgInfo = settings.createGroup("Info");

    private final Setting<Boolean> walk = sgGeneral.add(new BoolSetting.Builder()
        .name("Move")
        .description("Turn on to start moving")
        .defaultValue(false)
        .onChanged(this::onWalkChange)
        .build()
    );

    private final Setting<Integer> range = sgArea.add(new IntSetting.Builder()
        .name("Render distance")
        .description("In chunks")
        .defaultValue(8)
        .sliderRange(1, 32)
        .build()
    );

    private final Setting<BlockPos> pos1 = sgArea.add(new BlockPosSetting.Builder()
        .name("pos1")
        .description("North West corner of the area to scan (- -)")
        .defaultValue(new BlockPos(0,0 ,0))
        .build()
    );

    private final Setting<BlockPos> pos2 = sgArea.add(new BlockPosSetting.Builder()
        .name("pos2")
        .description("South East corner of the area to scan (+ +)")
        .defaultValue(new BlockPos(0,0 ,0))
        .build()
    );

    private final Setting<Direction> dir = sgArea.add(new EnumSetting.Builder<Direction>()
        .name("Direction")
        .description("start from north or south")
        .defaultValue(Direction.NorthToSouth)
        .build()
    );

    private final Setting<Start> start = sgArea.add(new EnumSetting.Builder<Start>()
        .name("Start from")
        .description("Allows you to resume where you left off")
        .defaultValue(Start.Start)
        .build()
    );

    private final Setting<Boolean> info = sgInfo.add(new BoolSetting.Builder()
        .name("Infos")
        .description("progress and stuff")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> render = sgInfo.add(new BoolSetting.Builder()
        .name("Render")
        .description("Draws lines.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> debug = sgInfo.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Haha.")
        .defaultValue(false)
        .build()
    );

    public Explore() {
        super(HAHAddon.CATEGORY, "Explore", "Maps an area");
    }

    private List<PathLine> path = new ArrayList<>();
    private Iterator<PathLine> it;
    private PathLine currLine;
    private int totalLength;

    @Override
    public void onDeactivate() {
        press(mc.options.forwardKey, false);
    }

    public void pathFind() {
        if (info.get()) info("Pathfinding...");
        path = genPath();
        it = path.iterator();
        currLine = null;
        totalLength = (int) blocksLeft();
        if (info.get()) info("Total length : " + totalLength + " blocks");
    }

    public void onWalkChange(boolean b) {
        if (mc.player == null) return;
        if (totalLength == 0) return;
        press(mc.options.forwardKey, b);

        if (path.isEmpty()) pathFind();
        int left = (int) blocksLeft();
        if (info.get()) info(left + " blocks remaining (" + left * 100 / totalLength + "percent)");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (path.isEmpty()) return;
        if (!walk.get()) return;
        if (currLine == null) currLine = it.next();

        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        mc.player.setYaw((float) face(playerX, playerZ, currLine.end.getX(), currLine.end.getZ()));
        press(mc.options.forwardKey, true);
        if (debug.get()) info("blocks left : " + bpDist(currLine.getEnd(), mc.player.getBlockPos()));

        if (bpDist(currLine.getEnd(), mc.player.getBlockPos()) < 3) {
            if (it.hasNext()) {
                currLine = it.next();
                int left = (int) blocksLeft();
                if (info.get()) info(left + " blocks remaining (" + left * 100 / totalLength + "percent)");
            }
            else {
                walk.set(false);
                if (info.get()) info("Done !");
            }
            if (debug.get()) info("next");
        }
    }

    private double bpDist(BlockPos bp1, BlockPos bp2) {
        int x = bp1.getX() - bp2.getX();
        int z = bp1.getZ() - bp2.getZ();

        return Math.sqrt(x*x + z*z);
    }

    private void press(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    @EventHandler
    public void onRender(Render3DEvent event) {

        if (!render.get()) return;

        event.renderer.box(pos1.get().getX(), 80, pos1.get().getZ(), pos2.get().getX(), 80, pos2.get().getZ(), Color.BLACK, Color.BLACK, ShapeMode.Lines, 0);

        if (path.isEmpty()) return;

        for (PathLine p : path) {
            int x1 = p.getStart().getX();
            int x2 = p.getEnd().getX();
            int z1 = p.getStart().getZ();
            int z2 = p.getEnd().getZ();
            event.renderer.line(x1, 80, z1, x2, 80, z2, Color.BLACK);
        }
    }

    private List<PathLine> genPath() {
        List<PathLine> l = new ArrayList<>();

        double xDist = pos2.get().getX() - pos1.get().getX();
        double zDist = pos2.get().getZ() - pos1.get().getZ();

        int spacing = range.get() * 2 * 16;

        BlockPos oldEnd = mc.player.getBlockPos();

        for (int i = 0; i < zDist / spacing; i++) {

            int startX = pos1.get().getX() + spacing / 2;
            int endX = pos2.get().getX() - spacing / 2;

            int z;
            if (dir.get() == Direction.NorthToSouth) {
                z = pos1.get().getZ() + (i * spacing + spacing / 2);
                if (start.get() == Start.CurrentPos && z < mc.player.getZ()) continue;
            } else {
                z = pos2.get().getZ() - (i * spacing + spacing / 2);
                if (start.get() == Start.CurrentPos && z > mc.player.getZ()) continue;
            }

            BlockPos start = new BlockPos(startX, 0, z);
            BlockPos end = new BlockPos(endX, 0, z);

            if (i % 2 == 0) {
                l.add(new PathLine(oldEnd, start));
                l.add(new PathLine(start, end));
                oldEnd = end;
            } else {
                l.add(new PathLine(oldEnd, end));
                l.add(new PathLine(end, start));
                oldEnd = start;
            }
        }
        return l;
    }

    private double blocksLeft()  {
        double dist = 0;
        for (PathLine p : path) {
            if (currLine == p) dist = 0;
            dist += bpDist(p.getStart(), p.getEnd());
        }
        return dist;
    }

    private double face(double playerX, double playerZ, double targetX, double targetZ) {
        double x = targetX - playerX;
        double z = targetZ - playerZ;
        double yaw = Math.toDegrees(Math.atan2(z, x)) - 90;
        return yaw;
    }

    private class PathLine {
        private BlockPos start;
        private BlockPos end;
        private Boolean explored;

        public PathLine(BlockPos start, BlockPos end) {
            this.start = start;
            this.end = end;
        }

        public void setExplored(boolean e) {
            explored = e;
        }

        public BlockPos getStart() {return start;}
        public BlockPos getEnd() {return end;}
        public Boolean getExplored() {return explored;}
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton b = list.add(theme.button("Pathfind")).expandX().widget();
        b.action = this::pathFind;

        return list;
    }

    public enum Direction {
        NorthToSouth,
        SouthToNorth
    }

    public enum Start {
        CurrentPos,
        Start
    }

}
