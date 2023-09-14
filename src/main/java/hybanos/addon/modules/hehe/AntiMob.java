package hybanos.addon.modules.hehe;

import hybanos.addon.HAHAddon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.Set;

// Made by Cookie

public class AntiMob extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDistance = settings.createGroup("Anti Mob");

    public AntiMob() {
        super(HAHAddon.COOKIE, "Anti Mob", "Automatically disconnects you when certain mobs are nearby.");
    }

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entites")
        .description("Select specific entities.")
        .defaultValue(EntityType.CREEPER)
        .build()
    );

    private final Setting<Integer> maxDist = sgDistance.add(new IntSetting.Builder()
        .name("max-distance")
        .description("Min distance for mobs to disconnect you.")
        .defaultValue(10)
        .min(0)
        .sliderMax(50)
        .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-off")
        .description("Disables Anti Mob after usage.")
        .defaultValue(true)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !entities.get().contains(entity.getType())) continue;
            if (mc.player.distanceTo(entity) < maxDist.get()) {
                mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Anti Mob] " + Language.getInstance().get(entity.getType().toString()) + " got in your distance.")));
                if (toggleOff.get()) this.toggle();
                break;
            }
        }	
    }
}