package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.ArrayList;

public class SCAFFOLD extends Module {

    private final SettingGroup sgGeneral  = settings.getDefaultGroup();

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("The blocks to use")
        .defaultValue()
        .build()
    );

    private final Setting<Mode> yMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("The mode to use for Y level.")
        .defaultValue(Mode.FIRST_PLACE)
        .build()
    );

    private final Setting<Integer> level = sgGeneral.add(new IntSetting.Builder()
        .name("Y level")
        .description("The Y level to place blocks")
        .defaultValue(64)
        .sliderRange(-64, 319)
        .visible(() -> yMode.get() == Mode.STATIC)
        .build()
    );

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("The radius of the bonemeal action")
        .defaultValue(3)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay in between places.")
        .defaultValue(0)
        .build()
    );

    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
        .name("blocks pet tick")
        .description("The maximum amount of blocks to place each tick.")
        .defaultValue(5)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("air place")
        .description("Places blocks with no support.")
        .defaultValue(false)
        .build()
    );

    private int timer = 0;
    private int firstPlace;

    public SCAFFOLD() {
        super(HAHAddon.CATEGORY, "SCAFFOLD\u220b", "Scaffold but bigger");
    }

    public void onActivate() {
        firstPlace = mc.player.getBlockPos().getY() - 1;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (timer >= delay.get()) {
            timer = 0;
        } else {
            timer++;
            return;
        }
        int blockCount = 0;
        for (Block block : blocks.get()) {
            FindItemResult result = InvUtils.find(block.asItem());
            blockCount = blockCount + result.count();
        }
        if (blockCount == 0) return;

        ItemStack handStack = mc.player.getMainHandStack();
        int blocksToPlace = 0;
        int slot = mc.player.getInventory().selectedSlot;

        if (blocks.get().contains(Block.getBlockFromItem(handStack.getItem()))) {
            blocksToPlace = handStack.getCount();
        } else {
            FindItemResult item;
            for (Block block : blocks.get()) {
                item = InvUtils.findInHotbar(block.asItem());
                if (item.found()) {
                    InvUtils.swap(item.slot(), true);
                    break;
                }
            }
            return;
        }

        int y = 0;

        switch (yMode.get()) {
            case UNDER -> y = mc.player.getBlockPos().getY() - 1;
            case FIRST_PLACE -> y = firstPlace;
            case STATIC -> y = level.get();
        }

        blocksToPlace = blocksToPlace > bpt.get() ? bpt.get() : blocksToPlace;
        for (BlockPos bp : bpProvider(mc.player.getBlockPos(), radius.get(), y, airPlace.get())) {
            if (blocksToPlace <= 0) return;
            placeBlock(bp, slot);
            blocksToPlace--;
        }
    }

    private void placeBlock(BlockPos blockpos, int slot) {
        BlockUtils.place(blockpos, Hand.MAIN_HAND, slot, false, 50, false, true, false);
    }

    public List<BlockPos> bpProvider(BlockPos centerPos, int radius, int height, boolean air) {
        if (air) {
            return getSphere(centerPos, radius, height);
        }
        List<BlockPos> blocks = new ArrayList<>();
        for (BlockPos bp : getSphere(centerPos, radius, height)) {
            if (validPlace(bp)) {
                blocks.add(bp);
            }
        }
        return blocks;
    }

    public boolean validPlace(BlockPos pos) {
        boolean valid = false;
        BlockPos newPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        if (pos.getY() < 319 && mc.world.getBlockState(newPos).getBlock() != Blocks.AIR) valid = true;
        newPos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        if (pos.getY() > -64 && mc.world.getBlockState(newPos).getBlock() != Blocks.AIR) valid = true;
        
        if (valid ||
        mc.world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ())).getBlock() != Blocks.AIR ||
        mc.world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ())).getBlock() != Blocks.AIR ||
        mc.world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1)).getBlock() != Blocks.AIR ||
        mc.world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1)).getBlock() != Blocks.AIR
        ) valid = true;

        return valid;
    }

    public List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        List<BlockPos> blocks = new ArrayList<>();

        for (int i = centerPos.getX() - radius; i <= centerPos.getX() + radius; i++) {
            for (int k = centerPos.getZ() - radius; k <= centerPos.getZ() + radius; k++) {
                BlockPos pos = new BlockPos(i, height, k);
                if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) continue;
                if (!blocks.contains(pos)) blocks.add(pos);
            }
        }
        return blocks;
    }

    public enum Mode {
        UNDER,
        FIRST_PLACE,
        STATIC
    }
}
