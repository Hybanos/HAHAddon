package hybanos.addon.hud;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;

import java.time.Instant;
import java.lang.Math;
import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

// TODO, optimise, not check for null chunks

public class New_chunks extends HudElement {
    public static final HudElementInfo<New_chunks> INFO = new HudElementInfo<>(HAHAddon.HUD_GROUP, "New Chunks", "Brain-powered processing.", New_chunks::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBar = settings.createGroup("Timing bar");

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the logo.")
        .defaultValue(0.5)
        .min(0.1)
        .sliderRange(0.1, 5)
        .build()
    );

    private final Setting<Integer> size = sgGeneral.add(new IntSetting.Builder()
        .name("size")
        .description("Should match your render distance.")
        .defaultValue(16)
        .sliderRange(4,32)
        .onChanged(size -> onSizeChange())
        .build()
    );

    private final Setting<Integer> gens = sgGeneral.add(new IntSetting.Builder()
        .name("generations")
        .description("")
        .defaultValue(5)
        .build()
    );

    private final Setting<Integer> trigger = sgGeneral.add(new IntSetting.Builder()
        .name("time threshold (ms)")
        .description("")
        .defaultValue(20)
        .sliderRange(1,100)
        .build()
    );

    private final Setting<Integer> network = sgGeneral.add(new IntSetting.Builder()
        .name("network lag")
        .description("")
        .defaultValue(3)
        .sliderRange(0, 10)
        .build()
    );

    private final Setting<Integer> barNumber = sgBar.add(new IntSetting.Builder()
        .name("chunk count")
        .description("")
        .defaultValue(100)
        .sliderRange(0, 1000)
        .onChanged(barNumber -> updateBar())
        .build()
    );

    private final Setting<Integer> barLower = sgBar.add(new IntSetting.Builder()
        .name("lower limit (ms)")
        .description("")
        .defaultValue(1)
        .sliderRange(1, 50)
        .min(1)
        .onChanged(barLower -> updateBar())
        .build()
    );

    private final Setting<Integer> barUpper = sgBar.add(new IntSetting.Builder()
        .name("upper limit (ms)")
        .description("")
        .defaultValue(250)
        .sliderRange(0, 1000)
        .onChanged(barUpper -> updateBar())
        .build()
    );

    public New_chunks() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    private double width = 512;
    private double height = 512;
    private double boxX = this.getX();
    private double boxY = this.getY();

    public Grid grid = new Grid(size.get() * 2 + 1);
    public TimeBar timeBar = new TimeBar();

    private long lastChunk;
    private Double averageTime = 0.0;

    private Double posX;
    private Double posZ;
    public int chunkPosX;
    public int chunkPosZ;
    private Double oldPosX;
    private Double oldPosZ;

    private int timer = 0;
    private int networkChunks;

    private void info(String text) {
        ChatUtils.info(text);
    }

    private void onSizeChange() {
        grid = new Grid(size.get() * 2 + 1);
    }

    private void updateBar() {
        timeBar.update();
    }

    @Override
    public void setSize(double w, double h) {
        super.setSize(width * scale.get(), height * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(1,1);
        if (mc.player == null || mc.world == null) return;

        boxX = this.getX();
        boxY = this.getY();

        double x = boxX;
        double y = boxY;
        Double chunkSize = (width*scale.get())/(size.get()*2 + 1);
        updateChunkPos();

        for (int i = 0; i < grid.size(); i++) {
            for (int j = 0; j < grid.size(); j++) {
                Double localX = x+chunkSize*i;
                Double localY = y+chunkSize*j;

                int gen = 255 - (int)(((float)grid.getGeneration(i, j) / (float)gens.get()) * 255);
                gen = Math.min(255, Math.max(0, gen));
                int bg = 255;
                if (grid.getValue(i, j) == -1) bg = 0;
                Color color = new Color(gen, gen, gen, bg);

                renderer.quad(localX, localY, chunkSize, chunkSize, color);
                if (i == j && i == (grid.size() - 1) / 2 + 1) {
                    renderer.quad(localX, localY, chunkSize, chunkSize, new Color(255, 71, 105,255));
                }
            }
        }
        timeBar.render(renderer);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof ChunkDataS2CPacket) {

            int xChunk = ((ChunkDataS2CPacket)event.packet).getX();
            int zChunk = ((ChunkDataS2CPacket)event.packet).getZ();

            int xOff = xChunk - chunkPosX + grid.size() / 2 + 1;
            int zOff = zChunk -chunkPosZ + grid.size() / 2 + 1;

            if (!(xOff > grid.size() - 1 || xOff < 0 || zOff > grid.size() - 1 || zOff < 0) && distance(xOff - grid.size() / 2 + 1, zOff - grid.size() / 2 + 1) < size.get()) {

                long time = Math.max(0, Instant.now().toEpochMilli() - lastChunk);
                if (networkChunks > 0) {
                    time = 0L;
                    networkChunks--;
                }
                grid.render(xOff, zOff, (int)time);
                timeBar.addTime((int)time);
                lastChunk = Instant.now().toEpochMilli();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        timer++;
        if (timer %  20 == 0) {
            grid.flatten();
        }
    }

    private void updateChunkPos(){
        posX = mc.player.getPos().getX();
        posZ = mc.player.getPos().getZ();

        if (oldPosX == null) oldPosX = posX;
        if (oldPosZ == null) oldPosZ = posZ;

        chunkPosX = posX.intValue() / 16;
        chunkPosZ = posZ.intValue() / 16;
        int oldChunkPosX = oldPosX.intValue() / 16;
        int oldChunkPosZ = oldPosZ.intValue() / 16;

        oldPosX = posX;
        oldPosZ = posZ;

        if (chunkPosX != oldChunkPosX || chunkPosZ != oldChunkPosZ) {
            grid.offset(chunkPosX - oldChunkPosX, chunkPosZ - oldChunkPosZ);
            networkChunks = network.get();
        }
    }

    private int distance(int x, int z) {
        return (int) Math.sqrt(x*x + z*z);
    }

    public int sigmoid(int x) {
        return (int)(255 / ( 1 + Math.pow(Math.E, -(x / 20 - 6)) ) );
    }

    public class Grid {

        private Chunk[][] table;

        public Grid(int range) {
            table = new Chunk[range][range];

            for (int x = 0; x < size() - 1; x++) {
                for (int z = 0; z < size() - 1; z++) {
                    table[x][z] = new Chunk();
                }
            }
        }

        public int getValue(int x, int z) {
            if (table[x][z] == null) table[x][z] = new Chunk();
            return table[x][z].getValue();
        }

        public int getGeneration(int x, int z) {
            if (table[x][z] == null) table[x][z] = new Chunk();
            return table[x][z].getGeneration();
        }

        public void setValue(int x, int z, int value) {
            if (table[x][z] == null) table[x][z] = new Chunk();
            table[x][z].setValue(value);
        }

        public void render(int x, int z, int millis) {
            table[x][z].render(millis);
        }

        public int size() {
            return table.length;
        }

        public void offset(int xOff, int zOff) {

            Chunk[][] copy =  new Chunk[table.length][table.length];

            for (int x = 0; x < table.length; x++) {
                for (int z = 0; z < table.length; z++) {

                    int xNew = x + xOff;
                    int zNew = z + zOff;

                    if (xNew > table.length - 1 || xNew < 0 || zNew > table.length - 1 || zNew < 0) continue;

                    copy[x][z] = new Chunk(table[xNew][zNew]);
                }
            }

            table = copy;
        }

        public List<Chunk> neighboors(int x, int z, Boolean laggy) {

            List<Chunk> chunks = new ArrayList<>();

            int xLower = x == 0 ? 0 : x - 1;
            int xUpper = x == size() - 1 ? size() - 1 : x + 1;
            int zLower = z == 0 ? 0 : z - 1;
            int zUpper = z == size() - 1 ? size() - 1 : z + 1;

            for (int i = xLower; i <= xUpper ; i++) {
                for (int j = zLower; j <= zUpper; j++) {
                    if (table[i][j] == null) table[i][j] = new Chunk();
                    if (!(x == i && z == j) && table[i][j].getValue() > 0) {
                        if (!table[i][j].isLaggy() && laggy) continue;
                        chunks.add(table[i][j]);
                    }
                }
            }
            return chunks;
        }

        public void flatten() {

            Chunk[][] copy =  new Chunk[table.length][table.length];

            for (int x = 0; x < table.length; x++) {
                for (int z = 0; z < table.length; z++) {

                    copy[x][z] = new Chunk(table[x][z]);

                    List<Chunk> neighboors = neighboors(x, z, true);
                    int maxGen = 0;

                    for (Chunk neighboor : neighboors) {
                        maxGen = Math.max(maxGen, neighboor.getGeneration());
                    }

                    if (!table[x][z].isLaggy()) {
                        copy[x][z].collapse(maxGen - 1);
                    }
                }
            }

            table = copy;
        }
    }

    public class Chunk {
        private int value = -1;
        private Boolean laggy = false;
        private int generation = -1;

        public Chunk() {}

        public Chunk(Chunk newChunk) {
            value = newChunk.getValue();
            generation = newChunk.getGeneration();
            laggy = newChunk.isLaggy();
        }

        public void render(int millis) {
            if (value != -1) return;
            value = millis;
            if (value > trigger.get()) {
                laggy=true;
                generation = gens.get();
            }
        }

        public Boolean isLaggy() {
            return laggy;
        }

        public void setLaggy(Boolean lag) {
            laggy = lag;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int val) {
            value = val;
            if (value > 175) laggy = true;
        }

        public int getGeneration() {
            return generation;
        }

        public void collapse(int gen) {
            if (gen <= 0 || value == -1) return;
            setValue(200);
            generation = gen;
        }
    }

    public class TimeBar {
        private int lower = barLower.get();
        private int upper = barUpper.get();
        private int number = barNumber.get();
        public List<Integer> times = new ArrayList<>();

        public void addTime(int time) {
            times.add(time);
            while (times.size() > number) {
                times.remove(0);
            }
        }

        public void update() {
            lower = barLower.get();
            upper = barUpper.get();
            number = barNumber.get();
        }

        public void render(HudRenderer renderer) {
            double x = boxX;
            double y = boxY;

            double square = height * scale.get();

            Color left = new Color(255, 255, 255,255);
            Color right = new Color(0, 0, 0,255);

            renderer.quad(x, y + square + 20, square, 20, left, right, right, left);

            Color timeColor = new Color(255, 71, 105, 150);

            List<Integer> timesCopy = new ArrayList<>(times);

            for (Integer time : timesCopy) {
                if (time == null) continue;
                double xTime = x + ((double)time) / upper * square / lower - 2;
                double yTime = y + square + 20 - 5;

                xTime = Math.min(square - 2, xTime);

                renderer.quad(xTime, yTime, 4, 30, timeColor);
            }

            Color color = new Color(255, 71, 105, 255);
            double xThre = x + ((double)trigger.get()) / upper * square / lower - 2;
            double yThre = y + square + 20 - 10;

            renderer.quad(xThre, yThre, 4, 40, color);
        }
    }
}
