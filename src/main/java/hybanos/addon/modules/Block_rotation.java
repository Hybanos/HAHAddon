package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import hybanos.addon.mixin.AbstractBlockMixin;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;

public class Block_rotation extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Disables or randomizes block rotation.")
        .defaultValue(Mode.STATIC)
        .onChanged(Mode -> reload())
        .build()
    );

    public Block_rotation() {
        super(HAHAddon.CATEGORY, "Block Rotation", "Randomizes the rotation on blocks.");
    }

    public Integer getMode() {
        if (mode.get() == Mode.RANDOM) return 1;
        if (mode.get() == Mode.STATIC) return 2;
        else return 3;
    }

    @Override
    public void onActivate() {
        reload();
    }

    private void reload() {
        mc.worldRenderer.reload();
    }

    public enum Mode {
        RANDOM,
        STATIC,
        DISABLE
    }
}
