package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;


public class Test extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> ppt = sgGeneral.add(new IntSetting.Builder()
        .name("ppt")
        .description("packets per tick")
        .defaultValue(10)
        .build()
    );

    public Test() {
        super(HAHAddon.CATEGORY, "Test", "funny stuff");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Int2ObjectMap<ItemStack> map = new Int2ObjectArrayMap<ItemStack>();
        map.put(0, new ItemStack(Items.COCOA_BEANS, 1));

        for (int i=0; i<ppt.get();i++) {
            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(mc.player.currentScreenHandler.syncId, 12334, 2957234, 2859623, SlotActionType.PICKUP, new ItemStack(Items.AIR, -1), map));
        }
    }
}
