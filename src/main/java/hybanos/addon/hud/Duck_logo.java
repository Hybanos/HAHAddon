package hybanos.addon.hud;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.util.Identifier;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Duck_logo extends HudElement {
    public static final HudElementInfo<Duck_logo> INFO = new HudElementInfo<>(HAHAddon.HUD_GROUP, "Duck Logo", "Shows the duck logo.", Duck_logo::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the logo.")
        .defaultValue(0.5)
        .min(0.1)
        .sliderRange(0.1, 5)
        .build()
    );

    private final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma")
        .description("Rainbow duck ?")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("chroma speed")
        .description("The speed of the duck.")
        .defaultValue(0.09)
        .min(0.01)
        .sliderMax(5)
        .decimalPlaces(2)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of the background.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(() -> !chroma.get())
        .build()
    );

    public Duck_logo() {
        super(INFO);
    }

    private final Identifier DUCK = new Identifier("hahaddon", "textures/monners.png");
    private static final RainbowColor RAINBOW = new RainbowColor();

    @Override
    public void setSize(double h, double w) {
        super.setSize(512 * scale.get(), 512 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(1,1);
        if (!Utils.canUpdate()) return;
        GL.bindTexture(DUCK);
        Renderer2D.TEXTURE.begin();

        if (chroma.get()) {
            RAINBOW.setSpeed(chromaSpeed.get() / 100);
            Renderer2D.TEXTURE.texQuad(this.getX(), this.getY(), this.getWidth(), this.getHeight(), RAINBOW.getNext());
        } else {
            Renderer2D.TEXTURE.texQuad(this.getX(), this.getY(), this.getWidth(), this.getHeight(), color.get());
        }

        Renderer2D.TEXTURE.render(null);
    }
}
