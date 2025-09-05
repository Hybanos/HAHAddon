package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

public class No_bed_interact extends Module {

    private final SettingGroup sgBed = settings.createGroup("Beds");
    private final SettingGroup sgAnchor = settings.createGroup("Anchors");

    private final Setting<Boolean> bedOW = sgBed.add(new BoolSetting.Builder()
        .name("Overworld")
        .description("Blocks interaction with beds in the overwold.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> bedNether = sgBed.add(new BoolSetting.Builder()
        .name("Nether")
        .description("Blocks interaction with beds in the nether.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> bedEnd = sgBed.add(new BoolSetting.Builder()
        .name("End")
        .description("Blocks interaction with beds in the end.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> anchorOW = sgAnchor.add(new BoolSetting.Builder()
        .name("Overworld")
        .description("Blocks interaction with Anchors in the overwold.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> anchorNether = sgAnchor.add(new BoolSetting.Builder()
        .name("Nether")
        .description("Blocks interaction with Anchors in the nether.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> anchorEnd = sgAnchor.add(new BoolSetting.Builder()
        .name("End")
        .description("Blocks interaction with Anchors in the end.")
        .defaultValue(false)
        .build()
    );

    public No_bed_interact() {
        super(HAHAddon.CATEGORY, "No Bed Interact", "Prevents you from reseting your spawn, or blowing up.");
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (shouldCancel(event.result)) event.cancel();
    }

    private Boolean shouldCancel(BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();

        switch (PlayerUtils.getDimension()) {
            case Overworld -> {
                if ((mc.world.getBlockState(pos).getBlock() instanceof BedBlock && bedOW.get()) ||
                (mc.world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock && anchorOW.get())) return true;
                else return false;
            }
            case Nether -> {
                if ((mc.world.getBlockState(pos).getBlock() instanceof BedBlock && bedNether.get()) ||
                (mc.world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock && anchorNether.get())) return true;
                else return false;
            }
            case End -> {
                if ((mc.world.getBlockState(pos).getBlock() instanceof BedBlock && bedEnd.get()) ||
                (mc.world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock && anchorEnd.get())) return true;
                else return false;
            }
        }
        return false;
    }
}
