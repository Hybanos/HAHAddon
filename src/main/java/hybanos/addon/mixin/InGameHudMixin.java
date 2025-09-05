package hybanos.addon.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
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

@Mixin(InGameHud.class)
public class InGameHudMixin {
    /*

    @Inject(method="renderCrosshair", at=@At(value = "HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, CallbackInfo info) {

        if (Modules.get().isActive(F3_crosshair.class)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            GameOptions gameOptions = mc.options;

            if (!gameOptions.getPerspective().isFirstPerson() && !Modules.get().get(F3_crosshair.class).getPerspective()) {
                info.cancel();
                return;
            }

            if (mc.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
                Camera camera = mc.gameRenderer.getCamera();
                Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
                matrixStack.push();
                matrixStack.multiplyPositionMatrix(context.getMatrices().peek().getPositionMatrix());
                matrixStack.translate(mc.getWindow().getScaledWidth() / 2, mc.getWindow().getScaledHeight() / 2, 0.0f);
                matrixStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw()));
                matrixStack.scale(-1.0f, -1.0f, -1.0f);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.renderCrosshair(10);
                matrixStack.pop();
                RenderSystem.applyModelViewMatrix();
                info.cancel();
            }
        }
    }

     */
}
