package hybanos.addon.hud;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.renderer.GL;
import net.minecraft.util.Identifier;

import java.lang.Math;

import static meteordevelopment.meteorclient.MeteorClient.mc;

 public class BetterCompass extends HudElement {
    public static final HudElementInfo<BetterCompass> INFO = new HudElementInfo<>(HAHAddon.HUD_GROUP, "BetterCompass", "Because meteor's sucks.", BetterCompass::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgText = settings.createGroup("Text");
    private final SettingGroup sgSpawn = settings.createGroup("Spawn");
    private final SettingGroup sgQuadrant = settings.createGroup("Quadrant");
    private final SettingGroup sgBackground = settings.createGroup("Background/Frame");

    // General

    private final Setting<Double> width = sgGeneral.add(new DoubleSetting.Builder()
        .name("width")
        .description("The width of the box.")
        .defaultValue(120)
        .min(50)
        .max(1000)
        .sliderMin(50)
        .sliderMax(500)
        .build()
    );

    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("The height of the box.")
        .defaultValue(120)
        .min(50)
        .max(1000)
        .sliderMin(50)
        .sliderMax(500)
        .build()
    );

    private final Setting<Double> arrowScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("arrow scale")
        .description("The scale of the arrow.")
        .defaultValue(50)
        .sliderMin(20)
        .sliderMax(200)
        .build()
    );

    private final Setting<SettingColor> arrowColor = sgGeneral.add(new ColorSetting.Builder()
        .name("arrow color")
        .description("Color of the arrow.")
        .defaultValue(new SettingColor(73,107,255, 255))
        .build()
    );

    // Text

    private final Setting<TextRender> textRender = sgText.add(new EnumSetting.Builder<TextRender>()
        .name("text")
        .description("How to render the text.")
        .defaultValue(TextRender.AXIS)
        .build()
    );

    private final Setting<Double> slide = sgText.add(new DoubleSetting.Builder()
        .name("text offset")
        .description("Moves the text slightly.")
        .defaultValue(-13.5)
        .sliderMin(-50)
        .sliderMax(50)
        .visible(() -> textRender.get() != TextRender.NONE)
        .build()
    );

    private final Setting<Double> xOffText = sgText.add(new DoubleSetting.Builder()
        .name("X offset")
        .description("Moves the text slightly.")
        .defaultValue(1.55)
        .sliderMin(-50)
        .sliderMax(50)
        .visible(() -> textRender.get() != TextRender.NONE)
        .build()
    );

    private final Setting<Double> yOffText = sgText.add(new DoubleSetting.Builder()
        .name("Y offset")
        .description("Moves the text slightly.")
        .defaultValue(13.2)
        .sliderMin(-50)
        .sliderMax(50)
        .visible(() -> textRender.get() != TextRender.NONE)
        .build()
    );

    private final Setting<SettingColor> northColor = sgText.add(new ColorSetting.Builder()
        .name("north color")
        .description("Color of the north text.")
        .defaultValue(new SettingColor(255, 71, 105,255))
        .visible(() -> textRender.get() != TextRender.NONE)
        .build()
    );

    private final Setting<SettingColor> eastColor = sgText.add(new ColorSetting.Builder()
        .name("east color")
        .description("Color of the east text.")
        .defaultValue(new SettingColor(255,255,255,255))
        .visible(() -> textRender.get() != TextRender.NONE)
        .build()
    );

    private final Setting<SettingColor> southColor = sgText.add(new ColorSetting.Builder()
        .name("south color")
        .description("Color of the south text.")
        .defaultValue(new SettingColor(255,255,255,255))
        .visible(() -> textRender.get() != TextRender.NONE)
        .build()
    );

    private final Setting<SettingColor> westColor = sgText.add(new ColorSetting.Builder()
        .name("west color")
        .description("Color of the west text.")
        .defaultValue(new SettingColor(255,255,255,255))
        .visible(() -> textRender.get() != TextRender.NONE)
        .build()
    );

    private final Setting<Boolean> textShadow = sgText.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Renders shadow behind text.")
        .defaultValue(false)
        .build()
    );

    // Spawn

    private final Setting<SpawnMode> spawnMode = sgSpawn.add(new EnumSetting.Builder<SpawnMode>()
        .name("spawn marker")
        .description("How to render the text.")
        .defaultValue(SpawnMode.CIRCLE)
        .build()
    );

    private final Setting<Double> spawnLineScale = sgSpawn.add(new DoubleSetting.Builder()
        .name("line thickness")
        .description("The thickness of the line.")
        .defaultValue(2)
        .sliderMin(0)
        .sliderMax(10)
        .min(0)
        .visible(() -> spawnMode.get() == SpawnMode.LINE)
        .build()
    );

    private final Setting<Double> spawnScale = sgSpawn.add(new DoubleSetting.Builder()
        .name("dot scale")
        .description("The scale of the dot.")
        .defaultValue(20)
        .sliderMin(0)
        .sliderMax(100)
        .visible(() -> spawnMode.get() == SpawnMode.CIRCLE)
        .build()
    );

    private final Setting<Double> spawnRadius = sgSpawn.add(new DoubleSetting.Builder()
        .name("radius")
        .description("The distance from the marker to the center of the compass.")
        .defaultValue(20)
        .sliderMin(0)
        .sliderMax(200)
        .visible(() -> spawnMode.get() != SpawnMode.NONE)
        .build()
    );

    private final Setting<Double> spawnDistance = sgSpawn.add(new DoubleSetting.Builder()
        .name("spawn distance (km)")
        .description("The max distance from spawn where the marker renders. (km)")
        .defaultValue(50)
        .sliderMin(0)
        .sliderMax(3000)
        .visible(() -> spawnMode.get() != SpawnMode.NONE)
        .build()
    );

    private final Setting<SettingColor> spawnColor = sgSpawn.add(new ColorSetting.Builder()
        .name("spawn color")
        .description("Color of the spawn marker.")
        .defaultValue(new SettingColor(255, 71, 105, 255))
        .visible(() -> spawnMode.get() != SpawnMode.NONE)
        .build()
    );

    // Navigation

    private final Setting<Boolean> quadrant = sgQuadrant.add(new BoolSetting.Builder()
        .name("show quadrant")
        .description("Splits the compass in 4 quadrants.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> quadrantDistance = sgQuadrant.add(new DoubleSetting.Builder()
        .name("spawn distance (km)")
        .description("The max distance from spawn where the quadrant renders. (km)")
        .defaultValue(50)
        .sliderMin(0)
        .sliderMax(3000)
        .visible(quadrant::get)
        .build()
    );

    private final Setting<SettingColor> quadrantColor = sgQuadrant.add(new ColorSetting.Builder()
        .name("quadrant color")
        .description("Color of the quadrant.")
        .defaultValue(new SettingColor(40,80,200, 150))
        .visible(quadrant::get)
        .build()
    );

    // Background/Borders

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("show background")
        .description("Shows the background.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> frame = sgBackground.add(new BoolSetting.Builder()
        .name("show frame")
        .description("Shows the frame.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background color")
        .description("Color of the background.")
        .defaultValue(new SettingColor(10, 10, 50, 100))
        .visible(background::get)
        .build()
    );

    private final Setting<SettingColor> frameColor = sgBackground.add(new ColorSetting.Builder()
        .name("frame color")
        .description("Color of the frame.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(frame::get)
        .build()
    );

    public BetterCompass() {
        super(INFO);
    }

    private final Identifier ARROW = new Identifier("hahaddon", "textures/arrow.png");
    private final Identifier CIRCLE = new Identifier("hahaddon", "textures/circle.png");

    @Override
    public void setSize(double w, double h) {
        super.setSize(width.get(), height.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(1,1);
        double x = this.getX();
        double y = this.getY();
        double height = this.getHeight();
        double width = this.getWidth();
        double arrowX = arrowScale.get();
        double arrowY = arrowScale.get();
        double playerX;
        double playerZ;
        double playerYaw;
        if (mc.world != null) {
            playerX = mc.player.getPos().getX();
            playerZ = mc.player.getPos().getZ();
            playerYaw = mc.player.getYaw();
        } else {
            playerX = 1;
            playerZ = 1;
            playerYaw = 0;
        }
        double dist = Math.sqrt(playerX*playerX + playerZ*playerZ);
        double xOff = xOffText.get();
        double yOff = yOffText.get();
        double off = slide.get();

        if (background.get()) {
            renderer.quad(x, y , this.getWidth() , this.getHeight(), backgroundColor.get());
        }

        if (quadrant.get() && dist < quadrantDistance.get() * 1000) {
            double posX = x;
            double posY = y;
            if (playerX > 0) posX = x + width/2;
            if (playerZ > 0) posY = y + height/2;
            renderer.quad(posX, posY, width/2, height/2, quadrantColor.get());
        }

        if (spawnMode.get() != SpawnMode.NONE && spawnDistance.get() * 1000 > dist) {
            double angle = Math.atan2(0 - playerX, 0 - playerZ) - Math.toRadians(90);

            double circleX = Math.cos(angle) * spawnRadius.get();
            double circleY = -(Math.sin(angle) * spawnRadius.get());
            double circleSize = spawnScale.get();

            if (spawnMode.get() == SpawnMode.LINE) {
                for (int i=0; i < spawnLineScale.get(); i++) {
                    renderer.line(x + width/2 + i, y + height/2 + i,x + width/2 + circleX + i,y + height/2 + circleY + i, spawnColor.get());
                    renderer.line(x + width/2 - i, y + height/2 - i,x + width/2 + circleX + i,y + height/2 + circleY + i, spawnColor.get());
                }

            } else {
                GL.bindTexture(CIRCLE);
                Renderer2D.TEXTURE.begin();
                Renderer2D.TEXTURE.texQuad(x + width/2 + circleX - circleSize/2,y + height/2 + circleY - circleSize/2, circleSize, circleSize, spawnColor.get());
                Renderer2D.TEXTURE.render(null);
            }
        }

        switch(textRender.get()) {
            case DIRECTION -> {
                xOff *= 4;
                renderer.text("N", x + width/2 - xOff, y - yOff - off, northColor.get(), textShadow.get());
                renderer.text("S", x + width/2 - xOff, y + height - yOff + off, southColor.get(), textShadow.get());
                renderer.text("E", x + width - xOff + off, y + height/2 - yOff, eastColor.get(), textShadow.get());
                renderer.text("W", x - xOff - off, y + height/2 - yOff, westColor.get(), textShadow.get());
            }
            case AXIS -> {
                xOff *= 9;
                renderer.text("Z-", x + width/2 - xOff, y - yOff - off, northColor.get(), textShadow.get());
                renderer.text("Z+", x + width/2 - xOff, y + height - yOff + off, southColor.get(), textShadow.get());
                renderer.text("X+", x + width - xOff + off, y + height/2 - yOff, eastColor.get(), textShadow.get());
                renderer.text("X-", x - xOff - off, y + height/2 - yOff, westColor.get(), textShadow.get());
            }
            case BOTH -> {
                xOff *= 18;
                renderer.text("N Z-", x + width/2 - xOff, y - yOff - off, northColor.get(), textShadow.get());
                renderer.text("S Z+", x + width/2 - xOff, y + height - yOff + off, southColor.get(), textShadow.get());
                renderer.text("E X+", x + width - xOff + off, y + height/2 - yOff, eastColor.get(), textShadow.get());
                renderer.text("W X-", x - xOff - off, y + height/2 - yOff, westColor.get(), textShadow.get());
            }
        }

        if (frame.get()) {
            renderer.quad(x - 1, y - 1, width + 2, 1, frameColor.get());
            renderer.quad(x - 1, y + height - 1, width + 2, 1, frameColor.get());
            renderer.quad(x - 1, y, 1, height, frameColor.get());
            renderer.quad(x + width, y, 1, height, frameColor.get());
        }

        GL.bindTexture(ARROW);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x + width/2 - arrowX/2,y + height/2 - arrowY/2, arrowX, arrowY, playerYaw + 180, 0, 0, 1, 1, arrowColor.get());
        Renderer2D.TEXTURE.render(null);

    }

    public enum TextRender {
        DIRECTION,
        AXIS,
        BOTH,
        NONE
    }

    public enum SpawnMode {
        CIRCLE,
        LINE,
        NONE
    }
}
