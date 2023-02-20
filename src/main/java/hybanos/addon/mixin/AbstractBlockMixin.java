package hybanos.addon.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import hybanos.addon.modules.haha.Block_rotation;

import java.lang.Math;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    int xOff = (int)(Math.random() * 20000000 - 10000000);
    int yOff = (int)(Math.random() * 20000000 - 10000000);
    int zOff = (int)(Math.random() * 20000000 - 10000000);

    @Inject(method="getRenderingSeed", at=@At("HEAD"), cancellable = true)
    private void onGetRenderingSeed(BlockState state, BlockPos pos, CallbackInfoReturnable<Long> info) {
        if (Modules.get().isActive(Block_rotation.class)) {
            int x = 0;
            int y = 0;
            int z = 0;

            Integer mode = Modules.get().get(Block_rotation.class).getMode();

            if (mode == 1) {
                x = (int)(Math.random() * 20 - 10);
                y = (int)(Math.random() * 20 - 10);
                z = (int)(Math.random() * 20 - 10);
            }
            if (mode == 2) {
                x += pos.getX() + xOff;
                y += pos.getY() + yOff;
                z += pos.getZ() + zOff;
            }

            BlockPos nextPos = new BlockPos(x, y, z);
            info.setReturnValue(MathHelper.hashCode(nextPos));
        }
    }
}
