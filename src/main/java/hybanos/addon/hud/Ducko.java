package hybanos.addon.hud;

import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import net.minecraft.util.Identifier;
import static meteordevelopment.meteorclient.utils.Utils.WHITE;

public class Ducko extends HudElement {
    private final Identifier TEXTURE = new Identifier("hahaddon", "textures/ducko.png");

    public Ducko(HUD hud) {
        super(hud, "Ducko", "Ducko");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(128,128);
    }

    @Override
    public void render(HudRenderer renderer) {
        GL.bindTexture(TEXTURE);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(box.getX(), box.getY(), box.width, box.height, WHITE);
        Renderer2D.TEXTURE.render(null);
    }
}
