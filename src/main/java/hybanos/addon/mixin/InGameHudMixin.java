package hybanos.addon.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.GameMode;
import com.mojang.blaze3d.systems.RenderSystem;

import hybanos.addon.modules.haha.F3_crosshair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "shouldRenderCrosshair", at=@At(value= "HEAD"), cancellable = true)
    private void onShouldRenderCrosshair(CallbackInfoReturnable<Boolean> cir) {
        if (Modules.get().isActive(F3_crosshair.class)) {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean val = client.options.getPerspective() == Perspective.FIRST_PERSON && !client.player.hasReducedDebugInfo() && client.options.getReducedDebugInfo().getValue() == false;
            cir.setReturnValue(val);
        }
    }
}
