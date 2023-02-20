package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import hybanos.addon.settings.Item2IntMapSetting;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.orbit.EventHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class Trash_can extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2IntMap<Item>> items = sgGeneral.add(new Item2IntMapSetting.Builder()
            .name("items")
            .description("The items you want to delete.")
            .defaultValue(Item2IntMapSetting.createItemMap())
            .build()
    );

    public Trash_can() {
        super(HAHAddon.CATEGORY, "Trash Can", "Deletes items in your inventory.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {

        if (mc.currentScreen instanceof HandledScreen<?>) return;

        int id = -1;
        int button = 50;

        ScreenHandler handler = mc.player.currentScreenHandler;

        for (Item item : items.get().keySet()) {
            int number = items.get().getInt(item);
            if (number <= 0) continue;

            FindItemResult result = InvUtils.find(item);

            if (result.count() >= number) {
                id = result.slot();
                break;
            }
        }

        if (id == -1) return;
        id = dataSlotToNetworkSlot(id);

        handler.onSlotClick(id, 50, SlotActionType.SWAP, mc.player);
        Int2ObjectMap<ItemStack> stacks = new Int2ObjectOpenHashMap<ItemStack>();

        if (!(mc.currentScreen instanceof HandledScreen<?>)) {
            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(0, handler.getRevision(), id, button, SlotActionType.SWAP, handler.getCursorStack().copy(), stacks));
        }
    }

    private static int dataSlotToNetworkSlot(int index) {
            if (index <= 8)
                index += 36;
            else if (index == 100)
                index = 8;
            else if (index == 101)
                index = 7;
            else if (index == 102)
                index = 6;
            else if (index == 103)
                index = 5;
            else if (index >= 80 && index <= 83)
                index -= 79;
            return index;
        }

        @Override
        public WWidget getWidget(GuiTheme theme) {
            WVerticalList list = theme.verticalList();
            list.add(theme.label("Select a number for the item you want to delete,")).widget();
            list.add(theme.label("when the number of item in your inventory is greater")).widget();
            list.add(theme.label("than the number you selected, a SLOT will be deleted.")).widget();
            list.add(theme.label("(0 to disable)")).widget();
            list.add(theme.label("Be careful.")).widget();

            return list;
        }

}
