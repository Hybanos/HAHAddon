package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.entity.EntityType;

import java.util.Set;

public class Photoshoot extends Module {
    private SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("The mode to use.")
        .defaultValue(Mode.CAMERA)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entites")
        .description("Select specific entities.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("distance")
        .description("The distance from which the heads will lock.")
        .defaultValue(50)
        .sliderMin(0)
        .sliderMax(200)
        .build()
    );

    private final Setting<Double> pitch = sgGeneral.add(new DoubleSetting.Builder()
        .name("pitch")
        .description("The head and body pitch of the entities.")
        .defaultValue(90)
        .sliderMin(0)
        .sliderMax(360)
        .visible(() -> mode.get() == Mode.PITCHYAW)
        .build()
    );

    private final Setting<Double> yaw = sgGeneral.add(new DoubleSetting.Builder()
        .name("yaw")
        .description("The head and body yaw of the entities.")
        .defaultValue(0)
        .sliderMin(0)
        .sliderMax(360)
        .visible(() -> mode.get() == Mode.PITCHYAW)
        .build()
    );

    private final Setting<Boolean> player = sgGeneral.add(new BoolSetting.Builder()
        .name("include player")
        .description("If the player should have his head rotated.")
        .defaultValue(false)
        .build()
    );

    public Photoshoot() {
        super(HAHAddon.CATEGORY, "Photoshoot", "Locks the heads of nearby entities to make better screenshots :D");
    }

    public int getMode() {
        if (mode.get() == Mode.CAMERA) return 0;
        if (mode.get() == Mode.PITCHYAW) return 1;
        return -1;
    }

    public double getDistance() {
        return distance.get();
    }

    public Set<?> getEntities() {
        return entities.get();
    }

    public double getPitch() {
        return pitch.get();
    }

    public double getYaw() {
        return yaw.get();
    }

    public Boolean getPlayer() {
        return player.get();
    }

    public enum Mode {
        CAMERA,
        PITCHYAW
    }
}
