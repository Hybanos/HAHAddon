package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.Items;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.ArrayList;

public class Auto_grow extends Module {

    private final SettingGroup sgGeneral  = settings.getDefaultGroup();

    private final Setting<List<Block>> crops = sgGeneral.add(new BlockListSetting.Builder()
        .name("filter")
        .description("The crops to bonemeal")
        .defaultValue(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM)
        .filter(this::blockFilter)
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
        .description("The delay in ticks")
        .defaultValue(0)
        .build()
    );

    private final Setting<Integer> count = sgGeneral.add(new IntSetting.Builder()
        .name("count")
        .description("The number of bonemeal actions per tick")
        .defaultValue(1)
        .sliderMin(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Direc> direction = sgGeneral.add(new EnumSetting.Builder<Direc>()
        .name("direction")
        .description("The face to bonemeal on")
        .defaultValue(Direc.UP)
        .build()
    );

    private final Setting<Boolean> hands = sgGeneral.add(new BoolSetting.Builder()
        .name("switch hands")
        .description("Selects the slot with bonemeal for you")
        .defaultValue(false)
        .build()
    );

    private int timer;
    private int n = 0;
    private Direction dir;

    public Auto_grow() {
        super(HAHAddon.CATEGORY, "Auto Grow", "Automatically bonemeals nearby crops.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (timer < delay.get()) {
            timer++;
            return;
        } else {
            timer = 0;
        }

        FindItemResult bone_meal = InvUtils.findInHotbar(Items.BONE_MEAL);
        if (!bone_meal.found()) return;

        switch(direction.get()) {
            case UP:
                dir = Direction.UP;
            case DOWN:
                dir = Direction.DOWN;
            case NORTH:
                dir = Direction.NORTH;
            case SOUTH:
                dir = Direction.SOUTH;
            case EAST:
                dir = Direction.EAST;
            case WEST:
                dir = Direction.WEST;
        }
        n = 0;
        for (BlockPos bp : getSphere(mc.player.getBlockPos(), radius.get(), radius.get())) {
            if (crops.get().contains(mc.world.getBlockState(bp).getBlock())) {
                n++;
                boneMeal(bp, bone_meal, dir);
                if (n >= count.get()) break;
            }
        }
    }

    private void boneMeal(BlockPos blockpos, FindItemResult bone_meal, Direction dir) {
        if (hands.get()) InvUtils.swap(bone_meal.slot(), true);
        BlockHitResult result = new BlockHitResult(new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 0.1, blockpos.getZ() + 0.5), dir, blockpos, true);
        ActionResult res = mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, result);
    }

    // thanks eureka :)
    public static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        List<BlockPos> blocks = new ArrayList<>();

        for (int i = centerPos.getX() - radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (centerPos.getSquaredDistance(pos) <= radius && !blocks.contains(pos)) blocks.add(pos);
                }
            }
        }
        return blocks;
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    private boolean blockFilter(Block block) {
        return block == Blocks.MELON_STEM ||
        block == Blocks.PUMPKIN_STEM ||
        block == Blocks.WHEAT ||
        block == Blocks.CARROTS ||
        block == Blocks.POTATOES ||
        block == Blocks.BEETROOTS ||
        block == Blocks.BAMBOO ||
        block == Blocks.OAK_SAPLING ||
        block == Blocks.SPRUCE_SAPLING ||
        block == Blocks.ACACIA_SAPLING ||
        block == Blocks.BIRCH_SAPLING ||
        block == Blocks.JUNGLE_SAPLING ||
        block == Blocks.DARK_OAK_SAPLING ||
        block == Blocks.SUNFLOWER ||
        block == Blocks.LILAC ||
        block == Blocks.ROSE_BUSH ||
        block == Blocks.PEONY ||
        block == Blocks.GRASS ||
        block == Blocks.FERN ||
        block == Blocks.SEAGRASS ||
        block == Blocks.RED_MUSHROOM ||
        block == Blocks.BROWN_MUSHROOM ||
        block == Blocks.COCOA ||
        block == Blocks.SWEET_BERRY_BUSH ||
        block == Blocks.SEA_PICKLE ||
        block == Blocks.CRIMSON_FUNGUS ||
        block == Blocks.WARPED_FUNGUS ||
        block == Blocks.WEEPING_VINES ||
        block == Blocks.TWISTING_VINES ||
        block == Blocks.CAVE_VINES ||
        block == Blocks.GLOW_LICHEN ||
        block == Blocks.BIG_DRIPLEAF ||
        block == Blocks.SMALL_DRIPLEAF ||
        block == Blocks.MOSS_BLOCK ||
        // block == Blocks.KELP_PLANT_BLOCK ||
        block == Blocks.ROOTED_DIRT;
    }

    public enum Direc {
        UP,
        DOWN,
        EAST,
        WEST,
        NORTH,
        SOUTH
    }
}
