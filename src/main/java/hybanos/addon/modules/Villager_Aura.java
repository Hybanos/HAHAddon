package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import hybanos.addon.mixin.VillagerEntityAccessor;
import hybanos.addon.mixin.HandledScreenAccessor;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.village.TradeOffer;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;

import java.util.List;
import java.util.ArrayList;

public class Villager_Aura extends Module {

    private final SettingGroup sgGeneral  = settings.getDefaultGroup();
    private final SettingGroup sgReset = settings.createGroup("RESET");
    private final SettingGroup sgChat = settings.createGroup("INFO");

    private final Setting<List<Item>> buying = sgGeneral.add(new ItemListSetting.Builder()
        .name("buy")
        .description("The items to buy.")
        .defaultValue(Items.COOKIE, Items.GOLDEN_CARROT)
        .filter(this::buyFilter)
        .onChanged(Item -> forget(true, true))
        .build()
    );

    private final Setting<List<Item>> selling = sgGeneral.add(new ItemListSetting.Builder()
        .name("sell")
        .description("The items to sell.")
        .filter(this::sellFilter)
        .onChanged(Item -> forget(true, true))
        .build()
    );

    private final Setting<Boolean> close = sgGeneral.add(new BoolSetting.Builder()
        .name("close")
        .description("Closes the inventory of villagers when all your trades are done.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> click = sgGeneral.add(new BoolSetting.Builder()
        .name("interact")
        .description("Opens the inventory of villagers in range.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("The max distance to click villagers.")
        .defaultValue(4)
        .sliderRange(0,5)
        .visible(() -> click.get())
        .build()
    );

    private final Setting<Integer> emptySlots = sgGeneral.add(new IntSetting.Builder()
        .name("free slots")
        .description("The number of empty inventory slots to keep.")
        .defaultValue(4)
        .sliderRange(0,10)
        .build()
    );

    private final Setting<Integer> resetTime = sgReset.add(new IntSetting.Builder()
        .name("timer")
        .description("Forgets what villagers you traded with every x seconds. (0 to disable)")
        .defaultValue(60)
        .sliderRange(0, 300)
        .build()
    );

    private final Setting<Boolean> resetRestock = sgReset.add(new BoolSetting.Builder()
        .name("on restock")
        .description("Forgets what villagers you traded with when refilling your inventory for new trades.")
        .defaultValue(true)
        .build()
    );

    // private final Setting<Boolean> resetClick = sgReset.add(new BoolSetting.Builder()
    //     .name("on click")
    //     .description("Forgets what villagers you traded with when manually clicking on one.")
    //     .defaultValue(true)
    //     .build()
    // );

    private final Setting<Boolean> info = sgChat.add(new BoolSetting.Builder()
        .name("info")
        .description("Gives you info on what you need to restock.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> stats = sgChat.add(new BoolSetting.Builder()
        .name("stats")
        .description("Displays some stats in chat when deactivating the module.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> debug = sgChat.add(new BoolSetting.Builder()
        .name("debug")
        .description("if the game crashes it's because of betterchat (fr)")
        .defaultValue(false)
        .visible(() -> false)
        .build()
    );

    public Villager_Aura() {
        super(HAHAddon.CATEGORY, "Villager Aura", "Buy and sell items to nearby villagers.");
    }

    int tradeCount = 0;
    int itemsSold = 0;
    int itemsBought = 0;
    int emeraldNet = 0;
    int emeraldCount = 0;
    int invTotal = 0;
    int lastAction = 0;
    int timer = 0;
    VillagerEntityAccessor lastVillager;
    Object2IntMap<VillagerEntityAccessor> villagerMap = new Object2IntArrayMap<>();


    @Override
    public void onActivate() {
        getVillagers();
        updateInventory();
        lastVillager = null;
        villagerMap = new Object2IntArrayMap<>();
        emeraldCount = InvUtils.find(Items.EMERALD).count();
        tradeCount = 0;
        itemsSold = 0;
        itemsBought = 0;
        emeraldNet = 0;
    }

    @Override
    public void onDeactivate() {
        if (!stats.get()) return;
        info("Number of trades : " + tradeCount);
        info("Items sold : " + itemsSold);
        info("Items bought : " + itemsBought);
        info("Emerald profit : " + emeraldNet);
    }

    @EventHandler
    public void onInteractEntity(InteractEntityEvent event) {
        if (event.entity instanceof VillagerEntity) {
            lastVillager = (VillagerEntityAccessor)event.entity;
            if (!villagerMap.containsKey(lastVillager)) villagerMap.put(lastVillager, 0);
            // if (resetClick.get()) forget(true, true);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        timer++;

        // villagerMap checks
        long time = mc.world.getTime() % 24000;
        if (click.get()) {

            if (timer % 200 == 0 || (timer % 40 == 0 && villagerMap.isEmpty())) {
                getVillagers();
            }

            if (time == 2000 || time == 4000 || time == 6000) {
                if (debug.get()) info("restocked, time : " + time);
                forget(true, true);
            }
        }

        // resets
        if (resetTime.get() > 0 && timer % (20 * resetTime.get()) == 0) {
            forget(true, true);
            if (debug.get()) info("reset time");
        }
        if (resetRestock.get() && !(mc.currentScreen instanceof MerchantScreen)) {
            updateInventory();
        }

        // buying / selling
        if (mc.currentScreen instanceof MerchantScreen) {
            boolean success;
            MerchantScreenHandler handler = ((MerchantScreenHandler)((HandledScreenAccessor)mc.currentScreen).getHandler());
            if (invFull()) return;
            success = trySell(handler);
            if (!success) success = tryBuy(handler);
            if (lastAction < timer && close.get()) mc.player.closeHandledScreen();
        }

        // interacting
        if (!(villagerMap.isEmpty() || mc.currentScreen instanceof HandledScreen<?>) && click.get()) {
            if (invFull()) return;
            for (VillagerEntityAccessor villager : villagerMap.keySet()) {
                if (villagerMap.get(villager) > 0) {
                    if (debug.get()) info("villager status : " + villagerMap.get(villager));
                    continue;
                }
                if (range.get() > PlayerUtils.distanceTo((Entity)villager)) {
                    mc.interactionManager.interactEntity(mc.player, (Entity)villager, Hand.MAIN_HAND);
                    lastVillager = villager;
                    break;
                }
            }
        }
    }

    // items to emeralds
    public boolean trySell(MerchantScreenHandler handler) {
        int index = 0;
        boolean ranOut = true;
        boolean allDisabled = true;
        boolean success = false;
        for (TradeOffer offer : handler.getRecipes()) {
            int uses = offer.getUses();
            if (selling.get().contains(offer.getAdjustedFirstBuyItem().getItem()) && !offer.isDisabled()) {
                allDisabled = false;
                FindItemResult item = InvUtils.find(offer.getAdjustedFirstBuyItem().getItem());
                if (item.count() >= offer.getAdjustedFirstBuyItem().getCount()) {
                    clickTrade(handler, index);
                    trade(handler);
                    resetSlots(handler);
                    lastAction = timer;
                    tradeCount = tradeCount + (offer.getUses() - uses);
                    itemsSold = itemsSold + (item.count() - InvUtils.find(offer.getAdjustedFirstBuyItem().getItem()).count());
                    emeraldNet = emeraldNet + offer.getSellItem().getCount() * (offer.getUses() - uses);
                    ranOut = false;
                    success = true;
                    if (debug.get()) info("sold something");

                    break;
                } else {
                    if (info.get()) info("You need more " + offer.getAdjustedFirstBuyItem().getItem().toString() + " !");
                }
            }
            index++;
        }
        if (ranOut) villagerMap.replace(lastVillager, 1);
        if (allDisabled) villagerMap.replace(lastVillager, 2);
        return success;
    }

    // emeralds to items
    public boolean tryBuy(MerchantScreenHandler handler) {
        emeraldCount = InvUtils.find(Items.EMERALD).count();
        int index = 0;
        boolean outOfEmeralds = false;
        boolean allDisabled = true;
        boolean success = false;
        for (TradeOffer offer : handler.getRecipes()) {
            int uses = offer.getUses();
            if (buying.get().contains(offer.getSellItem().getItem()) && !offer.isDisabled()) {
                allDisabled = false;
                if (emeraldCount >= offer.getAdjustedFirstBuyItem().getCount()) {
                    clickTrade(handler, index);
                    trade(handler);
                    resetSlots(handler);
                    lastAction = timer;
                    tradeCount = tradeCount + (offer.getUses() - uses);
                    emeraldNet = emeraldNet + (InvUtils.find(Items.EMERALD).count() - emeraldCount);
                    itemsBought = itemsBought + offer.getSellItem().getCount() * (offer.getUses() - uses);
                    success = true;
                    if (debug.get()) info("bought something");
                    break;
                } else {
                    if (info.get()) info("You need more emeralds !");
                    outOfEmeralds = true;
                }
            }
            index++;
        }
        if (outOfEmeralds) villagerMap.replace(lastVillager, 1);
        if (allDisabled) villagerMap.replace(lastVillager, 2);
        return success;
    }

    public void clickTrade(MerchantScreenHandler handler, int index) {
        handler.setRecipeIndex(index);
        handler.switchTo(index);
        mc.getNetworkHandler().sendPacket(new SelectMerchantTradeC2SPacket(index));
    }

    public void trade(MerchantScreenHandler handler) {
        InvUtils.quickMove().slotId(2);
    }

    public void resetSlots(MerchantScreenHandler handler) {
        InvUtils.quickMove().slotId(0);
        InvUtils.quickMove().slotId(1);
    }

    public void getVillagers() {
        ArrayList<VillagerEntityAccessor> villagerList = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof VillagerEntity)) continue;
            VillagerEntityAccessor entity = (VillagerEntityAccessor)e;
            boolean isIn = false;
            for (VillagerEntityAccessor villager : villagerMap.keySet()) {
                if (((Entity)villager).getId() == ((Entity)entity).getId()) {
                    isIn = true;
                }
            }
            if (!isIn)  {
                villagerMap.put(entity, 0);
            }
        }
        for (VillagerEntityAccessor villager : villagerMap.keySet()) villagerList.add(villager);
        for (VillagerEntityAccessor villager : villagerList) {
            boolean isIn = false;
            for (Entity entity : mc.world.getEntities()) {
                if (((Entity)villager).getId() == entity.getId()) isIn = true;
            }
            if (!isIn) {
                villagerMap.remove(villager);
            }
        }
    }

    public void forget(boolean disable, boolean restock) {
        for (VillagerEntityAccessor villager : villagerMap.keySet()) {
            if (villagerMap.get(villager) == 1 && restock) villagerMap.replace(villager, 0);
            if (villagerMap.get(villager) == 2 && disable) villagerMap.replace(villager, 0);
        }
    }

    public void updateInventory() {
        int newTotal = 0;
        for (Item item : selling.get()) {
            newTotal = newTotal + InvUtils.find(item).count();
        }
        newTotal = newTotal + InvUtils.find(Items.EMERALD).count();

        if (newTotal > invTotal) {
            if (debug.get()) info("reset restock");
            forget(true, false);
        }
        invTotal = newTotal;
    }

    public boolean invFull() {
        int airCount = 0;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.AIR) {
                airCount++;
            }
        }
        return airCount <= emptySlots.get();
    }

    private boolean buyFilter(Item item) {
        // Farmer
        return item == Items.BREAD ||
        item == Items.PUMPKIN_PIE ||
        item == Items.APPLE ||
        item == Items.COOKIE ||
        item == Items.SUSPICIOUS_STEW ||
        item == Items.CAKE ||
        item == Items.GOLDEN_CARROT ||
        item == Items.GLISTERING_MELON_SLICE ||

        // Butcher
        item == Items.RABBIT_STEW ||
        item == Items.COOKED_PORKCHOP ||
        item == Items.COOKED_CHICKEN ||

        // Fisherman
        item == Items.COD_BUCKET ||
        item == Items.COOKED_COD ||
        item == Items.CAMPFIRE ||
        item == Items.COOKED_SALMON ||
        item == Items.FISHING_ROD ||

        // Librarian
        item == Items.BOOKSHELF ||
        item == Items.ENCHANTED_BOOK ||
        item == Items.LANTERN ||
        item == Items.GLASS ||
        item == Items.CLOCK ||
        item == Items.COMPASS ||
        item == Items.NAME_TAG ||

        // Armorer
        item == Items.IRON_BOOTS ||
        item == Items.IRON_LEGGINGS ||
        item == Items.IRON_CHESTPLATE ||
        item == Items.IRON_HELMET ||
        item == Items.DIAMOND_BOOTS ||
        item == Items.DIAMOND_LEGGINGS ||
        item == Items.DIAMOND_CHESTPLATE ||
        item == Items.DIAMOND_HELMET ||
        item == Items.BELL ||
        item == Items.SHIELD ||

        // Toolsmith
        item == Items.STONE_AXE ||
        item == Items.STONE_PICKAXE ||
        item == Items.STONE_HOE ||
        item == Items.STONE_SHOVEL ||
        item == Items.IRON_AXE ||
        item == Items.IRON_PICKAXE ||
        item == Items.IRON_HOE ||
        item == Items.IRON_SHOVEL ||
        item == Items.DIAMOND_AXE ||
        item == Items.DIAMOND_PICKAXE ||
        item == Items.DIAMOND_HOE ||
        item == Items.DIAMOND_SHOVEL ||

        // Shepherd
        item == Items.SHEARS ||
        item == Items.WHITE_WOOL ||
        item == Items.ORANGE_WOOL ||
        item == Items.MAGENTA_WOOL ||
        item == Items.LIGHT_BLUE_WOOL ||
        item == Items.YELLOW_WOOL ||
        item == Items.LIME_WOOL ||
        item == Items.PINK_WOOL ||
        item == Items.GRAY_WOOL ||
        item == Items.LIGHT_GRAY_WOOL ||
        item == Items.CYAN_WOOL ||
        item == Items.PURPLE_WOOL ||
        item == Items.BLUE_WOOL ||
        item == Items.BROWN_WOOL ||
        item == Items.GREEN_WOOL ||
        item == Items.RED_WOOL ||
        item == Items.BLACK_WOOL ||
        item == Items.WHITE_CARPET ||
        item == Items.ORANGE_CARPET ||
        item == Items.MAGENTA_CARPET ||
        item == Items.LIGHT_BLUE_CARPET ||
        item == Items.YELLOW_CARPET ||
        item == Items.LIME_CARPET ||
        item == Items.PINK_CARPET ||
        item == Items.GRAY_CARPET ||
        item == Items.LIGHT_GRAY_CARPET ||
        item == Items.CYAN_CARPET ||
        item == Items.PURPLE_CARPET ||
        item == Items.BLUE_CARPET ||
        item == Items.BROWN_CARPET ||
        item == Items.GREEN_CARPET ||
        item == Items.RED_CARPET ||
        item == Items.BLACK_CARPET ||
        item == Items.PAINTING ||
        item == Items.WHITE_BED ||
        item == Items.ORANGE_BED ||
        item == Items.MAGENTA_BED ||
        item == Items.LIGHT_BLUE_BED ||
        item == Items.YELLOW_BED ||
        item == Items.LIME_BED ||
        item == Items.PINK_BED ||
        item == Items.GRAY_BED ||
        item == Items.LIGHT_GRAY_BED ||
        item == Items.CYAN_BED ||
        item == Items.PURPLE_BED ||
        item == Items.BLUE_BED ||
        item == Items.BROWN_BED ||
        item == Items.GREEN_BED ||
        item == Items.RED_BED ||
        item == Items.BLACK_BED  ||

        // Leatherworker
        item == Items.LEATHER_BOOTS ||
        item == Items.LEATHER_LEGGINGS ||
        item == Items.LEATHER_CHESTPLATE ||
        item == Items.LEATHER_HELMET ||
        item == Items.LEATHER_HORSE_ARMOR ||
        item == Items.SADDLE ||

        // Cleric
        item == Items.REDSTONE ||
        item == Items.LAPIS_LAZULI ||
        item == Items.GLOWSTONE ||
        item == Items.ENDER_PEARL ||
        item == Items.EXPERIENCE_BOTTLE ||

        // Cartographer
        item == Items.MAP ||
        item == Items.FILLED_MAP ||
        item == Items.ITEM_FRAME ||
        item == Items.WHITE_BANNER ||
        item == Items.ORANGE_BANNER ||
        item == Items.MAGENTA_BANNER ||
        item == Items.LIGHT_BLUE_BANNER ||
        item == Items.YELLOW_BANNER ||
        item == Items.LIME_BANNER ||
        item == Items.PINK_BANNER ||
        item == Items.GRAY_BANNER ||
        item == Items.LIGHT_GRAY_BANNER ||
        item == Items.CYAN_BANNER ||
        item == Items.PURPLE_BANNER ||
        item == Items.BLUE_BANNER ||
        item == Items.BROWN_BANNER ||
        item == Items.GREEN_BANNER ||
        item == Items.RED_BANNER ||
        item == Items.BLACK_BANNER ||
        item == Items.FLOWER_BANNER_PATTERN ||
        item == Items.CREEPER_BANNER_PATTERN ||
        item == Items.MOJANG_BANNER_PATTERN ||
        item == Items.SKULL_BANNER_PATTERN ||
        item == Items.GLOBE_BANNER_PATTERN ||
        item == Items.PIGLIN_BANNER_PATTERN ||

        // Fletcher
        item == Items.ARROW ||
        item == Items.FLINT ||
        item == Items.BOW ||
        item == Items.CROSSBOW ||
        item == Items.TIPPED_ARROW ||

        // Weaponsmith
        item == Items.IRON_AXE ||
        item == Items.IRON_SWORD ||
        item == Items.DIAMOND_AXE ||
        item == Items.DIAMOND_SWORD ||

        // Mason
        item == Items.BRICK ||
        item == Items.CHISELED_STONE_BRICKS ||
        item == Items.WHITE_TERRACOTTA ||
        item == Items.ORANGE_TERRACOTTA ||
        item == Items.MAGENTA_TERRACOTTA ||
        item == Items.LIGHT_BLUE_TERRACOTTA ||
        item == Items.YELLOW_TERRACOTTA ||
        item == Items.LIME_TERRACOTTA ||
        item == Items.PINK_TERRACOTTA ||
        item == Items.GRAY_TERRACOTTA ||
        item == Items.LIGHT_GRAY_TERRACOTTA ||
        item == Items.CYAN_TERRACOTTA ||
        item == Items.PURPLE_TERRACOTTA ||
        item == Items.BLUE_TERRACOTTA ||
        item == Items.BROWN_TERRACOTTA ||
        item == Items.GREEN_TERRACOTTA ||
        item == Items.RED_TERRACOTTA ||
        item == Items.BLACK_TERRACOTTA ||
        item == Items.WHITE_GLAZED_TERRACOTTA ||
        item == Items.ORANGE_GLAZED_TERRACOTTA ||
        item == Items.MAGENTA_GLAZED_TERRACOTTA ||
        item == Items.LIGHT_BLUE_GLAZED_TERRACOTTA ||
        item == Items.YELLOW_GLAZED_TERRACOTTA ||
        item == Items.LIME_GLAZED_TERRACOTTA ||
        item == Items.PINK_GLAZED_TERRACOTTA ||
        item == Items.GRAY_GLAZED_TERRACOTTA ||
        item == Items.LIGHT_GRAY_GLAZED_TERRACOTTA ||
        item == Items.CYAN_GLAZED_TERRACOTTA ||
        item == Items.PURPLE_GLAZED_TERRACOTTA ||
        item == Items.BLUE_GLAZED_TERRACOTTA ||
        item == Items.BROWN_GLAZED_TERRACOTTA ||
        item == Items.GREEN_GLAZED_TERRACOTTA ||
        item == Items.RED_GLAZED_TERRACOTTA ||
        item == Items.BLACK_GLAZED_TERRACOTTA;
    }

    private boolean sellFilter(Item item) {

        // Farmer
        return item == Items.WHEAT ||
        item == Items.BEETROOT ||
        item == Items.CARROT ||
        item == Items.POTATO ||
        item == Items.PUMPKIN ||
        item == Items.MELON ||

        // Butcher
        item == Items.CHICKEN ||
        item == Items.PORKCHOP ||
        item == Items.RABBIT ||
        item == Items.COAL ||
        item == Items.MUTTON ||
        item == Items.BEEF ||
        item == Items.DRIED_KELP_BLOCK ||
        item == Items.SWEET_BERRIES ||

        // Fisherman
        item == Items.STRING ||
        item == Items.COD ||
        item == Items.SALMON ||
        item == Items.TROPICAL_FISH ||
        item == Items.PUFFERFISH ||
        item == Items.OAK_BOAT ||
        item == Items.SPRUCE_BOAT ||
        item == Items.ACACIA_BOAT ||
        item == Items.BIRCH_BOAT ||
        item == Items.DARK_OAK_BOAT ||

        // Librarian
        item == Items.PAPER ||
        item == Items.BOOK ||
        item == Items.INK_SAC ||
        item == Items.WRITABLE_BOOK ||

        // Armorer
        item == Items.IRON_ORE ||
        item == Items.LAVA_BUCKET ||
        item == Items.DIAMOND ||

        // Toolsmith
        item == Items.FLINT ||

        // Shepherd
        item == Items.WHITE_WOOL ||
        item == Items.BLACK_WOOL ||
        item == Items.GRAY_WOOL ||
        item == Items.BROWN_WOOL ||
        item == Items.WHITE_DYE ||
        item == Items.LIGHT_GRAY_DYE ||
        item == Items.BLACK_DYE ||
        item == Items.LIGHT_BLUE_DYE ||
        item == Items.LIME_DYE ||
        item == Items.YELLOW_DYE ||
        item == Items.ORANGE_DYE ||
        item == Items.RED_DYE ||
        item == Items.GRAY_DYE ||
        item == Items.PURPLE_DYE ||
        item == Items.MAGENTA_DYE ||
        item == Items.PINK_DYE ||
        item == Items.BLUE_DYE ||
        item == Items.CYAN_DYE ||
        item == Items.GREEN_DYE ||
        item == Items.BROWN_DYE ||

        // Leatherworker
        item == Items.LEATHER ||
        item == Items.RABBIT_HIDE ||

        // Cleric
        item == Items.ROTTEN_FLESH ||
        item == Items.GOLD_INGOT ||
        item == Items.RABBIT_FOOT ||
        item == Items.GLASS_BOTTLE ||
        item == Items.NETHER_WART ||

        // Cartographer
        item == Items.GLASS_PANE ||
        item == Items.COMPASS ||

        // Fletcher
        item == Items.STICK ||
        item == Items.STRING ||
        item == Items.FEATHER ||
        item == Items.TRIPWIRE_HOOK ||

        // Weaponsmith

        // Mason
        item == Items.CLAY ||
        item == Items.GRAVEL ||
        item == Items.DIORITE ||
        item == Items.GRANITE ||
        item == Items.ANDESITE ||
        item == Items.POLISHED_DIORITE ||
        item == Items.POLISHED_GRANITE ||
        item == Items.POLISHED_ANDESITE ||
        item == Items.QUARTZ;
    }
}
