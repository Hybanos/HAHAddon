package hybanos.addon.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import hybanos.addon.modules.haha.Photoshoot;

import java.lang.Math;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method="tick", at=@At("TAIL"))
    private void onTick(CallbackInfo info) {
        if (Modules.get().isActive(Photoshoot.class) && (!(((Entity)(Object)this) instanceof ClientPlayerEntity) || Modules.get().get(Photoshoot.class).getPlayer()) && Modules.get().get(Photoshoot.class).getEntities().contains(((Entity)(Object)this).getType())) {

            int mode =  Modules.get().get(Photoshoot.class).getMode();

            double x = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().getX() - ((Entity)(Object)this).getX();
            double y = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().getY() - ((Entity)(Object)this).getEyeY();
            double z = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().getZ() - ((Entity)(Object)this).getZ();

            double dist = Math.sqrt(x*x + y*y + z*z);

            double distance = Modules.get().get(Photoshoot.class).getDistance();

            if (dist < distance) {

                double ya = Math.toDegrees(Math.atan2(z, x));
                double pi = Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z)));

                if (mode == 1) {
                    ya = Modules.get().get(Photoshoot.class).getYaw() + 90;
                    pi = Modules.get().get(Photoshoot.class).getPitch();
                }

                ((Entity)(Object)this).setYaw((float)ya - 90 % 360);
                ((Entity)(Object)this).setHeadYaw((float)ya - 90 % 360);
                ((Entity)(Object)this).setPitch(-(float)pi % 360);

            }
        }
    }
}
