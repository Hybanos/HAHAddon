package hybanos.addon.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.client.option.GameOptions;
import net.minecraft.world.GameMode;
import net.minecraft.util.math.Vec3f;
import com.mojang.blaze3d.systems.RenderSystem;

import hybanos.addon.modules.haha.F3_crosshair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method="renderCrosshair", at=@At(value = "HEAD"), cancellable = true)
    private void onRenderCrosshair(MatrixStack matrices, CallbackInfo info) {

        if (Modules.get().isActive(F3_crosshair.class)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            GameOptions gameOptions = mc.options;

            if (!gameOptions.getPerspective().isFirstPerson() && !Modules.get().get(F3_crosshair.class).getPerspective()) {
                info.cancel();
                return;
            }

            if (mc.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
                Camera camera = mc.gameRenderer.getCamera();
                MatrixStack matrixStack = RenderSystem.getModelViewStack();
                matrixStack.push();
                matrixStack.translate(mc.getWindow().getScaledWidth() / 2, mc.getWindow().getScaledHeight() / 2, ((InGameHud)(Object)this).getZOffset());
                matrixStack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));
                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw()));
                matrixStack.scale(-1.0f, -1.0f, -1.0f);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.renderCrosshair(10);
                matrixStack.pop();
                RenderSystem.applyModelViewMatrix();
                info.cancel();
            }
        }
    }
}
